{
  // SideChain claiming sERG contract
  // tokens:
  //   #0 - NFT (not used now, may be used in future for refunds
  //   #1 - sERG tokens

  val doubleUnlockPreventionContractNFT = fromBase64("") // todo: inject

  val mainChainStateVarId = 125

  val mainchainUtxoState = getVar[AvlTree](mainChainStateVarId).get

  val mainchainBox = getVar[Box](0).get
  val mainchainUtxoProof = getVar[Coll[Byte]](1).get

  val selfOutput = OUTPUTS(0)
  val unlockState = SELF.R4[AvlTree].get
  val action = getVar[Byte](0).get

  // ensure the sERG amount to be claimed is pegged to mainchain box value
  val validTokenTransfer = (SELF.tokens(0)._2 - selfOutput.tokens(0)._2) == mainchainBox.value

  val amountToUnlock = mainchainBox.value

  val validTransition =
    selfOutput.value == SELF.value &&
    selfOutput.propositionBytes == SELF.propositionBytes &&
    selfOutput.tokens(1)._1 == SELF.tokens(1)._1 && selfOutput.tokens(1)._2 == SELF.tokens(1)._2 - amountToUnlock

  // ensure mainchain box provided exists in utxo set
  val properBox = mainchainUtxoState.get(mainchainBox.id, mainchainUtxoProof).get == mainchainBox.bytes

  // keep track of unlock request, prevent double unlock
  val doubleUnlockContractOutput = OUTPUTS(1)
  val doubleUnlockUpdated = doubleUnlockContractOutput.tokens(0)._1 == doubleUnlockPreventionContractNFT &&
                            doubleUnlockContractOutput.R5[Coll[Byte]].get == mainchainBox.id

  val completeOut = OUTPUTS(2)
  val completeOk = completeOut.propositionBytes == fromBase64("") && //todo: set to hash of UnlockComplete
                   completeOut.tokens(0)._1 == SELF.tokens(1)._1 &&
                   completeOut.tokens(0)._2 == amountToUnlock &&
                   completeOut.R4[Coll[Byte]].get == mainchainBox.id &&
                   completeOut.R5[Int].get <= HEIGHT &&
                   completeOut.R5[Int].get >= HEIGHT - 5

  sigmaProp(validTransition && properBox && doubleUnlockUpdated)

}
