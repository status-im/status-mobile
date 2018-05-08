---
id: 83-energy-efficient
title: Energy Efficient Status
status: Active
created: 2018-02-19
category: core
contributors:
    - mandrigin
    - lukaszfryc
    - janherich
    - yenda
exit-criteria: yes
success-metrics: yes
clear-roles: yes
future-iterations: yes
okrs:
   - "[P4] Objective: Performance significantly improves"
   - "[P0] KR: Reduce power consumption to <120% of Telegram/Skype"
---

## Preamble

    Idea: 83
    Title: Energy Efficient Status
    Status: In Progress
    Created: 2018-02-19


## Summary
A specific issue about the energy efficiency of different parts of Status (`status-go`, `status-react`, `desktop` (when running on a laptop battery).


## Swarm Participants
- Lead Contributor: @mandrigin (~25h)
- Contributor (Clojure): @janerich
- Contributor (Clojure): @yenda
- Contributor (QA): @lukaszfryc (~10h/week)

## Product Overview
Energy consumption is a crucial part of the mobile experience, and even though it is related to performance, it is worth having a separate.
The end goal is:
- to provide a toolkit and guidelines to test energy efficiency of different parts of an app on different platforms;
- using this toolkit to fix the top battery drainers;
- notice regressions early by having tests in place.

### Goals

1. Create a BoK for Energy efficiency testing: https://github.com/orgs/status-im/projects/18
a. test apps are created for both Android and iOS
b. test cases are written and tools are created to measure the energy efficiency of both Status client (status-react+status-go) and tests apps (status-go exclusively).
c. tests are run on a regular basis
d. (stretch goal) tests are automated for both platforms

2. Fix obvious issues with energy consumption
https://github.com/orgs/status-im/projects/17


## Exit criteria
- [x] We have test cases and tools to check the energy efficiency of `status-go` and `status-react` independently;
- [x] We can notice regressions/improvements caused, e.g. by updating the version of `go-ethereum` or new features of Status;
- [ ] Top energy draining issues are identified and fixed.

## Success Metrics
KR: 
(1) foreground energy consumption is < 120% of the apps in a similar class (messengers) 
(2) background energy consumption is < 120% of the apps in a similar class (messengers)


## MVP(s)
MVP (development): Identify top issues with CPU hogging in Status for Android


## Supporting Role Communication

## Useful Links
https://github.com/dgryski/go-perfbook

## Copyright
Copyright and related rights waived via [CC0](https://creativecommons.org/publicdomain/zero/1.0/).
