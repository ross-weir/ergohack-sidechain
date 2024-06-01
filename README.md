

This repository contains code and documentation for ErgoHack VII and ErgoHack VIII submissions.

ErgoHack VIII
=============

ErgoHack VIII project is exploring Bitcoin relay on Ergo.

There are two related contracts in the repository:

* [relay/BtcRelay.es](contracts/relay/BtcRelay.es) - processing submitted Bitcoin headers and building a commitment 
to a best Bitcoin chain which can be built out of them (thus operating as a Bitcoin SPV client), for client applications
* [relay/BtcTxCheck.es](contracts/relay/BtcTxCheck.es) - using relay contract to check if provided Bitcoin transaction 
is included into Bitcoin blockchain and has enough confirmations

There are also tests which can be found in [src/test/scala/sidechain/relay](src/test/scala/sidechain/relay) folder.

Slides: [docs/relays.pdf](docs/relays.pdf)

Video:

Use cases:

* Trustless Bitcoin hashrate derivatives on Ergo (i.e. Bitcoin miners can do contracts on Bitcoin hashrate on Ergo, to hedge against possible hashrate increase)   
* Making Ergo a smart layer for Bitcoin (Ergo contracts can do actions based on Bitcoin transactions and data written on Bitcoin, e.g. in OP_RETURN fields, that is how RGB/RGB++ etc operate)    
* Trustless bridging from Bitcoin to Ergo (but how to send back ?)
* Trustless cross-chain DEXes

ErgoHack VII
============

ErgoHack VII project was exploring implementation of sidechains on Ergo.

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