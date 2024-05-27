package sidechain

import org.ergoplatform.sdk.NetworkType
import org.ergoplatform.{ErgoAddress, ErgoAddressEncoder}
import sigma.VersionContext
import sigma.ast.ErgoTree
import sigma.ast.syntax.ValueOps
import sigmastate.eval.CompiletimeIRContext
import sigmastate.interpreter.Interpreter.ScriptEnv
import sigmastate.lang.SigmaCompiler


object Constants {

  val networkType = NetworkType.Mainnet
  val networkPrefix = networkType.networkPrefix
  val ergoAddressEncoder = new ErgoAddressEncoder(networkPrefix)

  def getAddressFromErgoTree(ergoTree: ErgoTree): ErgoAddress = ergoAddressEncoder.fromProposition(ergoTree).get

  // totally inefficient substitution method, but ok for our contracts
  def substitute(contract: String, substitutionMap: Map[String, String]): String = {
    substitutionMap.foldLeft(contract) { case (c, (k, v)) =>
      c.replace("$" + k, v)
    }
  }

  def readContract(path: String, substitutionMap: Map[String, String] = Map.empty): String = {
    substitute(scala.io.Source.fromFile("contracts/" + path, "utf-8").getLines.mkString("\n"), substitutionMap)
  }

  def compile(ergoScript: String): ErgoTree = {
    val compiler = SigmaCompiler(networkPrefix)
    val env: ScriptEnv = Map.empty
    implicit val IR = new CompiletimeIRContext
    val res = compiler.compile(env, ergoScript)
    ErgoTree.fromProposition(res.buildTree.asSigmaProp)
 }

  val sidechainStateContract = readContract("MainChain/SideChainState.es")
  val sidechainStateErgoTree = compile(sidechainStateContract)
  val sidechainStateAddress = getAddressFromErgoTree(sidechainStateErgoTree)

  val doubleUnlockPreventionContract = readContract("DoubleUnlockPrevention.es")
  val doubleUnlockPreventionErgoTree = compile(doubleUnlockPreventionContract)
  val doubleUnlockPreventionAddress = getAddressFromErgoTree(doubleUnlockPreventionErgoTree)

  val transferUnlockContract = readContract("MainChain/Unlock.es")
  val transferUnlockErgoTree = compile(transferUnlockContract)
  val transferUnlockAddress = getAddressFromErgoTree(transferUnlockErgoTree)

  val sidechainUnlockContract = readContract("SideChain/Unlock.es")
  val sidechainUnlockErgoTree = compile(sidechainUnlockContract)
  val sidechainUnlockAddress = getAddressFromErgoTree(sidechainUnlockErgoTree)

  val sidechainUnlockCompleteContract = readContract("SideChain/UnlockComplete.es")
  val sidechainUnlockCompleteErgoTree = compile(sidechainUnlockCompleteContract)
  val sidechainUnlockCompleteAddress = getAddressFromErgoTree(sidechainUnlockCompleteErgoTree)

  val btcRelayAddress = VersionContext.withVersions(VersionContext.V6SoftForkVersion, 0) {
    // relay contracts
    val btcRelayContract = readContract("relay/BtcRelay.es")
    val btcRelayErgoTree = compile(btcRelayContract)
    getAddressFromErgoTree(btcRelayErgoTree)
  }

}

object Printer extends App {
  import Constants._

  println(s"Btc relay contract address: $btcRelayAddress")

  println(s"Sidechain state contract address: ${sidechainStateAddress}")
  println(s"Double unlock prevention contract address: ${doubleUnlockPreventionAddress}")
  println(s"Mainchain unlock contract address: ${transferUnlockAddress}")
  println(s"Sidechain unlock contract address: ${sidechainUnlockAddress}")
  println(s"Sidechain unlock-complete contract address: ${sidechainUnlockCompleteAddress}")
}