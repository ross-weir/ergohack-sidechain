package sidechain

import org.ergoplatform.{ErgoAddress, ErgoAddressEncoder}
import org.ergoplatform.appkit.{AppkitHelpers, NetworkType}
import sigmastate.Values.ErgoTree

import java.util

object Constants {

  val networkType = NetworkType.MAINNET
  val networkPrefix = networkType.networkPrefix
  val ergoAddressEncoder = new ErgoAddressEncoder(networkPrefix)

  def getAddressFromErgoTree(ergoTree: ErgoTree): ErgoAddress = ergoAddressEncoder.fromProposition(ergoTree).get

  def compile(ergoScript: String): ErgoTree = {
    AppkitHelpers.compile(new util.HashMap[String, Object](), ergoScript, networkPrefix)
  }

  val sidechainStateContract = scala.io.Source.fromFile("contracts/MainChain/SideChainState.es", "utf-8").getLines.mkString("\n")
  val sidechainStateErgoTree = compile(sidechainStateContract)
  val sidechainStateAddress = getAddressFromErgoTree(sidechainStateErgoTree)

  val doubleUnlockPreventionContract = scala.io.Source.fromFile("contracts/DoubleUnlockPrevention.es", "utf-8").getLines.mkString("\n")
  val doubleUnlockPreventionErgoTree = compile(doubleUnlockPreventionContract)
  val doubleUnlockPreventionAddress = getAddressFromErgoTree(doubleUnlockPreventionErgoTree)

  val transferUnlockContract = scala.io.Source.fromFile("contracts/MainChain/Unlock.es", "utf-8").getLines.mkString("\n")
  val transferUnlockErgoTree = compile(transferUnlockContract)
  val transferUnlockAddress = getAddressFromErgoTree(transferUnlockErgoTree)

  val sidechainUnlockContract = scala.io.Source.fromFile("contracts/SideChain/Unlock.es", "utf-8").getLines.mkString("\n")
  val sidechainUnlockErgoTree = compile(sidechainUnlockContract)
  val sidechainUnlockAddress = getAddressFromErgoTree(sidechainUnlockErgoTree)

  val sidechainUnlockCompleteContract = scala.io.Source.fromFile("contracts/SideChain/UnlockComplete.es", "utf-8").getLines.mkString("\n")
  val sidechainUnlockCompleteErgoTree = compile(sidechainUnlockCompleteContract)
  val sidechainUnlockCompleteAddress = getAddressFromErgoTree(sidechainUnlockCompleteErgoTree)

}

object Printer extends App {
  import Constants._

  println(s"Sidechain state contract address: ${sidechainStateAddress}")
  println(s"Double unlock prevention contract address: ${doubleUnlockPreventionAddress}")
  println(s"Mainchain unlock contract address: ${transferUnlockAddress}")
  println(s"Sidechain unlock contract address: ${sidechainUnlockAddress}")
  println(s"Sidechain unlock-complete contract address: ${sidechainUnlockCompleteAddress}")
}