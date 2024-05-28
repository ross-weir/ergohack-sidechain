{
    val relayNftId = fromBase16("")
    val relayDataInput = CONTEXT.dataInputs(0)

    val properRelay = relayDataInput.tokens(0)._1 == relayNftId
    sigmaProp(properRelay)
}