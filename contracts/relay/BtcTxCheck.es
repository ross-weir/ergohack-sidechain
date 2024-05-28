{
    // context vars:
    // #1 - tx bytes
    // #2 - header id
    // #3 - headerProof
    // #4 - Merkle proof

    //hardcoded constant
    val relayNftId = fromBase16("")
    val minConfs = 6 // minimum 6 confirmations required

    val relayDataInput = CONTEXT.dataInputs(0)

    val properRelay = relayDataInput.tokens(0)._1 == relayNftId

    def doubleSha256(bytes: Coll[Byte]) = sha256(sha256(bytes))

    def reverse32(bytes: Coll[Byte]): Coll[Byte] = {
        Coll(bytes(31), bytes(30), bytes(29), bytes(28), bytes(27), bytes(26), bytes(25), bytes(24),
             bytes(23), bytes(22), bytes(21), bytes(20), bytes(19), bytes(18), bytes(17), bytes(16),
             bytes(15), bytes(14), bytes(13), bytes(12), bytes(11), bytes(10), bytes(9), bytes(8),
             bytes(7), bytes(6), bytes(5), bytes(4), bytes(3), bytes(2), bytes(1), bytes(0))
    }

    val txBytes = getVar[Coll[Byte]](1).get
    val txId = doubleSha256(txBytes)

    // todo: check Merkle proof
    // todo: parse transaction

    val headerId = getVar[Coll[Byte]](2).get

    val headerProof = getVar[Coll[Byte]](3).get

    val merkleProof = getVar[Coll[Coll[Byte]]](4).get

    val bestChain = relayDataInput.R4[AvlTree].get

    val headerAndHeight = bestChain.get(headerId).get

    val height = byteArrayToLong(headerAndHeight.slice(80, 88))

    val tipHeight = relayDataInput.R6[Int].get

    val enoughConfs = (tipHeight - height) >= minConfs

    val merkleRootBytes = reverse32(headerBytes.slice(36, 68))

    sigmaProp(properRelay && enoughConfs)
}