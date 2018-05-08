# Gas Abstraction

## Preamble

    Idea: 150-gas-abstraction
    Title: Gas Abstraction
    Status: Draft
    Created: 2018-02-01
    Requires: 145-identity
    Replaces: 073-economic-abstraction

## Summary

Enable Status users to pay gas fee with any token that is valueable to society. 
Users would sign a message request to contracts which will use user's Identity balance of selected token to refund proportional used gas to a incentivezed ETH owner to include that message in a transction.

## Swarm Participants

- Lead Contributor: Ricardo Guilherme Schmidt
- Contributor: Richard Ramos
- Contributor: Iuri Matias
- UX (Mist): Alexandre Van de Sande 
- UX (Status): (help needed)


## Product Overview

A barrier for user adoption to blockchain through Status would be the need of holding two tokens for paying fees, ether gas for moving the [SNT](https://etherscan.io/address/0x744d70fdbe2ba4cf95131626614a1763df805b9e#readContract) fee from user balance to decentralized service provider. 

The product solve this by emulating a gas abstraction by adding "Gas Relayer Actor" in top of smart contracts as: [Identity](https://github.com/status-im/contracts/blob/73-economic-abstraction/contracts/identity/Identity.sol)[GasRelay](https://github.com/status-im/contracts/blob/73-economic-abstraction/contracts/identity/IdentityGasRelay.sol), MultiSigWallet, [SNTController](https://github.com/status-im/contracts/blob/73-economic-abstraction/contracts/status/SNTController.sol#L82) (updated from SNTPlaceHolder). 

- Allows SNT holder to broadcast ethereum signed messages to anyone with ETH to validate them in the GasRelay smart contracts, and offering a refund of gas used in a gas price set in SNT. 
- Whoever have ether could verify if the gas price worth the SNT gas price offered.
- This becomes a micro trade of ETH->SNT.
- Allow user to use any token they want (even ETH, or ETH stored in Identity).

### Product Description

Gas Relay node: 
- Status Destkop extension or an independent node can include messages by making automatic transactions calls to smart contracts to earn tokens being offered as gas price refund.
- Configuration for tokens accepted and types of contract willing to interact with.
Important as fundamental pivot actor. 

Identity Gas Relay adaptor: 
- include smart contract terms for accepting ethereum signed messages representing authorization to call by/for Identity owner offering a gas price refund for relayer.
- Identity UI should allow user to choose what token it wants to use as gas price.
Important for gas abstraction of call. 

SNTController Gas Relay adaptor: 
- include smart contract terms for accepting ethereum signed messages to call `IdentityGasRelayFactory` and for moving the tokens from there using SNT controller contract.
- a "wizard" UI would make the calls to create a Identity and send user SNT to Identity (so it can be used as ether gas).
Important for opt-in gas abstraction without ever holding ether.


### Requirements & Dependancies

Idea [145-identity](https://github.com/status-im/ideas/pull/145) is important to seamless and safe integration of gas abstraction for any type of call, so identity can become `msg.sender` for other contracts.

### Minimum Viable Product

Goal Date: 2018-05-15
Description:
- Users can use Identity paying gasPrice in any token.
- Users can create Identity using SNTController and paying with SNT.
- Users can earn tokens running a node that include other's messages

#### Identity Gas RelayIdentity UX should allow user to choose what token it wants to use as gas price.


Goal Date: 2018-04-20
Description: 
- Gas Relay Node that recieve whisper messages from Identity owners, 
- Identity User Interface with Gas Relayed option of calls

#### SNT Gas Relayer

Goal Date: 2018-04-27
Description: 
- Gas Relay Node implements watching messages for SNTController 
- User interface for creating Identity (pay gas in SNT)
- Moving SNT to other address from SNTController terms (paying gas in SNT)

#### UX Integration

Goal Date: 2018-05-15
Description:
- User can create Identity paying in SNT.
- User can select differnt tokens in gasPrice when using Identity.

## Success Metrics

Users are able to use Ethereum Virtual Machine and Status Network with only ever holding SNT.

## Supporting Role Communication

## Copyright
Copyright and related rights waived via [CC0](https://creativecommons.org/publicdomain/zero/1.0/).
