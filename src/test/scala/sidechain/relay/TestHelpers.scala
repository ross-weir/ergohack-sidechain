package sidechain.relay

import scorex.crypto.hash.Sha256
import scorex.util.encode.Base16

trait TestHelpers {

  val relayNftId = Array.fill(32)(0.toByte)

  def hash(arg: Array[Byte]) = Sha256.hash(Sha256.hash(arg))

  def fromHex(hex: String) = Base16.decode(hex).get

}
