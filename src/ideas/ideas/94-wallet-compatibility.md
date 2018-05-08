## Preamble

    Idea: #94
    Title: Wallet Compatibility
    Status: Draft
    Created: 2018-03-20
    Requires (*optional): <Idea number(s)>
    Replaces (*optional): <Idea number(s)>


## Summary
Make Status wallet compatible with the standards described in [BIP39](https://github.com/bitcoin/bips/blob/master/bip-0039.mediawiki), [BIP32](https://github.com/bitcoin/bips/blob/master/bip-0032.mediawiki), and [BIP44](https://github.com/bitcoin/bips/blob/master/bip-0044.mediawiki).

## Swarm Participants

- Lead Contributor: @pilu
- Testing & Evaluation: @asemiankevich
- Contributor: <!-- @username -->
- Contributor: <!-- @username -->
- PM: <!--- @username -->
- UX (if relevant): <!-- @username -->
<!-- - Contributor: @username -->

## Product Overview
As a user, I want to be able to create an account in Status, and open it in MyEtherWallet and MyCrypto.

To do this we should update the code that generates our extended keys to follow BIPs 39/32/44.

### Product Description

#### Steps

1. Update the salt used to generate the seed  from `status-im` to `mnemonic`, following `BIP39`.
2. Update the hmac key to `"Bitcoin seed"` following `BIP32`.
3. Check comments at https://github.com/status-im/status-go/blob/develop/extkeys/hdkey.go#L42.

After doing this, we should be able to import a Status account in MyEtherWallet and MyCrypto using the 12 mnemonic words and the passphrase.

Implemented with PR https://github.com/status-im/status-go/pull/783


### Requirements & Dependencies

**Pros**

* Ability to use a Status account in other wallets

**Cons**

* **IT WILL BE IMPOSSIBLE TO RECOVER ACCOUNTS GENERATED BEFORE THIS CHANGE, BECAUSE THE SEED GENERATED WITH THE MNEMONIC WORDS WILL BE DIFFERENT**


### Minimum Viable Product
Goal Date: 2018-04-06

Description: <!-- Description of Deliverables-->

## Dates
Goal Date: <!-- Date for evaluation in ISO 8601 (yyyy-mm-dd) format -->

Description: <!-- Description of Deliverables-->

Testing Days required: <!-- Days required at the end of development for testing -->

## Exit criteria

Being able to create an account in Status and open it in MyEtherWallet/MyCrypto

## Supporting Role Communication

Users of the alpha version must know that their current accounts won't be recoverable.

## Copyright
Copyright and related rights waived via [CC0](https://creativecommons.org/publicdomain/zero/1.0/).

