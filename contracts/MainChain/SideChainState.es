{
  // SideChainState box holds the current state of the sidechain.
  // Miners spend the box to perform sidechain state transitions.
  // It is possible to spend the box at most once per block.
  //
  // TOKENS
  //  0: NFT identifying the box as the SideChain state box.
  //
  //  TODO: add a counter (to R8?) which is increased by one on every update? then it can be used
  //  TODO: for hash escrow in contracts (see BIP-300 https://en.bitcoin.it/wiki/BIP_0300 )
  // REGISTERS
  //  R4: (Long)         h      - Height of the sidechain.
  //  R5: (Coll[Byte])  T_h     - Digest of state changes (transactions) done at h.
  //  R6: (Coll[Byte])  U_h     - UTXO set digest after processing changes.
  //  R7: (Coll[Byte])  chainDigest  - AVL tree where leaf has height as key and hash of corresponding states hash(h, T_h, U_h, chainDigest_{h-1}) as value.
  //  R8: (Int) - height of the main-chain when side-chain was updated last time

  // TODO: add receipt tokens, on each transition there will be additional output with current sidechain data and
  // TODO: one receipt token, then receipts can be used in unlock contract instead of this box
  // TODO: (which is being updated every block or with higher frequency even, then it would be hard to submit
  // TODO: transactions using sidechain data directly)

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

  // Proof of sidechain progress. Here we consider merged mining where (a subset of) Ergo miners is generating
  // sidechain blocks also. And so only a miner can submit new block data.
  // If a sidechain is small, only small subset of miners probably will mine it. Then only on small portion of blocks
  // sidechain progress can be recorded. Then instead of `CONTEXT.preHeader.minerPk`, a minerPk from on of last N blocks
  // can be used (upper limit for N is 10 in Ergo).
  //
  // For non merged mining setting, we still can use mainchain miners to submit sidechain data, a question then is
  // why miners should report sidechain data. They can be incentivized via something like BIP-301
  // (https://en.bitcoin.it/wiki/BIP_0301 ). See BIP-300 & BIP-301 criticism by Peter Todd and not only.
  // Another option is about building relays for other chains (and then abandon this contract or rework it into a
  // small proxy contract). For sha256 based chains PoW verification can be trivial, for complex PoW algorithms (
  // EthHash, EagleSong, kHeavyHash etc) optimistic (such as FairSwap) or Zero-Knowledge (such as BulletProofs or Halo2)
  // verifiable computing techniques.
  //
  // idea: we can still have miners posting sidechain data and receiving rewards from a contract on sidechain, which
  //       is much more clear option that BIP-301. For that, we need to add autolykos validation operation to the
  //       sidechain (and nbits conversion), and do a relay contract using it. With the relay it is possible to deliver
  //       mainchain data on the sidechain without trust to sidechain miners. Then we can have emission contract on the
  //       sidechain which is (using relay) paying to maichain miners for posting correct sidechain data on the
  //       mainchain. Thus part of sidechain emission is going to mainchain miners. Sidechain could be on PoS or other
  //       non-PoW consensus even.

  val minerProof = proveDlog(CONTEXT.preHeader.minerPk)

  minerProof && sigmaProp(validImmFields && validStateTransition)
}