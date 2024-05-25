import scorex.util.encode.Base16
import scorex.utils.{Ints, Longs}

import java.math.BigInteger

object relay extends App {

  /** Parse 4 bytes from the byte array (starting at the offset) as unsigned 32-bit integer in big endian format. */
  def readUint32BE(bytes: Array[Byte]): Long = ((bytes(0) & 0xffL) << 24) | ((bytes(1) & 0xffL) << 16) | ((bytes(2) & 0xffL) << 8) | (bytes(3) & 0xffL)


  private def decodeMPI(mpi: Array[Byte]): BigInteger = {

    val length: Int = readUint32BE(mpi).toInt
    val buf = new Array[Byte](length)
    System.arraycopy(mpi, 4, buf, 0, length)

    if (buf.length == 0) {
      BigInteger.ZERO
    } else {
      val isNegative: Boolean = (buf(0) & 0x80) == 0x80
      if (isNegative) buf(0) = (buf(0) & 0x7f).toByte
      val result: BigInteger = new BigInteger(buf)
      if (isNegative) {
        result.negate
      } else {
        result
      }
    }
  }

  def decodeCompactBits(compact: Long): BigInt = {
    val size: Int = (compact >> 24).toInt & 0xFF
    val bytes: Array[Byte] = new Array[Byte](4 + size)
    bytes(3) = size.toByte
    if (size >= 1) bytes(4) = ((compact >> 16) & 0xFF).toByte
    if (size >= 2) bytes(5) = ((compact >> 8) & 0xFF).toByte
    if (size >= 3) bytes(6) = (compact & 0xFF).toByte
    decodeMPI(bytes)
  }

  // height 566093
  val h1 = "00000020b45e33a345ad08ad2902cdd4101632fcbec009694b0c2500000000000000000016c99a795d8e0105d86f361341c7858d223fac261718bd608052822c5b4ae3cfd782815c505b2e17a56bb90b"

  // height 566092
  val h2 = "00000020a82ff9c62e69a6cbed277b7f2a9ac9da3c7133a59a6305000000000000000000f6cd5708a6ba38d8501502b5b4e5b93627e8dcc9bd13991894c6e04ade262aa99582815c505b2e17479a751b"
  val bytes = Base16.decode(h1).get


  val version = Ints.fromByteArray(bytes.take(4).reverse)

  val prevBlockHashBytes = bytes.slice(4, 36)
  val merkleRootBytes = bytes.slice(36, 68)
  val timeBytes = bytes.slice(68, 72).reverse
  val nBitsBytes = bytes.slice(72, 76).reverse
  val nonceBytes = bytes.slice(76, 80).reverse

  val pad = Array.fill(4)(0.toByte)

  val time = Longs.fromByteArray(pad ++ timeBytes)
  println(time)

  val nbits = Longs.fromByteArray(pad ++ nBitsBytes)
  println("nbits: " + nbits)
  val difficulty = decodeCompactBits(nbits)

  val nonce = Longs.fromByteArray(pad ++ nonceBytes)
  println("nonce: " + nonce)
  println(version)

  println("merkle root: " + Base16.encode(merkleRootBytes.reverse))

  println(difficulty)

  println(Base16.encode(prevBlockHashBytes.reverse))

}
