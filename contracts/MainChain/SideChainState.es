{
  // SideChainState box holds the current state of the sidechain.
  // Miners spend the box to perform sidechain state transitions.
  // It is possible to spend the box at most once per block.
  //
  // TOKENS
  //  0: NFT identifying the box as the SideChain state box.
  //
  // REGISTERS
  //  R4: (Long)         h      - Height of the sidechain.
  //  R5: (Coll[Byte])  T_h     - Digest of state changes (transactions) done at h.
  //  R6: (Coll[Byte])  U_h     - UTXO set digest after processing changes.
  //  R7: (Coll[Byte])  chainDigest  - AVL tree where leaf has height as key and hash of corresponding states hash(h, T_h, U_h, chainDigest_{h-1}) as value.
  //  R8: (Int) - height of the main-chain when side-chain was updated last time

  // TODO: add receipt tokens, on each transition there will be additional output with current sidechain data and
  // TODO: one receipt token, then receipts can be used in unlock contract instead of this box (which is being updated every block)

  val successor = OUTPUTS(0)

  val validImmFields =
    SELF.propositionBytes == successor.propositionBytes && SELF.tokens(0) == successor.tokens(0)

  // TODO: validate chain digest transition? Or it would be too complicated in the presence of rollbacks ?
  val validStateTransition = successor.R4[Long].get == SELF.R4[Long].get + 1 &&
    successor.R5[Coll[Byte]].isDefined &&
    successor.R6[Coll[Byte]].isDefined &&
    successor.R7[Coll[Byte]].isDefined &&
    successor.R8[Int].get == HEIGHT &&
    HEIGHT > SELF.R8[Int].get // todo: if skipped then many sidechain blocks per mainchain one could be generated

  val minerProof = proveDlog(CONTEXT.preHeader.minerPk)

  minerProof && sigmaProp(validImmFields && validStateTransition)
}