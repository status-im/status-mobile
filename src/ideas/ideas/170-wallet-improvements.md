
---
id: 170-wallet-improvements
title: Wallet Improvements
status: Active
created: 2018-04-13
category: core
contributors:
    - goranjovic
    - asemiankevich
    - chadyj
    - denis-sharypin
    - hesterbruikman
    - jpbowen
exit-criteria: yes
success-metrics: yes
clear-roles: yes
future-iterations: yes
okrs:
   - "[P0]: Objective: Beta is launched successfully"
   - "[P3]: >20% of users send a transaction"
---

## Preamble

    Idea: 170
    Title: Wallet Improvements
    Status: In Progress
    Created: 2018-04-13
    
## Summary

Status Wallet does not fully support ERC20 tokens (requesting and transaction history not available)
and generally the transactions UX is different between Chat and Wallet. The intention of this swarm
is to wrap up those inconsistencies and study and improve wallet and transaction UX problems and
limitations to make the wallet experience great.

## Swarm Participants
- Lead Contributor: [@goranjovic](https://github.com/goranjovic)
- Contributor: 
- Testing & Evaluation: [@asemiankevich](https://github.com/asemiankevich)
- PM: [@chadyj](https://github.com/chadyj)
- UX: [@denis-sharypin](https://github.com/denis-sharypin)
- UX: [@hesterbruikman](https://github.com/hesterbruikman)

## Product Overview & Description

Add native support for all ERC20 token operations and rudimentary support for ERC721 tokens.

## Use Cases

 - As a user I want to see my ERC20 transactions in history of transactions (Wallet -> Transaction History).
 - As a user I want to distinguish my ERC20 transactions easily in History of transactions.
 - As a user I want to understand clearly whom i sent transaction to (ERC20, ETH, etc).
 - As a user I want to request erc20 tokens in 1x1 chat.
 - As a user I want to send erc20 tokens in 1x1 chat by responding to appropriate request.
 - As a user I want to send erc 20 tokens in a group chat.
 - As a user I want to send erc20 tokens in a group chat by responding to appropriate request.


### Requirements & Dependencies

- Possibly depends on extensions swarm for integrations with Chat 
- Related to #163 - support ERC721 tokens 


### Minimum Viable Product

Goal Date: 2018-05-12

Description: 

- Support for ERC20 transaction history in Wallet. 
- UX improvements derived from UXR sessions

### Iteration 1 

Goal Date: 2018-05-04

Description: 

- Wallet spring cleanup - triage, fix or remove reported bugs so far - https://github.com/orgs/status-im/projects/24
- Compile UX insights into design decisions for the next iterations 
- Update the subsequent iterations accordingly
 
### Iteration 2

Goal Date:

Description: 
- Support for ERC20 tokens in `/send` and `/request` chat command API. 
This iteration covers the prerequisites on the Command API side.
- UXR sessions and recommendations 
 
### Iteration 3

Goal Date:

Description: Integration of Send and Request operations between Wallet and Chat.

 
### Iteration 4

Goal Date: 

Description: ERC721 support in Wallet

Testing days required:

## Exit Criteria

- Display ERC20 token transfers in transaction history
- Send ERC20 tokens from chat
- Request ERC20 tokens from wallet and chat
- Unified Chat/Wallet transaction UX
- ERC721 support in Wallet

## Success Metrics

- All exit criteria met
- UXR retested with measurable improvements

## Copyright

Copyright and related rights waived via [CC0](https://creativecommons.org/publicdomain/zero/1.0/).

