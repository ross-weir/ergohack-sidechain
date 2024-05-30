package sidechain.relay

import org.ergoplatform.ErgoBox.{AdditionalRegisters, R4, R5, R6, R7, R8, R9, Token}
import org.ergoplatform.{DataInput, ErgoBox, UnsignedInput}
import org.ergoplatform.modifiers.mempool.{ErgoTransaction, UnsignedErgoTransaction}
import org.ergoplatform.nodeView.state.ErgoStateContext
import org.ergoplatform.settings.{ChainSettings, ChainSettingsReader, LaunchParameters}
import org.ergoplatform.wallet.interpreter.ErgoProvingInterpreter
import org.scalatest.matchers.should.Matchers
import org.scalatest.propspec.AnyPropSpec
import scorex.crypto.authds.ADDigest
import scorex.crypto.hash.Sha256
import scorex.util.ModifierId
import scorex.util.encode.Base16
import scorex.utils.Longs
import sidechain.Constants
import sigma.{Coll, Colls}
import sigma.ast.syntax.CollectionConstant
import sigma.ast.{AvlTreeConstant, BigIntConstant, ByteArrayConstant, CollectionConstant, EvaluatedValue, IntConstant, SByte, SCollection, SType}
import sigma.data.{AvlTreeFlags, Digest32Coll}
import sigma.interpreter.ContextExtension
import work.lithos.plasma.PlasmaParameters
import work.lithos.plasma.collections.PlasmaMap

class BitcoinMerkleProofSpec extends AnyPropSpec with Matchers with TestHelpers {

  property("Proper Merkle proof should be validated") {

    val height = 93500
    val header = "01000000076379e2c0ec4a614ad1bf0ec716e6873f2c7abac604a08cc78e070000000000579a6bbcd07e9c3d622672ad20495d4485b5233395ab4081db7cab0fd2b577d2396cec4c2a8b091b031a7313"
    val id = Base16.decode("000000000003b8e6533b3f238ee00ff8dd68c3a2377a213f7a72c3ef0fe0c54b").get
    val headerAndHeight = fromHex(header) ++ Longs.toByteArray(93500)


    val txHex = "0100000001eba8353ac2e5503f15548975108013246457ed83d331db760f0595b8bd7c54cb000000008c4930460221008c64f29882d9a59cbb070d75b4cdca56c04b523b0af37a0ffecee24e31cb2814022100b183ab317ad217f4a6f4e610c6138e5c2d7681d40f46201f268a5a90c1c07afa0141040b362c040204c13f6e1ec78b60978bdd76d851d4a1612cd9e82ead5177694f8f37fa4e8c78579876bbaf8a561772f320d3125f36cd1f1c5e9eb3f8bc08b626d2ffffffff0280e9fd97000000001976a914f0630fd41ff0722cf29de4db609f06a4c17fad2d88ac002a7515000000001976a9141dea9e37227b8d7a6296849fc76e00e8f5a6674e88ac00000000"
    val txBytes = fromHex(txHex)

    val txId = hash(txBytes)

    val plasmaMap = new PlasmaMap[Array[Byte], Array[Byte]](AvlTreeFlags.InsertOnly, PlasmaParameters.default)
    plasmaMap.insert(id -> headerAndHeight)
    val outTree = plasmaMap.ergoValue.getValue

    val headerLookUpProof = plasmaMap.lookUp(id).proof

    val relayDataInput = new ErgoBox (
      value = 100000000L, // does not matter
      ergoTree = Constants.btcRelayErgoTree,
      Colls.fromItems((Digest32Coll @@ Colls.fromArray(relayNftId)) -> 1),
      additionalRegisters = Map(
        R4 -> AvlTreeConstant(outTree),
        R5 -> AvlTreeConstant(outTree), // fake value
        R6 -> IntConstant(height + 6),  // min confs met
        R7 -> ByteArrayConstant(id),    // fake value
        R8 -> BigIntConstant(0)         // fake value
      ),
      ModifierId @@ Base16.encode(txId),
      0.toShort,
      creationHeight = 0
    )

    val txCheckInput = new ErgoBox (
      value = 100000000L, // does not matter
      ergoTree = Constants.btcTxCheckErgoTree,
      Colls.emptyColl[Token],
      additionalRegisters = Map(),
      ModifierId @@ Base16.encode(txId.reverse),
      0.toShort,
      creationHeight = 0
    )

    val tx1 = fromHex("a7c2b4a2cc940f9f541905048fe8352bd158dab18d15221fab7ee2187bd3cb5e")
    val tx2 = fromHex("1d74396699ae0effcd67fd5d031b780ff56c336bfc5d2d015d21db687d732764")
    val tx3Id = fromHex("d8c9d6a13a7fb8236833b1e93d298f4626deeb78b2f1814aa9a779961c08ce39")


    // tx of interest is #3
    val merkleProof = Array(
      1.toByte +: tx3Id.reverse,
      0.toByte +: hash(tx1.reverse ++ tx2.reverse)
    ).map(arr => Colls.fromArray(arr))

    /*
    //todo: debugging code commented out now, remove

    val root = hash(hash (tx1.reverse ++ tx2.reverse) ++ hash(tx3Id.reverse ++ tx3Id.reverse))

    def computeLevel(prevHash: Coll[Byte], proofElem: Coll[Byte]) = {
      val elemHash = proofElem.slice(1,33)
      if(proofElem(0) == 0){
        Colls.fromArray(hash(elemHash.append(prevHash).toArray))
      } else {
        Colls.fromArray(hash(prevHash.append(elemHash).toArray))
      }
    }

    val computedMerkleRoot = merkleProof.fold(Colls.fromArray(txId))(computeLevel)
     */

    val contextVars: Map[Byte, EvaluatedValue[_ <: SType]] = Map(
      1.toByte -> ByteArrayConstant(txBytes),
      2.toByte -> ByteArrayConstant(id),
      3.toByte -> ByteArrayConstant(headerLookUpProof.bytes),
      4.toByte -> CollectionConstant[SCollection[SByte.type]](Colls.fromArray(merkleProof), SCollection(SByte))
    )

    val inputs = IndexedSeq(new UnsignedInput(txCheckInput.id, ContextExtension(contextVars)))
    val dataInputs = IndexedSeq(DataInput(relayDataInput.id))
    val outputCandidates = IndexedSeq(txCheckInput.toCandidate)

    val unsignedTx = new UnsignedErgoTransaction(inputs, dataInputs, outputCandidates)

    // no secrets needed
    val prover = ErgoProvingInterpreter(IndexedSeq(), LaunchParameters)
    val chainSettings = ChainSettingsReader.read("src/test/resources/application.conf").get
    val genesisStateDigest: ADDigest = chainSettings.genesisStateDigest
    val esc = ErgoStateContext.empty(genesisStateDigest, chainSettings, LaunchParameters)

    prover.sign(unsignedTx, IndexedSeq(txCheckInput), IndexedSeq(relayDataInput), esc).get
  }

}
