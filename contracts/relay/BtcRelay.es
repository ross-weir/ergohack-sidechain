{
    val headersDigest = SELF.R4[AvlTree].get
    val tipHeight = SELF.R5[Int].get
    val tipHash = SELF.R6[Coll[Byte]].get

    // todo: implement reverse for 6.0

    def reverse4(bytes: Coll[Byte]): Coll[Byte] = {
        Coll(bytes(3), bytes(2), bytes(1), bytes(0))
    }

    def reverse32(bytes: Coll[Byte]): Coll[Byte] = {
        Coll(bytes(31), bytes(30), bytes(29), bytes(28), bytes(27), bytes(26), bytes(25), bytes(24),
             bytes(23), bytes(22), bytes(21), bytes(20), bytes(19), bytes(18), bytes(17), bytes(16),
             bytes(15), bytes(14), bytes(13), bytes(12), bytes(11), bytes(10), bytes(9), bytes(8),
             bytes(7), bytes(6), bytes(5), bytes(4), bytes(3), bytes(2), bytes(1), bytes(0))
    }

    val headerBytes = getVar[Coll[Byte]](1).get
    val prevBlockHashBytes = reverse32(headerBytes.slice(4, 36))
    val merkleRootBytes = reverse32(headerBytes.slice(36, 68))
    val timeBytes = reverse4(headerBytes.slice(68, 72))
    val nBitsBytes = reverse4(headerBytes.slice(72, 76))
    val nonceBytes = reverse4(headerBytes.slice(76, 80))

    // todo: check PoW, check PoW change

    val validParent = prevBlockHashBytes == tipHash

    // todo: check tipHash and tipHeight update

    sigmaProp(validParent)
}