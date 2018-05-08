---
id: 154
title: Support web3.js library
status: Active
created: 2018-04-09
category: core
lead-contributor: flexsurfer
contributors:
    - flexsurfer
    - dmitryn
    - mandrigin
    - dshulyak
    - rachelhamlin
exit-criteria: yes
success-metrics: yes
clear-roles: yes
future-iterations: yes
roles-needed:
---

## Preamble

    Idea: 154
    Title: Support web3.js library
    Status: In Progress
    Created: 2018-04-09
    
## Summary
Ensure that any DApp that works in Metamask also works in Status by providing web3.js library support.

## Swarm Participants
- Lead Contributor: [@flexsurfer](https://github.com/flexsurfer)
- Contributor: [@dmitryn](https://github.com/dmitryn)
- Contributor: [@mandrigin](https://github.com/mandrigin)
- Contributor: [@dshulyak](https://github.com/dshulyak)
- Testing & Evaluation: 
- PM: [@rachelhamlin](https://github.com/rachelhamlin)
- UX: N/A

## Product Overview

A fundamental step in supporting the developer ecosystem is to ensure that any existing DApp loads and is operable in the Status browser.

Users:
1. Developers who want to develop a new HTML DApp for Ethereum from scratch
2. Developers of existing HTML DApps that work on desktop with Metamask
3. Users exploring DApps in Status

There are a number of bugs preventing this currently. In addition to resolving any known issues, we will provide support for the web3.js library.

### Product Description

Developers testing on Status should find that any mobile-optimized HTML DApp is compatible.

Users can load, log in to, and transact inside any DApp using the Status browser.

### Requirements & Dependencies

- [X] [#3174](https://github.com/status-im/status-react/issues/3174): Some Android devices can't find web3 library

- [X] [#2704](https://github.com/status-im/status-react/issues/2704): Personal sign message support (CryptoKitties blocker)

- [ ] [#784](https://github.com/status-im/status-go/issues/784): Contract handler not called after contract deployment

- [ ] [#3823](https://github.com/status-im/status-react/issues/3823): Sync calls are not supported in Status 

- [ ] [#2479](https://github.com/status-im/status-react/issues/2479): web3 filters don't work with Status chat bot

- [ ] [#3824](https://github.com/status-im/status-react/issues/3824): [Blockchain Cuties](https://blockchaincuties.co ) does not load inside Status

### Minimum Viable Product

Goal date: 

Description:
- Fix known issues. 
- Support web3.js library v.0.20.3

Testing days required:

## Exit Criteria
- Test cases defined for web3.js v.0.20.3
- Known issues closed

## Success Metrics
- All tests pass QA
- Every Selected DApp works (loads, logs in, transacts)

## Future Iterations
- Support [web3.js 1.0](https://web3js.readthedocs.io/en/1.0/)

## Copyright

Copyright and related rights waived via [CC0](https://creativecommons.org/publicdomain/zero/1.0/).

