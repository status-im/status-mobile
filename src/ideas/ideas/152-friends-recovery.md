# Friends Recovery (Secret Multisig)

## Preamble

    Idea: 152-friends-recovery
    Title: Friends Recovery (Secret Multisig)
    Status: Draft
    Created: 2018-04-07
    Requires: 145-identity


## Summary

A multisig controller that store owners privately and requires a secret to sign a call, to be used as a recovery for Identity through trust of selected friends.

## Swarm Participants
- Lead Contributor: Ricardo Guilherme Schmidt
- Testing & Evaluation:
- Contributor: Richard Ramos
- PM: 
- UX: 

## Product Overview

A bad experience of cryptosystems is the lost or exposure of a private key, which can lead to irreversible situations. 
Some contracts, such as Identity, support a recovery address which can recover a lost key or compromised contract.
This product will provide a way of Status users selecting friends to recover this contracts, together with a built-in user interface for requesting to friends and signing recovery request from locked-out friends. 

### Product Description

User would be able to:
- Use semi-private data as secret, such as personal details or biometric data.
- Select privately a big amount of friends able to recover but only require a few of them

Locked out user would be able to:
- Request recovery to friends

User's friends would be able to:
- Only know they are recovery when recovery is completed
- Sign recovery call allowance for Friends Recovery contract to help locked out user (gas is paid by anyone/user)
- Call Friends Recovery from Identity call allowance to help locked out friends (gas is paid by allower)


### Requirements & Dependencies

- Idea 151-Identity is the first use case of this product, but Friends Recovery contract itself don't depend on Identity.

### Minimum Viable Product

Goal Date: 2018-07-01

Description: 
 
 - Users can use a semi-private secret answer as recovery lock
 - Users can use friends as allowance for Identity recovery
 - Users are notified when a recovery is requested for them
 - Users can help locked out friends to recover their Identity without paying gas

## Success Metrics

- Users are able to easly recover their Identity.

## Exit criteria

- Becomes an extension of Identity settings 

## Supporting Role Communication
<!-- Once Requirements and Goals are fleshed out, then it should be communicated to supporting organelles if required -->

## Copyright

Copyright and related rights waived via [CC0](https://creativecommons.org/publicdomain/zero/1.0/).
