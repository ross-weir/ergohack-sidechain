package relay

import org.ergoplatform.ErgoBox.{AdditionalRegisters, Token}
import org.ergoplatform.{DataInput, ErgoBox}
import org.ergoplatform.modifiers.mempool.ErgoTransaction
import org.ergoplatform.settings.LaunchParameters
import org.ergoplatform.wallet.interpreter.ErgoProvingInterpreter
import org.scalatest.matchers.should.Matchers
import org.scalatest.propspec.AnyPropSpec
import scorex.util.ModifierId
import scorex.util.encode.Base16
import sidechain.Constants
import sigma.Colls

class BitcoinMerkleProofSpec extends AnyPropSpec with Matchers {

  property("Proper Merkle proof should be validated") {

    val txId = Array.fill(32)(0.toByte)

    val relayDataInput = new ErgoBox (
      value = 100000000L, // does not matter
      ergoTree = Constants.btcRelayErgoTree,
      Colls.emptyColl[Token],
      additionalRegisters = Map.empty,
      ModifierId @@ Base16.encode(txId),
      0.toShort,
      creationHeight = 0
    )

    val inputs = IndexedSeq()
    val dataInputs = IndexedSeq(DataInput(relayDataInput.id))
    val outputCandidates = IndexedSeq()

    val tx = new ErgoTransaction(inputs, dataInputs, outputCandidates)

    // no secrets needed
    val prover = ErgoProvingInterpreter(IndexedSeq(), LaunchParameters)



  }

}
