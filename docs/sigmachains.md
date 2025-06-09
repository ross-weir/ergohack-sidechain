SigmaChains refactoring plan
============================

This document outlines changes needed for reusing possibilities of Ergo's contractual layer (Sigma), peer-to-peer 
and consensus-related functionalities (such as Autolykos) in different environments, such as:

* offchain contractual environments (such as offchain cash)
* merged-mined sidechains
* dedicated sigmachains (with trustless pegging)

Let's consider what's needed for these settings briefly before jumping on details:

SigmaState 
==========

For all the environments, modifications in contractual layer (Sigma repository) are needed. So makes sense to start 
with them. Changes in following entities would be needed, in most cases: 

* Header, PreHeader types (SHeader, SPreHeader classes) which are specific to Ergo blockchain
* Box (SBox) type which is a basic unit of storage on the Ergo blockchain
* MinerPubkey and Height AST nodes
* sigma.Context class

along with projections into SigmaJS and compiler levels

It makes sense to tag all those context-dependent entities with a special (empty or almost empty) trait, to find them 
easily. Then create special packages in all the modules with context-dependent entities. Then document what and how to 
change in order to get programmable money for a new environment.

Ergo node
=========

Ergo node modifications are not needed for offchain contractual environments, as such apps are not using blockchain.

For merged mining setting, likely, current node API would be enough just. A merge mining client connected to a mining 
node would use `/mining/candidateWithTxs` API method to include sidechain block data into mainchain transactions (
as shown in [https://github.com/ross-weir/ergohack-sidechain/blob/main/docs/whitepaper/sidechain.pdf](https://github.com/ross-weir/ergohack-sidechain/blob/main/docs/whitepaper/sidechain.pdf), Section 2),
along with existing methods to track blocks and transactions (e.g. ones available in `/blocks`). Merged mining client 
logic can be implemented using any technology stack then.

A dedicated sigmachain can be built in Rust or Scala. In case of Rust based sigma-chain reference client implementation,
 as currently only contractual layer is translated into Rust (sigma-rust framework), networking layer implementation 
would be needed, it can be adopted from existing Proof-of-Work (and not only) cryptocurrencies implementations done 
in Rust. This could be a good option to start a Sigma-powered blockchain which is similar, in regards with consensus 
and/or block structure to ones having client implementation in Rust already. 

Another option is Scala. Here it is possible, as with Rust, to just use Sigma (Scala implementation) and adopt consensus 
/ block structure from some existing cryptocurrency with client done in Scala. But in most cases a dedicated Sigmachain 
implemented in Scala would derive from Ergo node code likely.


