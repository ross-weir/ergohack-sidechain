The Pioneer Sidechain
===================

Motivation
----------

Proof-of-Work cryptocurrencies with minimal trust assumptions, such as Bitcoin and Ergo, can be seen as digital gold,
with decentralized mining network burning electricity in order to mint coins. And with decentralized derivatives 
which can coexist in the same ledger (in Ergo), we can talk about digital gold 2.0. 

And with sidechains having trustless exchange mechanisms with main-chain, we can talk about digital gold 3.0, a 
constellation of interconnected blockchains with merged-mining (incentivizing Ergo blokchain miners and also 
experimenting with new features, or serving needs of a particular application), or dedicated 
mining to utilize hardware of different classes (ASICs, CPUs, GPUs), to create more collateral within interconnected
multichain DeFi ecosystem.

After ETH merge, a lot of Ethash ASICs (specialized hardware used to mine Ethereum) is underutilized now (as Ethereum
Classic, Ethereum PoW etc are much smaller than ETH combined). 

And thus we propose **The Pioneer** - first Ergo sidechain, an ASIC-friendly Ethhash-based sidechain with Sigma 
contracts. Contracts critical for locking and growing liquidity (Spectrum DEX, Rosen bridge, hodl etc) will be available 
since the genesis block. The Pioneer will have 15 sec blocks.

Tokenomics
----------

PIO token

Storage rent as in Ergo.

Distribution: ???

Pre-built applications
----------------------

* **Spectrum DEX**
* **Rosen bridge** - Ergo deployment can be used to transfer from the Pioneer 
* **Sidechaining (with UI or just scripts for hackers?)**
* **HodlPIO** - (instead of vesting) ? 

Tech plan
---------

* Choose ASIC-friendly Ethash parameters and implement Ethash
* Consider avg block delay (15 sec likely)
* Consider DAA (from ETH or BCH with short epoch)
* Consider GHOST elements (papers criticizing ETH's GHOST and proposed fixes)
* Consider contract validation related blockchain parameters
* Modify Sigma layer to have PIO-specific context during PIO related validation
* Modify Ergo node code to have both Ergo and PIO in modular architecture
* Write tests for sidechaining contracts, do contracts for tokens etc
* Consider Market-driven emission
* Launch testnet, deploy apps there
* Launch the mainnet



Sidechaining Security
---------------------

When sidechaining data is delivered, sidechaining security is reduced to security to another chain, or can be stronger
if we require for sender's signature on unlock.
 
Then we need to consider mechanisms for delivering sidechaining data for both directions

* ERG data on PIO: 

There is no need to support Ergo blockchain in PIO consensus, what is needed, is to leave existing data types 
and types, including Ergo header with functions to check PoW. Then it is possible to do a trustless relay contract for 
Ergo blockchain which is verifying and then accepting a best header with valid PoW, storing a commitment to Ergo block headers 
history (as a digest of AVL+ tree), as well as current state of the Ergo blockchain, including UTXO set commitment. Then
this state can be used in sidechaining contracts.


* PIO data on ERG: 

As Ethash PoW check is not supported, building trustless relay contract is not so trivial in order to deliver PIO data 
on ERG.

We can consider then that sidechaining data can be updated by Ergo miners , like in merged-mined case from the 
sidechains whitepaper (todo: ref), but as this is not merged-mined case, why should Ergo miners do that? 

As a possible option, we can reward Ergo miners on PIO for providing PIO data on the Ergo blockchain. For that, assuming
that trustless ERG->PIO relay exists, we create an emission contract which is releasing a reward to Ergo miner 
providing a proof of PIO data updating by himself on the Ergo blockchain. This is a bit similar to BIP-301, but with 
better security due to constant rewards mechanism.

Please note that there is no need for Ergo miner to verify PIO blockchain fully, it is likely enough to be just an SPV
client of the PIO network.

Another option is to do Ethash verification via Bulletproofs or FastSwap like techiques, left for further research. 




Resources needed
----------------
