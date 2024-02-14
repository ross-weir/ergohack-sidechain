ChainCash Sidechain
===================

In this article we describe a sidechain for ChainCash application. ChainCash details are not covered here, use
ChainCash whitepaper and other documents to get them.

Consensus
---------

ChainCash sidechain is sub-block based merged sidechain, which means that every Ergo main-chain sub-block may be a
sidechain block at the same time.

A ChainCash sidechain block is 


Workflow
--------

MainChain: send ERG (and other tokens) to 

Tokenomics
----------

We consider ChainCash SideChain Token (CST) to be used to pay fees. Motivation for using a custom token not ERG is as follows. 
ERG could be expensive, hard to obtain, or even unknown for its users, as ERG emission is going to miners, not users.

Thus we want to emit CST to users by using a following scheme:

* when a user send X ERG from mainchain to ChainCash sidechain, X CST is created on the sidechain as well
* additional CST can be got by CST staking or providing liquidity in cERG / GORT LP 
* when a note is created, at least N CST must be there
* sidechain fees are paid in CST, >= 1 CST per transaction
* when user sends X ERG from sidechain to mainchain, X CST must be  provided in the same transaction and burnt

System Applications and Transaction Types
-----------------------------------------

* cERG / GORT LP
* CST staking


