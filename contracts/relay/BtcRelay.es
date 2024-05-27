{
    // header bytes in context var #1

    // id -> header
    val bestChainDigest = SELF.R4[AvlTree].get

    // id -> (header, chain digest, cumulative work)
    val allHeadersDigest = SELF.R5[AvlTree].get

    val tipHeight = SELF.R6[Int].get
    val tipHash = SELF.R7[Coll[Byte]].get
    val tipWork = SELF.R8[BigInt].get

    val selfOut = OUTPUTS(0)

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

    // calculate target to validate PoW & calculate work
    val pad = Coll[Byte](0.toByte, 0.toByte, 0.toByte, 0.toByte)
    val nbits = byteArrayToLong(pad ++ nBitsBytes)
    val target = Global.decodeNbits(nbits) // 6.0 method

    // block (header) id
    val id = reverse32(sha256(sha256(headerBytes)))

    val validPow = {
        val hit = byteArrayToBigInt(id)

        // <= according to https://bitcoin.stackexchange.com/a/105224
        hit <= target
    }

    // todo: check diff change every 2016 blocks

    // todo: forking

    val validTipUpdate = if(prevBlockHashBytes == tipHash) {
        val keyVal = (id, headerBytes)

        val proof = getVar[Coll[Byte]](2).get

        val nextTree: Option[AvlTree] = bestChainDigest.insert(Coll(keyVal), proof)
         // This will fail if the operation failed or the proof is incorrect due to calling .get on the Option
        val outputDigest: Coll[Byte] = nextTree.get.digest

        val outBestChainTree = selfOut.R4[AvlTree].get

        outBestChainTree.digest == outputDigest &&
        outBestChainTree.enabledOperations == bestChainDigest.enabledOperations &&
        selfOut.R6[Int].get == tipHeight + 1 &&
        selfOut.R7[Coll[Byte]].get == id
    } else {
        true
    }

    val allHeadersDbUpdate = {
        // 2^255 as signed big int is used
        val maxTarget = bigInt("57896044618658097711785492504343953926634992332820282019728792003956564819968")
        val work = maxTarget / ((target + 1) / 2)

        val parentData = getVar[Coll[Byte]](2).get
        val parentCumWork = byteArrayToBigInt(parentData.slice(0, 0)) // todo: bytes
        val cumWork = parentCumWork + work

        if (cumWork > tipWork) {
            true // todo: implement
        } else {
            true // todo: implement
        }
    }

    sigmaProp(validPow && validTipUpdate && allHeadersDbUpdate)
}