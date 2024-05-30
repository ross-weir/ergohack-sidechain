package sidechain.relay

import org.ergoplatform.{DataInput, ErgoBox, UnsignedInput}
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

class BitcoinRelaySpec  extends AnyPropSpec with Matchers with TestHelpers {

  def getBlockWok(nBits: Long): BigInt = {
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

    //566,093
    val h2Hex = "00000020b45e33a345ad08ad2902cdd4101632fcbec009694b0c2500000000000000000016c99a795d8e0105d86f361341c7858d223fac261718bd608052822c5b4ae3cfd782815c505b2e17a56bb90b"
    val h2Bytes = fromHex(h2Hex)
    val h2Height = h1Height + 1


    // in tx input, only block @ 566,092 is committed, in the output, 566,093
    
    val bestChainPlasmaMap = new PlasmaMap[Array[Byte], Array[Byte]](AvlTreeFlags.InsertOnly, PlasmaParameters.default)
    bestChainPlasmaMap.insert(h1Id -> h1HeaderAndHeight)
    val bestChainTree = bestChainPlasmaMap.ergoValue.getValue

    val relayInput = new ErgoBox (
      value = 100000000L, // does not matter
      ergoTree = Constants.btcRelayErgoTree,
      Colls.fromItems((Digest32Coll @@ Colls.fromArray(relayNftId)) -> 1),
      additionalRegisters = Map(
        R4 -> AvlTreeConstant(bestChainTree),
        R5 -> AvlTreeConstant(bestChainTree), // fake value
        R6 -> IntConstant(566092),
        R7 -> ByteArrayConstant(h1Id),
        R8 -> BigIntConstant(100500500500L)         // fake value
      ),
      ModifierId @@ Base16.encode(h1Id),
      0.toShort,
      creationHeight = 0
    )


    val contextVars: Map[Byte, EvaluatedValue[_ <: SType]] = Map(
    )


    val inputs = IndexedSeq(new UnsignedInput(relayInput.id, ContextExtension(contextVars)))
    val dataInputs = IndexedSeq.empty[DataInput]
    val outputCandidates = IndexedSeq()

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
