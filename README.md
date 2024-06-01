

This repository contains code and documentation for ErgoHack VII and ErgoHack VIII submissions.

ErgoHack VIII
=============

ErgoHack VIII 

ErgoHack VII
============

project exploring implementation of sidechains on Ergo.

Whitepaper can be found in [docs/whitepaper/sidechain.pdf](docs/whitepaper/sidechain.pdf), video is available at 
[https://www.youtube.com/watch?v=G6xggrwA8ys](https://www.youtube.com/watch?v=G6xggrwA8ys).

Contracts are under [contracts/](contracts/) folder:

* [MainChain/SideChainState.es](contracts/MainChain/SideChainState.es) - contract which is containing sidechain data
on the mainchain in the merged-mining setting (so mainchain miners can update sidechain data).
* [MainChain/Unlock.es](contracts/MainChain/Unlock.es) - contract a user needs to send funds to get them on the sidechain.
Funds unlocked when burned on the sidechain and enough sidechain confirmations passed since then.
* [SideChain/Unlock.es](contracts/SideChain/Unlock.es) and [SideChain/UnlockComplete.es](contracts/SideChain/UnlockComplete.es) -
sidechain contracts which are unlocking sidechain ergs (sERG) when ERG is locked on the mainchain 
* [DoubleUnlockPrevention.es](contracts/DoubleUnlockPrevention.es) helper contract preventing double unlocks