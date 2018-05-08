---
id: 090-branch-perf-stats
title: Make branch performance stats visible
status: draft
created: 2018-03-12
category: core
lead-contributor: PombeirP
contributors:
    -
    -
    -
exit-criteria: no
success-metrics: no
clear-roles: no
future-iteration: no
roles-needed:
    - DevOps
    - Go dev
okrs:
   - "[P4]: Objective: Implement continuous delivery"
   - "[P0]: KR: >80% automated test coverage"
---

## Preamble

    Idea: 090
    Title: Make branch performance stats visible
    Status: Draft
    Created: 2018-03-12

## Summary

Right now there is no visibility of how a PR impacts performance before it is merged to develop. It would be highly beneficial to have automated performance tests run and attach info to each Jenkins build, so that we can have historical records and compare current branch with parent branch.

## Swarm Participants

- Lead Contributor: PombeirP
- Contributor (Go): TBD
- Contributor (DevOps): TBD

## Product Overview

Right now, we run end-to-end tests but we don’t collect performance metrics on them. That’s throwing away a lot of valuable information that can inform discussions and decisions down the road. It’s important to have metrics on every PR branch and not just on develop, so that contributors have visibility into how their changes will potentially be affecting the stable branch.

### User Stories

Adding measurements like CPU, memory and network usage to each build would allow having a better idea of a branch performance quality, as well as the long term trend. This would serve to inform decisions, pull request reviews, and potentially have a single source of truth in terms of low-level performance tests.

### Requirements & Dependencies

Defining the best option in terms of CI/CD platform. Although this swarm doesn't technically require a particular platform, some lend themselves better to exposing and surfacing this type of information.

### Security and Privacy Implications

N/A

## Dates

> Description of deliverables at a given date, for example each Town Hall (default).
> Add more iterations as required.
>
> Evaluator accepts responsbility to checkin at Goal dates, forces discussion to
> continue implementation or recommend disband and post-mortem.
>
> Upcoming Town Halls this quarter:
> 2018-04-23, 2018-05-07, 2018-05-21, 2018-06-04, 2018-06-18

### Minimum Viable Product

> Mandatory, completes the Idea in the fastest route possible, can be hacky,
> needed to feel progress. See https://imgur.com/a/HVlw3

Goal Date:

Description:

A PoC would start with the lowest level (status-go) to guarantee that the building blocks are sound. This would add a report to the CI build artifacts containing the results for one representative use case (e.g. creating 2 accounts and sending 100 1:1 chat messages to each other sequentially), measuring only time taken and network traffic for the PoC. Report would be potentially consist of a CSV file and an HTML report file. The absolute requirement is that one of the files lends itself well to automated processing, including e.g. pasting to a Google Sheets file for further processing.

**NOTE**: TeamCity allows collecting custom statistical values from each build (just by outputting a value in a special format to the console) and then aggregating in a time-series graph:

![graph](https://user-images.githubusercontent.com/138074/37280336-c04ad1c6-25ed-11e8-8064-1dd4a01c4882.png)

### Iteration 1

Goal Date:

Description:

## Success Metrics

- More than 66% of developers surveyed think that they can rely on the CI system to easily compare the measured properties of each build between to branches.
- Builds with a lower quality (network consumption, etc) fail automatically.

## Exit criteria

- status-go and status-react builds are handled and success metrics are fulfilled for those two teams.

## Supporting Role Communication

N/A

## Copyright

Copyright and related rights waived
via [CC0](https://creativecommons.org/publicdomain/zero/1.0/).
