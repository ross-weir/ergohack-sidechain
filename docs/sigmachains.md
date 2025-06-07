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
