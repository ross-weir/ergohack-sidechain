package sidechain.relay

import org.ergoplatform.{DataInput, ErgoBox, ErgoBoxCandidate, UnsignedInput}
import org.ergoplatform.ErgoBox.{R4, R5, R6, R7, R8}
import org.ergoplatform.modifiers.mempool.UnsignedErgoTransaction
import org.ergoplatform.nodeView.state.ErgoStateContext
import org.ergoplatform.settings.{ChainSettingsReader, LaunchParameters}
import org.ergoplatform.wallet.interpreter.ErgoProvingInterpreter
import org.scalatest.matchers.should.Matchers
import org.scalatest.propspec.AnyPropSpec
import scorex.crypto.authds.ADDigest
import scorex.util.ModifierId
import scorex.util.encode.Base16
import scorex.utils.Longs
import sidechain.Constants
import sigma.Colls
import sigma.ast.{AvlTreeConstant, BigIntConstant, ByteArrayConstant, CollectionConstant, EvaluatedValue, IntConstant, SByte, SCollection, SType}
import sigma.data.{AvlTreeFlags, Digest32Coll}
import sigma.interpreter.ContextExtension
import sigma.util.NBitsUtils
import work.lithos.plasma.PlasmaParameters
import work.lithos.plasma.collections.PlasmaMap
import java.math.BigInteger

class BitcoinRelaySpec  extends AnyPropSpec with Matchers with TestHelpers {

  def getBlockWork(nBits: Long): BigInt = {
    val target = NBitsUtils.decodeCompactBits(nBits)

    (BigInt(1) << 256) / (target + BigInt(1))
  }

  property("Adding header to best chain") {
    // add block 566,093 on top of 566,092

    //566,092
    val h1Hex = "00000020a82ff9c62e69a6cbed277b7f2a9ac9da3c7133a59a6305000000000000000000f6cd5708a6ba38d8501502b5b4e5b93627e8dcc9bd13991894c6e04ade262aa99582815c505b2e17479a751b"
    val h1Bytes = fromHex(h1Hex)
    val h1Id = hash(h1Bytes)
    val h1Height = 566092
    val h1HeaderAndHeight = h1Bytes ++ Longs.toByteArray(h1Height)

    val h1CumWork = new BigInteger("100500500500")
    //566,093
    val h2Hex = "00000020b45e33a345ad08ad2902cdd4101632fcbec009694b0c2500000000000000000016c99a795d8e0105d86f361341c7858d223fac261718bd608052822c5b4ae3cfd782815c505b2e17a56bb90b"
    val h2Bytes = fromHex(h2Hex)
    val h2Id = hash(h2Bytes)
    val h2Height = h1Height + 1
    val h2HeaderAndHeight = h2Bytes ++ Longs.toByteArray(h2Height)
    val h2Nbits = Longs.fromByteArray(Array.fill(4)(0.toByte) ++ h2Bytes.slice(72, 76).reverse)
    val h2CumWork = h1CumWork.add(getBlockWork(h2Nbits).bigInteger)

    // in tx input, only block @ 566,092 is committed, in the output, 566,093

    val bestChainPlasmaMap = new PlasmaMap[Array[Byte], Array[Byte]](AvlTreeFlags.InsertOnly, PlasmaParameters.default)
    bestChainPlasmaMap.insert(h1Id -> h1HeaderAndHeight)
    val bestChainTree1 = bestChainPlasmaMap.ergoValue.getValue

    val allHeadersPlasmaMap = new PlasmaMap[Array[Byte], Array[Byte]](AvlTreeFlags.InsertOnly, PlasmaParameters.default)
    val h1Record = h1HeaderAndHeight ++ bestChainTree1.digest.toArray ++ h1CumWork.toByteArray
    allHeadersPlasmaMap.insert(h1Id -> h1Record)
    val allHeadersTree1 = bestChainPlasmaMap.ergoValue.getValue

    val h1LookupRes = allHeadersPlasmaMap.lookUp(h1Id)

    val relayInput = new ErgoBox (
      value = 100000000L, // does not matter
      ergoTree = Constants.btcRelayErgoTree,
      Colls.fromItems((Digest32Coll @@ Colls.fromArray(relayNftId)) -> 1),
      additionalRegisters = Map(
        R4 -> AvlTreeConstant(bestChainTree1),
        R5 -> AvlTreeConstant(allHeadersTree1),
        R6 -> IntConstant(h1Height),
        R7 -> ByteArrayConstant(h1Id),
        R8 -> BigIntConstant(h1CumWork)
      ),
      ModifierId @@ Base16.encode(h1Id),
      0.toShort,
      creationHeight = 0
    )

    val insertH2BestRes = bestChainPlasmaMap.insert(h2Id -> h2HeaderAndHeight)
    val bestChainTree2 = bestChainPlasmaMap.ergoValue.getValue

    val h2Record = h2HeaderAndHeight ++ bestChainTree2.digest.toArray ++ h2CumWork.toByteArray
    val insertH2AllRes = allHeadersPlasmaMap.insert((h2Id -> h2Record))
    val allHeadersTree2 = allHeadersPlasmaMap.ergoValue.getValue

    val relayOutput = new ErgoBoxCandidate (
      value = 100000000L, // does not matter
      ergoTree = Constants.btcRelayErgoTree,
      creationHeight = 0,
      Colls.fromItems((Digest32Coll @@ Colls.fromArray(relayNftId)) -> 1),
      additionalRegisters = Map(
        R4 -> AvlTreeConstant(bestChainTree2),
        R5 -> AvlTreeConstant(allHeadersTree2),
        R6 -> IntConstant(h2Height),
        R7 -> ByteArrayConstant(h2Id),
        R8 -> BigIntConstant(h2CumWork)
      )
    )


    // #1 - new header bytes
    // #2 - best chain tree insert proof // if tip update
    // #3 - parent header lookup proof in all headers db
    // #4 - parent header's best chain digest
    // #5 - parent header's best chain insert proof
    // #6 - all headers insert proof
    // #7 - new header's cumulative work as byte array
    val contextVars: Map[Byte, EvaluatedValue[_ <: SType]] = Map(
      1.toByte -> ByteArrayConstant(h2Bytes),
      2.toByte -> ByteArrayConstant(insertH2BestRes.proof.bytes),
      3.toByte -> ByteArrayConstant(h1LookupRes.proof.bytes),
      4.toByte -> AvlTreeConstant(bestChainTree1),
      5.toByte -> ByteArrayConstant(insertH2BestRes.proof.bytes),
      6.toByte -> ByteArrayConstant(insertH2AllRes.proof.bytes),
      7.toByte -> ByteArrayConstant(h2CumWork.toByteArray)
    )

    val inputs = IndexedSeq(new UnsignedInput(relayInput.id, ContextExtension(contextVars)))
    val dataInputs = IndexedSeq.empty[DataInput]
    val outputCandidates = IndexedSeq(relayOutput)

    val unsignedTx = new UnsignedErgoTransaction(inputs, dataInputs, outputCandidates)

    // no secrets needed
    val prover = ErgoProvingInterpreter(IndexedSeq(), LaunchParameters)
    val chainSettings = ChainSettingsReader.read("src/test/resources/application.conf").get
    val genesisStateDigest: ADDigest = chainSettings.genesisStateDigest
    val esc = ErgoStateContext.empty(genesisStateDigest, chainSettings, LaunchParameters)

    prover.sign(unsignedTx, IndexedSeq(relayInput), IndexedSeq.empty, esc).get

  }

  property("Adding header to non-best chain w. switching") {

  }

}
