---
id: 58-mainnet
title: Use Mainnet as the default network
status: Limbo
created: 2017-12-08
category: core
contributors:
    - adambabik
    - annadanchenko
    - themue
    - rasom
    - chadyj
exit-criteria: yes
success-metrics: yes
clear-roles: yes
future-iterations: yes
roles-needed:
    - Clojure dev
okrs:
   - "[P1]: Objective: Beta is launched successfully"
   - "[P0]: KR: 5k daily active users"
---

## Preamble

    Idea: 58-mainnet-default
    Title: Use Mainnet as the default network
    Status: In progress
    Created: 2017-12-08


## Summary
Alpha builds of Status should default to Mainnet. We should be using Status in a real-world environment.

## Swarm Participants

- Lead Contributor: adambabik
- Testing & Evaluation: annadanchenko
- Contributor: themue
- Contributor: roman (supportive role; still in need of one Clojure dev)
- PM: chadyj

## Product Overview

Currently the Status alpha builds use testnet, but using Mainnet is a key milestone for the public launch of Status. There will be potentially major performance and security implications so the goal is to surface these issues early enough by testing the app in a real-world environment.

From a users perspective, Mainnet should be a default network as it is required by most popular DApps, and is needed for real transactions, so the full potential of Status can't be seen until we switch. The other networks will continue to be available, e.g. for developers and tests.

## Product Description

This is a default setting change, so there aren't any user facing options. The other networks are still visible.

Some users, such as DApp creators may want to test their app on testnet (or some other use case), so they will be able to toggle networks as can be done currently.

## Requirements & Dependancies

Security and performance should should be discussed and considered.

Also, several DApps should be tested.

## Minimum Viable Product

**This MVP does not mean that after it's finished we release the app with Mainnet enabled by default but defines prerequisites that should be done before we consider doing so.**

Goal Date: 2018-04-06

1. [ ] Development of required components is done. The work is tracked here: https://github.com/orgs/status-im/projects/15,
1. [ ] There is a Status App build with mainnet available,
1. [ ] All components have been tested by the Test Team,
1. [ ] Mainnet is default in dev/nightly builds.

## Audit iteration

Goal Date: 2018-04-23

1. [ ] Security audit is finished by an independent company,
1. [ ] The final report is available,
1. [ ] All reported security issues are evaluated and fixed.

## Status App Bug Bounty iteration (CANCELED)

**This iteration got canceled as we do not plan to announce Status App Bug Bounty for the time being.**

Goal Date: 2018-04-??

1. [ ] Status App Bug Bounty program is announced and will last for at least X weeks.

## Success and exit criteria

1. Mainnet is operational just like testnets and availalbe in dev/nightly builds,
1. The audit is completed and there are no low, medium, high or critical issues.

## Copyright
Copyright and related rights waived via [CC0](https://creativecommons.org/publicdomain/zero/1.0/).
