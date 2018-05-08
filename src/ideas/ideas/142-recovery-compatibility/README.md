---
id: 142-recovery-compatibility
title: Wallet Recovery Compatibility
status: Active
created: 2018-04-04
category: core
lead-contributor: pilu
contributors:
    - pilu
    - antdanchenko
    - asemiankevich
    - flexsurfer
    - chadyj
    - jpbowen
exit-criteria: yes
success-metrics: yes
clear-roles: yes
future-iterations: yes
roles-needed:
---

## Meta

    Idea: #142
    Title: Wallet Recovery Compatibility
    Status: Draft
    Created: 2018-04-04
    Requires (*optional): #94 Wallet Compatibility
    Replaces (*optional): <Idea number(s)>

## Summary

A user should be able to recover accounts created in MyCrypto, MetaMask, Toshi, Cipher Browser, and other
mnemonic wallets, into Status.

## Swarm Participants

- Lead Contributor: @pilu
- Testing & Evaluation: @antdanchenko
- Testing & Evaluation: @asemiankevich
- Contributor: @flexsurfer
- Contributor: <!-- @username -->
- PM: @chadyj
- UX (if relevant): @jpbowen
<!-- - Contributor: @username -->

## Product Overview

The wallets named above create the private key with the mnemonic words but without password.

In those wallets, the password is only used to lock the account and encrypt the private key, and it's not needed to recover it.

In Status, the same password is used to generate the account and to recover it,
so a user cannot recover a wallet created with the 12 words but without password.

To be compatible with those wallets, we should create and recover wallets without password,
and use the passoword only to lock the account on the phone.

### Product Description

[need help from UX team]

### Requirements & Dependencies
<!-- Are there bugs or feature requests in other repositories that are part of this Idea? -->
<!-- There is no approval unless the idea requires to be reviewed by supporting organelles (Financial, Hiring, or Design). -->
<!-- The Swarm must develop a fully fleshed out Requirements document for the idea to proceed, to the satisfaction of participants. -->

Requires Idea #94 (Wallet Compatibility)
Impacts Idea #58-mainnet
Impacts Idea #80-onboarding

### Minimum Viable Product

Goal Date: 2018-04-20

Description: <!-- Description of Deliverables-->

## Dates
Goal Date: <!-- Date for evaluation in ISO 8601 (yyyy-mm-dd) format -->

Description: <!-- Description of Deliverables-->

Testing Days required: <!-- Days required at the end of development for testing -->

## Success Metrics
<!-- Assuming the idea ships, what would success look like? What are the most important metrics that you would move? -->

<!-- Example: Onboarding conversion rate. Target >30% full funnel -->

* 5k daily active users (OKR 2.1 of Q2)
* 20% of users send a transaction (OKR 2.4 of Q2)

## Exit criteria
<!-- Launch new onboarding UI flow -->

Accounts created with the mnemonic wallets named above can be recovered in Status.

## Supporting Role Communication
<!-- Once Requirements and Goals are fleshed out, then it should be communicated to supporting organelles if required -->

## Copyright
Copyright and related rights waived via [CC0](https://creativecommons.org/publicdomain/zero/1.0/).
