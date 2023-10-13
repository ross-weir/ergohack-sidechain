{
   // MainChain contract
   // registers:
   // R4 - sidechain's last data id (hash) during unlock start
   // R5 - approx mainchain HEIGHT at unlock moment

   val sideChainState: Box = CONTEXT.dataInputs(0)

   val unlockStartHash = SELF.R4[Coll[Byte]]

   if(unlockStartHash.isDefined) {
     val sidechainConfs = 50
     // todo: check that enough confs

     val committedHeight = getVar[Long](0).get
     val chT = getVar[Coll[Byte]](1).get
     val chU = getVar[Coll[Byte]](2).get
     val chChainDigest = getVar[Coll[Byte]](3).get

     val committedHeightHash = longToByteArray(committedHeight)

     val chHash = blake2b256(committedHeightHash ++ chT ++ chU ++ chChainDigest)
     val proof = getVar[Coll[Byte]](4).get
     val chainTree = getVar[AvlTree](5).get

     val properTree = chainTree.digest.slice(0,32) == sideChainState.R7[Coll[Byte]].get

     val treeContainsCommittedHash = chainTree.get(committedHeightHash, proof).get == chHash

     val mainchainHeightMet = SELF.R5[Int].get > HEIGHT
     val enoughSidechainConfs = (sideChainState.R4[Long].get - committedHeight) > 50
     sigmaProp(mainchainHeightMet && properTree && treeContainsCommittedHash && enoughSidechainConfs)
   } else {
     // starting unlock

     val sideChainStateHash = blake2b256(longToByteArray(sideChainState.R4[Long].get) ++
        sideChainState.R5[Coll[Byte]].get ++ sideChainState.R6[Coll[Byte]].get ++ sideChainState.R7[Coll[Byte]].get)

     val selfOutput = OUTPUTS(0)
     val outHeight = selfOutput.R5[Int].get

     val validTransition =
        selfOutput.value == SELF.value &&
        selfOutput.propositionBytes == SELF.propositionBytes &&
        selfOutput.tokens == SELF.tokens &&
        selfOutput.R4[Coll[Byte]].get == sideChainStateHash &&
        outHeight <= HEIGHT &&
        outHeight >= HEIGHT - 5

     sigmaProp(validTransition)
   }

}