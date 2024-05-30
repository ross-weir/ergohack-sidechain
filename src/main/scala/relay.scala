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


  // 93500
  val h3 = "01000000076379e2c0ec4a614ad1bf0ec716e6873f2c7abac604a08cc78e070000000000579a6bbcd07e9c3d622672ad20495d4485b5233395ab4081db7cab0fd2b577d2396cec4c2a8b091b031a7313"

  val bytes = Base16.decode(h3).get

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

  println((new BigInteger("2")).pow(255))

  // txid on testnet 44e504f5b7649d215be05ad9f09026dee95201244a3b218013c504a6a49a26ff
  // this tx has multiple inputs and outputs
  val txBytesHex = "01000000" +
    "02df80e3e6eba7dcd4650281d3c13f140dafbb823a7227a78eb6ee9f6cedd040011b0000006a473044022040f91c48f4011bf2e2edb6621bfa8fb802241de939cb86f1872c99c580ef0fe402204fc27388bc525e1b655b5f5b35f9d601d28602432dd5672f29e0a47f5b8bbb26012102c114f376c98d12a0540c3a81ab99bb1c5234245c05e8239d09f48229f9ebf011ffffffff" +
    "df80e3e6eba7dcd4650281d3c13f140dafbb823a7227a78eb6ee9f6cedd04001340000006b483045022100cf317c320d078c5b884c44e7488825dab5bcdf3f88c66314ac925770cd8773a7022033fde60d33cc2842ea73fce5d9cf4f8da6fadf414a75b7085efdcd300407f438012102605c23537b27b80157c770cd23e066cd11db3800d3066a38b9b592fc08ae9c70ffffffff" +
    "02c02b00000000000017a914b0b06365c482eb4eabe6e0630029fb8328ea098487e81c0000000000001976a914938da2b50fd6d8acdfa20e30df0e7d8092f0bc7588ac00000000"

  val txBytes = Base16.decode(txBytesHex)

  val versionBytes = bytes.take(4)
  val txVersion = Ints.fromByteArray(versionBytes.reverse)

  def parseCompactSizeUIntSize(byte: Byte): Long = {
    // 8 bit number
    if ((byte & 0xff) < 253) 1
    // 16 bit number
    else if ((byte & 0xff) == 253) 3
    // 32 bit number
    else if ((byte & 0xff) == 254) 5
    // 64 bit number
    else 9
  }

  println(parseCompactSizeUIntSize(Base16.decode("FD").get.head))

}
