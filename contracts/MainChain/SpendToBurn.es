{
    // todo: inject token id

    def noTokens(b: Box) = !(b.tokens.exists{ (t:(Coll[Byte], Long)) => t._1 == fromBase64("")})
    sigmaProp(OUTPUTS.forall(noTokens))
}