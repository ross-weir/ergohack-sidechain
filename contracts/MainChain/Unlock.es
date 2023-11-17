{
   // MainChain unlock contract.
   // ERGs are locked on main chain when locked in a box protected by this contract. The contract allows for
   // unlocking ERGs

   // registers:
   // R4 - sidechain's last data id (hash) during unlock start
   // R5 - approx mainchain HEIGHT at unlock moment

   val doubleUnlockPreventionContractNFT = fromBase64("") // todo: inject

   val sideChainState: Box = CONTEXT.dataInputs(0) // todo: check validity

   val unlockStartHash = SELF.R4[Coll[Byte]]

   //todo: add refund path
   if(unlockStartHash.isDefined) {
     val sidechainConfs = 50

     val committedHeight = getVar[Long](0).get
     val chT = getVar[Coll[Byte]](1).get
     val chU = getVar[Coll[Byte]](2).get
     val chChainDigest = getVar[Coll[Byte]](3).get

     val committedHeightBytes = longToByteArray(committedHeight)

     val chHash = blake2b256(committedHeightBytes ++ chT ++ chU ++ chChainDigest)
     val proof = getVar[Coll[Byte]](4).get
     val chainTree = getVar[AvlTree](5).get

     val properTree = chainTree.digest.slice(0,32) == sideChainState.R7[Coll[Byte]].get

     val treeContainsCommittedHash = chainTree.get(committedHeightBytes, proof).get == chHash

     val mainchainHeightMet = SELF.R5[Int].get > HEIGHT
     val enoughSidechainConfs = (sideChainState.R4[Long].get - committedHeight) > sidechainConfs
     sigmaProp(mainchainHeightMet && properTree && treeContainsCommittedHash && enoughSidechainConfs)
   } else {
     // starting unlock

     val sideChainStateHash = blake2b256 (
        longToByteArray(sideChainState.R4[Long].get) ++
        sideChainState.R5[Coll[Byte]].get ++
        sideChainState.R6[Coll[Byte]].get ++
        sideChainState.R7[Coll[Byte]].get
     )

     val selfOutput = OUTPUTS(0)
     val outHeight = selfOutput.R5[Int].get

     val validTransition =
        selfOutput.value == SELF.value &&
        selfOutput.propositionBytes == SELF.propositionBytes &&
        selfOutput.tokens == SELF.tokens &&
        selfOutput.R4[Coll[Byte]].get == sideChainStateHash &&
        outHeight <= HEIGHT &&
        outHeight >= HEIGHT - 5

     val stateTree = getVar[AvlTree](0).get
     val properStateTree = stateTree.digest.slice(0,32) == sideChainState.R6[Coll[Byte]].get

     val sidechainBox = getVar[Box](1).get
     val boxId = sidechainBox.id
     val boxBytes = sidechainBox.bytes

     val sidechainBoxProof = getVar[Coll[Byte]](2).get

     val properBox = stateTree.get(boxId, sidechainBoxProof).get == boxBytes

     val properScript = sidechainBox.propositionBytes == fromBase64("") // todo: false contract

     // check that sidechain box contains enough sERG sidechain tokens
     val properAmount = sidechainBox.tokens(0)._1 == fromBase64("") && sidechainBox.tokens(0)._2 <= SELF.value  // todo: sERG id

     val doubleUnlockContractOutput = OUTPUTS(1)
     val doubleUnlockUpdated = doubleUnlockContractOutput.tokens(0)._1 == doubleUnlockPreventionContractNFT &&
                                    doubleUnlockContractOutput.R5[Coll[Byte]].get == boxId

     sigmaProp(validTransition && properStateTree && properBox && properScript && properAmount && doubleUnlockUpdated)
   }

}