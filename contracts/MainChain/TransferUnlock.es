{
   // MainChain contract
   // registers:
   // R4 - sidechain's last data id (hash) during unlock start
   // R5 - approx mainchain HEIGHT at unlock moment

   val sideChainState: Box = CONTEXT.dataInputs(0)

   val unlockStartHash = SELF.R4[Coll[Byte]]

   if(unlockStartHash.isDefined) {
     val heightMet = SELF.R5[Int].get > HEIGHT
     sigmaProp(heightMet)
   } else {
     // starting unlock

     val sideChainStateHash = blake2b256(longToByteArray(sideChainState.R4[Long].get) ++
        sideChainState.R5[Coll[Byte]].get ++ sideChainState.R6[Coll[Byte]].get ++ sideChainState.R7[Coll[Byte]].get)

     val selfOutput = OUTPUTS(0)
     val outHeight = selfOutput.R5[Int].get

     val validTransition =
        selfOutput.value == SELF.value &&
        selfOutput.propositionBytes == SELF.propositionBytes &&
        selfOutput.tokens == SELF.tokens &&
        selfOutput.R4[Coll[Byte]].get == sideChainStateHash &&
        outHeight <= HEIGHT &&
        outHeight >= HEIGHT - 5

     sigmaProp(validTransition)
   }

}