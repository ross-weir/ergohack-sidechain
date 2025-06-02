SigmaChains refactoring plan
============================

This document outlines changes needed for reusing possibilities of Ergo's contractual layer (Sigma), peer-to-peer 
and consensus-related functionalities (such as Autolykos) in different environments, such as:

* offchain contractual environments (such as offchain cash)
* merged-mined sidechains
* dedicated sigmachains (with trustless pegging)

Let's consider what's needed for these settings briefly before jumping on details:

+for all the environments, changes in following entities would be needed, in most cases: 

* Header, PreHeader types (SHeader, SPreHeader classes) which are specific to Ergo blockchain
* Box (SBox) type which is a basic unit of storage on the Ergo blockchain