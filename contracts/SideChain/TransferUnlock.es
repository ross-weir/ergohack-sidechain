{
  // SideChain claiming sERG contract
  val mainchainUtxoState = getVar[AvlTree](125).get
  val mainchainUtxoProof = getVar[Coll[Byte]](124).get
  val selfOutput = OUTPUTS(0)
  val unlockState = SELF.R4[AvlTree].get
  val action = getVar[Byte](0).get

  // complete unlock
  if (action == 1) {
    val mainchainBox = getVar[Box](1).get

    // successor box unchanged
    val validTransition =
      selfOutput.value == SELF.value &&
      selfOutput.propositionBytes == SELF.propositionBytes

    // ensure enough confs
    val unlockStateProof = getVar[Coll[Byte]](2).get
    val lockStartHeight = unlockState.get(mainchainBox.id, unlockStateProof).get
    val enoughConfs = (HEIGHT - lockStartHeight) >= 50

    // ensure mainchain box provided still exists in utxo set
    val properBox = mainchainUtxoState.get(mainchainBox.id, mainchainUtxoProof).get == mainchainBox.bytes

    // ensure the sERG amount to be claimed is pegged to mainchain box value
    val validTokenTransfer = (SELF.tokens(0)._2 - selfOutput.tokens(0)._2) == mainchainBox.value

    // ensure unlock state hasn't changed
    val validUnlockState = unlockState.digest == selfOutput.R4[AvlTree].get.digest

    sigmaProp(validTransition && enoughConfs && properBox && validTokenTransfer && validUnlockState)
  } else {
    // start unlock
    val mainchainBox = getVar[Box](1).get

    val validTransition =
      selfOutput.value == SELF.value &&
      selfOutput.propositionBytes == SELF.propositionBytes &&
      outHeight <= HEIGHT &&
      outHeight >= HEIGHT - 5

    // ensure mainchain box provided exists in utxo set
    val properBox = mainchainUtxoState.get(mainchainBox.id, mainchainUtxoProof).get == mainchainBox.bytes

    // keep track of unlock request, prevent double unlock
    val unlockStateProof = getVar[Coll[Byte]](2).get
    val insertOps: Coll[(Coll[Byte], Int)] = Coll((mainchainBox.id, HEIGHT))
    val expectedUnlockState = unlockState.insert(insertOps, unlockStateProof).get
    val validUnlockState = expectedUnlockState.digest == selfOutput.R4[AvlTree].get.digest

    sigmaProp(validTransition && properBox && validUnlockState)
  }
}
