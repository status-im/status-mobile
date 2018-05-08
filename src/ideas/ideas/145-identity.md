# Self-sovereign Identity

## Preamble

    Idea: 145-identity
    Title: Self-sovereign Identity
    Status: Draft 
    Created: 2018-05-04
    Requires: 152-friends-recovery ERC725 ERC735
    
## Summary

Creates a common user-owned credentials for authentication on smart contracts and for enhancing users security with multisig and optional recovery.

## Swarm Participants

- Lead Contributor: Ricardo Guilherme Schmidt 
- Contributor: Richard Ramos 
- UX (Etherum Foundation/Mist): Alexandre Van de Sande
- Testing & Evaluation:  (help needed)
- PM: (help needed)
- UX (Status):  (help needed)


## Product Overview

Self sovereign identity can disrupt single sign on services by using blockchain ledger, smart contracts and encryption techniques.

#### Traditional sign-in:

1. Verification of personal information is required for each single sign on service.
2. Services usually require trusting your personal information to multiple companies, which may collect it.
3. Some services require payment credentials to be stored in their database, which might be abused.
4. Recovering lost/compromised Identity requires an authorithy, which might be censored/abused.
5. Access to your Identity is available as long as service decides.

In traditional services you can get conforted by having an authority taking care of your data, which not only control your access to it, but also can read it (and sell it).

#### Self-sovereign identity sign-in:

1. Verified claims over Identity could be required by services.
2. Personal data is held by Identity owner
3. Payment system is done by payment authorizations made by Identity owner.
4. Recovering lost/compromised Identity is done by recovery smart contracts, such as 152-friends-recover .
5. Access to Identity is available as long as owner decides.

In self-sovering identity its owner have full resposability over its safety, but also have full control of their Identity.

#### Fundamental terms:

- ID Manager (owner) can define MultiSig public keys required for its management/actions;
- ID Manager can define a Recovery contract;
- ID Manager can define public keys for other; users knowing how to encrypt data for identity owner;
- ID Manager can accept claims;
- ID Manager can make claims.
- ID Actor can call other contracts.

#### Status Use Case:

From user perspective, instead of talking to obscure address/contracts, users  would able to confirm that a person is whom they expect to be interacting.
Mainly because they usually know by community network who is the Identity holder and can safely assume is the right person.

This is important for chat system, where Identities provide the right key for others messaging, and Identity owners can always update this keys as they want.

Recovery contract, if safely defined, will garantee that Identity owner is able to get back their Identity from loss or theft, therefore no need to create a new one and ask friends to update their contact list with your new Identity.

#### External references: 

- [Fabian Vogelsteller - ERC: Identity - Ethereum London](https://www.youtube.com/watch?v=jv3BmGGFP7c)
- [DEVCON3: ERC Identity](https://www.youtube.com/watch?v=pkwYVagytuA)
- [Jolocom: enabling blockchain agnostic self- sovereign identity - Kai Wagner](https://www.youtube.com/watch?v=vkEdDj5HtVs)
- [Mist: UX insight - Alexande Van de Sande](https://gist.github.com/alexvandesande/434f143fc6d08cb4388479a3d9f527a9)
- [DEVCON1: Digital Identity - Christian Lundkvist](https://www.youtube.com/watch?v=QpaTOvAhLR4)
- [uPort – Usable key management & identity - Rouven Heck and Dr. Christian Lundkvist](https://www.youtube.com/watch?v=qRevDM9D8WE)
- [ConsenSys and uPort: Powering Decentralized Identity](https://www.youtube.com/watch?v=VXAZdBtN3N0)
- [Self-sovereign BigchainDB data injection in smart contracts through the Jolocom Identity Gateway](https://www.youtube.com/watch?v=8K-BDlsx8KQ)
- [IDbox – Cost efficient device for self-sovereign identity](https://www.youtube.com/watch?v=h1Oz3oEtZxE)

### Product Description

- Smart contracts: See [GDocs Identity Terms Intent](https://docs.google.com/document/d/1K8tFjGneScuKiudpD3HfSpAd4eOe8012jn0Q3iDxEV0/edit#) and [Identity Readme](https://github.com/status-im/contracts/blob/develop/Identity.md)
- Basic contracts demo: Helps engaging developers and bug hunting. 
- Identity Sign-in: Actions for user creating or recovering their wallets.
- Identity Usage: User may select their Identity to make tansactions in behalf of.
- Identity Management: User may setup keys & recovery options for its identity, confirm claims and even update the contract code.

### Requirements & Dependencies

- [ERC#725](https://github.com/ethereum/EIPs/issues/725) define identity management interface used by dapps to read Identity and manage it. Important for cross-compatibility with other dapps. 

- [ERC#735](https://github.com/ethereum/EIPs/issues/735) define identity claims interface used by DApps to read claims. Important for cross-compatibility with other dapps.

- Idea [152-friends-recovery](https://github.com/status-im/ideas/pull/152) define a recovery option. Important to provide a first decentralized recovery contract option that uses friends to recover.

## Dates

### Proof-of-concept

Goal Date: 2018-05-15

Description: 
- Working contracts system 
- Basic UI for direct interaction with blockchain 

### Minimal Viable Product

Goal Date:  2018-06-01

Description:
- Status Wallet enables creation, association or recovery of Identity 
- Status Wallet enables transactions to be executed by Identity
- Identity management (basic)

Testing Days required: <!-- Days required at the end of development for testing -->

### Seamless Status Integration

Goal Date: 2018-06-15

- Identity Sign-in:
    - Create new;
    - Reconfigure existing;
- Identity Usage:
    - Interact with other contracts in behalf of Identity;
    - Sign claims about others Identity.
- Identity Management 
    - Keys: Add/Remove/Update 
    - Accept Claims
    - Update of Kernel
    - Select a recovery contract
    
## Success Metrics

Status Users opt-in using an Identity to enhance interaction with decetralize systems (such as Status itself). 
Developers start using Status Identity implementation as base.
New users adopt Status because Identity support, which will be required for many dapps in future.

## Exit criteria

Fully integrated on Status App
    - Users can create new Indeitity
    - Users can manage their Identities
    - Users can use (action) Identities to call contracts

## Supporting Role Communication
<!-- Once Requirements and Goals are fleshed out, then it should be communicated to supporting organelles if required -->

## Copyright
Copyright and related rights waived via [CC0](https://creativecommons.org/publicdomain/zero/1.0/).
