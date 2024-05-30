{
    // context vars:
    // #1 - tx bytes
    // #2 - header id
    // #3 - headerProof
    // #4 - Merkle proof

    // registers:
    // no registers used

    //hardcoded constant

    // todo: change before deployment
    val relayNftId = fromBase16("0000000000000000000000000000000000000000000000000000000000000000")
    val minConfs = 6 // minimum 6 confirmations required

    val relayDataInput = CONTEXT.dataInputs(0)

    val properRelay = relayDataInput.tokens(0)._1 == relayNftId

    def doubleSha256(bytes: Coll[Byte]) = sha256(sha256(bytes))

    val txBytes = getVar[Coll[Byte]](1).get
    val txId = doubleSha256(txBytes)

    // todo: parse transaction

    val headerId = getVar[Coll[Byte]](2).get

    val headerProof = getVar[Coll[Byte]](3).get

    val bestChain = relayDataInput.R4[AvlTree].get

    val headerAndHeight = bestChain.get(headerId, headerProof).get

    val height = byteArrayToLong(headerAndHeight.slice(80, 88))

    val tipHeight = relayDataInput.R6[Int].get

    val enoughConfs = (tipHeight - height) >= minConfs

    val merkleRootBytes = headerAndHeight.slice(36, 68)

    val merkleProof = getVar[Coll[Coll[Byte]]](4).get

    def computeLevel(prevHash: Coll[Byte], proofElem: Coll[Byte]) = {
        val elemHash = proofElem.slice(1,33)
        if(proofElem(0) == 0){
          doubleSha256(elemHash ++ prevHash)
        } else {
          doubleSha256(prevHash ++ elemHash)
        }
    }

    val computedMerkleRoot = merkleProof.fold(txId, computeLevel)

    val properProof = computedMerkleRoot == merkleRootBytes

    // todo: add NFT and preservation checks ?

    sigmaProp(properRelay && enoughConfs && properProof)
}