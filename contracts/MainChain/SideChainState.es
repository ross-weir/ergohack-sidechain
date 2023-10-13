{
  val validSuccessor = {
    val successor = OUTPUTS(0)

    SELF.propositionBytes == successor.propositionBytes && SELF.tokens(0) == successor.tokens(0)
  }

  val minerProof = proveDlog(CONTEXT.preHeader.minerPk)

  minerProof && sigmaProp(validSuccessor)
}