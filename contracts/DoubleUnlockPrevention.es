{
    // tokens:
    //   #0 - contract NFT
    val selfTree = SELF.R4[AvlTree].get

    val selfOutput = OUTPUTS(1)

    val added = selfOutput.R5[Coll[Byte]].get

    val proof = getVar[Coll[Byte]](0).get

    val insertOps: Coll[(Coll[Byte], Int)] = Coll((added, added))
    val expectedTree = selfTree.insert(insertOps, proof).get

    val validTransition =
        SELF.value == selfOutput.value &&
        SELF.tokens == selfOutput.tokens &&
        SELF.propositionBytes == selfOutput.propositionBytes &&
        expectedTree == selfOutput.R4[AvlTree].get

    sigmaProp(validTransition)
}