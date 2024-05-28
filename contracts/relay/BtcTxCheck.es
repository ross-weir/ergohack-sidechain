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

    val txBytes = getVar[Coll[Byte]](1).get
    val txId = doubleSha256(txBytes)

    // todo: check Merkle proof
    // todo: parse transaction

    val headerId = getVar[Coll[Byte]](2).get

    val headerProof = getVar[Coll[Byte]](3).get

    val merkleProof = getVar[Coll[Coll[Byte]]](4).get

    sigmaProp(properRelay)
}