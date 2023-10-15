{
    // todo: refund path

    val mainchainUtxoState = getVar[AvlTree](125).get

    val mainchainUtxoProof = getVar[Coll[Byte]](0).get
    val mainchainBox = SELF.R4[Box].get
    val lockStartHeight = SELF.R5[Int].get

    // ensure enough confs
    val enoughConfs = (HEIGHT - lockStartHeight) >= 50

    // ensure mainchain box provided still exists in utxo set
    val properBox = mainchainUtxoState.get(mainchainBox.id, mainchainUtxoProof).get == mainchainBox.bytes

    sigmaProp(enoughConfs && properBox)
}