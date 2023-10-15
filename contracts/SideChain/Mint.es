{
  // SideChain claiming sERG contract

  val mainchainUtxoState = getVar[AvlTree](125).get
  val mainchainUtxoProof = getVar[Coll[Byte]](126).get

  val successor = OUTPUTS(0)
  val transferUnlockBox = OUTPUTS(1)

  val mainchainBox = getVar[Box](1).get

  val validTransition =
      selfOutput.value == SELF.value &&
      selfOutput.propositionBytes == SELF.propositionBytes &&
      transferUnlockBox.R4[Int].get >= HEIGHT + 5 // 5 block unlock period

  // ensure mainchain box provided exists in utxo set
  val properBox = mainchainUtxoState.get(mainchainBox.id, mainchainUtxoProof).get == mainchainBox.bytes

  // ensure the sERG amount to be claimed is pegged to mainchain box value
  val validTokenTransfer = (SELF.tokens(0)._2 - selfOutput.tokens(0)._2) == mainchainBox.value

  sigmaProp(validTransition && properBox && validTokenTransfer)
}
