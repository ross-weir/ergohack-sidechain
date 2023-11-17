{
    // Contract attached to a box which contains a digest of a tree containing previous unlocks, to prevent
    // double unlocks

    // tokens:
    //   #0 - contract NFT
    val selfTree = SELF.R4[AvlTree].get

    val selfOutput = OUTPUTS(1)

    val added = selfOutput.R5[Coll[Byte]].get

    val proof = getVar[Coll[Byte]](0).get

    val insertOps: Coll[(Coll[Byte], Int)] = Coll((added, added))
    val expectedTree = selfTree.insert(insertOps, proof).get

    val validTransition =
        selfOutput.value == SELF.value &&
        selfOutput.tokens == SELF.tokens &&
        selfOutput.propositionBytes == SELF.propositionBytes &&
        selfOutput.R4[AvlTree].get == expectedTree

    sigmaProp(validTransition)
}