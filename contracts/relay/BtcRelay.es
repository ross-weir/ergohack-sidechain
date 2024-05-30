{
    // registers:
    // R4 - best headers-chain tree
    // R5 - all headers tree
    // R6 - tip height
    // R7 - tip block id
    // R8 - tip cumulative work
    //
    // context vars:
    // #1 - new header bytes
    // #2 - best chain tree insert proof
    // #3 - parent header lookup proof for all headers db
    // #4 - parent header's chain digest

    // id -> header (80 bytes) + height (8 bytes)
    val bestChainDigest = SELF.R4[AvlTree].get

    // id -> header (80 bytes) + height (8 bytes) + chain digest (33 bytes) + cumulative work
    // chain digest here is constructed in the same way as best header chain digest
    val allHeadersDigest = SELF.R5[AvlTree].get

    val tipHeight = SELF.R6[Int].get
    val tipHash = SELF.R7[Coll[Byte]].get
    val tipWork = SELF.R8[BigInt].get

    val selfOut = OUTPUTS(0)

    // todo: implement reverse for 6.0

    def reverse4(bytes: Coll[Byte]): Coll[Byte] = {
        Coll(bytes(3), bytes(2), bytes(1), bytes(0))
    }

    def doubleSha256(bytes: Coll[Byte]) = sha256(sha256(bytes))

    def headerId(headerBytes: Coll[Byte]) = doubleSha256(headerBytes)

    val headerBytes = getVar[Coll[Byte]](1).get
    val prevBlockId = headerBytes.slice(4, 36)
    // val merkleRootBytes = headerBytes.slice(36, 68)
    val timeBytes = reverse4(headerBytes.slice(68, 72))
    val nBitsBytes = reverse4(headerBytes.slice(72, 76))
    val nonceBytes = reverse4(headerBytes.slice(76, 80))

    // calculate target to validate PoW & calculate work
    val pad = Coll[Byte](0.toByte, 0.toByte, 0.toByte, 0.toByte)
    val nbits = byteArrayToLong(pad ++ nBitsBytes)
    val target = Global.decodeNbits(nbits) // 6.0 method

    // block (header) id
    val id = headerId(headerBytes)

    val validPow = {
        val hit = byteArrayToBigInt(id)

        // <= according to https://bitcoin.stackexchange.com/a/105224
        hit <= target
    }

    // 2^255 as signed big int is used
    val maxTarget = bigInt("57896044618658097711785492504343953926634992332820282019728792003956564819968")
    val work = maxTarget / ((target + 1) / 2)

    // todo: check diff change every 2016 blocks

    // best chain header record
    val headerRow = (id, headerBytes ++ longToByteArray(tipHeight.toLong))

    val validTipUpdate = if(prevBlockId == tipHash) {

        val proof = getVar[Coll[Byte]](2).get

        val nextTree: Option[AvlTree] = bestChainDigest.insert(Coll(headerRow), proof)
         // This will fail if the operation failed or the proof is incorrect due to calling .get on the Option
        val outputDigest: Coll[Byte] = nextTree.get.digest

        val outBestChainTree = selfOut.R4[AvlTree].get

        val cumWork = tipWork + work

        outBestChainTree.digest == outputDigest &&
        outBestChainTree.enabledOperations == bestChainDigest.enabledOperations &&
        selfOut.R6[Int].get == tipHeight + 1 &&
        selfOut.R7[Coll[Byte]].get == id &&
        selfOut.R8[BigInt].get == cumWork
    } else {
        true
    }

    val allHeadersDbUpdate = {

        val parentProof = getVar[Coll[Byte]](3).get
        val parentData = allHeadersDigest.get(prevBlockId, parentProof).get

        // todo: should we store first 80 bytes (header)? they are not used
        val parentChainDigest = parentData.slice(88, 121)

        // todo: with AVL tree constructing options in 6.0, this getVar can be eliminated
        val parentChainProvided = getVar[AvlTree](4).get
        val parentCumWork = byteArrayToBigInt(parentData.slice(121, parentData.size))
        val cumWork = parentCumWork + work

        val parentChainUpdateProof = getVar[Coll[Byte]](5).get
        val updDigest = parentChainProvided.insert(Coll(headerRow), parentChainUpdateProof).get.digest

        val allHeadersInsertProof = getVar[Coll[Byte]](6).get
        val cumWorkProvided = getVar[Coll[Byte]](7).get // todo: could be eliminated with BigInt serialization

        val keyVal = (id, (headerBytes ++ updDigest ++ cumWorkProvided))
        val allHeadersDbUpdated = allHeadersDigest.insert(Coll(keyVal), allHeadersInsertProof).get
        val newAllHeadersDigestProvided = selfOut.R5[AvlTree].get

        val allHeadersUpdateOk = parentChainProvided.digest == parentChainDigest &&
                                    cumWork == byteArrayToBigInt(cumWorkProvided) &&
                                    allHeadersDbUpdated == newAllHeadersDigestProvided

        if (cumWork > tipWork && prevBlockId != tipHash) {
            // switch to better chain

            val outBestChainTree = selfOut.R4[AvlTree].get
            val parentHeight = byteArrayToLong(parentData.slice(80, 88))

            val switchOk = outBestChainTree.digest == updDigest &&
                            outBestChainTree.enabledOperations == bestChainDigest.enabledOperations &&
                            selfOut.R6[Int].get == parentHeight + 1 &&
                            selfOut.R7[Coll[Byte]].get == id &&
                            selfOut.R8[BigInt].get == cumWork

            allHeadersUpdateOk && switchOk
        } else {
            // add header along with metadata to all-headers tree
            allHeadersUpdateOk
        }
    }

    val selfPreservation = selfOut.value >= SELF.value && selfOut.tokens == SELF.tokens

    sigmaProp(validPow && selfPreservation && validTipUpdate && allHeadersDbUpdate)
}