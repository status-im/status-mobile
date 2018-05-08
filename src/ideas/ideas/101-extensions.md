---
id: 101-extensions
title: Extensions
status: Active
created: 2018-03-26
category: core
lead-contributor: jeluard
contributors:
    - asemiankevich
    - janherich
    - cammellos
    - andytudhope
    - denis-sharypin
exit-criteria: yes
success-metrics: yes
clear-roles: yes
future-iterations: yes
roles-needed:
---

## Meta

    Idea: 101
    Title: Status extensions
    Status: In Progress
    Created: 2018-03-26

## Summary

A set of extension points so that external developers can enhance status

## Swarm Participants

- Lead Contributor: @jeluard
- Testing & Evaluation: @asemiankevich
- Contributor: @cammellos, @janherich
- PM: @andytudhope
- UX: @denis-sharypin

## Product overview

As an ecosystem status needs to offer extension capacities so that external developers can develop on top of it.
This swarm will review the current state of our extensions, specify a updated mechanism based on our current knowledge and eventually implement it.

### Product Description

3rd party developers will have access to API and documentation allowing them to extend status via various hooks using a simple API.
There won't be any distinction between those extensions and status itself (it will feel native).

### Requirements & Dependencies

None

### Research

* document existing working features offered by status [API](https://docs.status.im/)
* list (potential) new types of extensions
* prepare new workflow / mockups (special care for chat extensions)
* prepare draft specifications detailing technical solution to be discussed with team (development, distribution, incentivization)
* detail how to integrate with status API / screens

During definitions of specifications a number of potential use cases must be considered, namely:

* retrofit existing chat commands
* ethereum standards (721, ..)
* video sharing technologies ([livepeer](https://livepeer.org/))
* token transfers (https://raiden.network/)
* https://ethlend.io/en/, https://trustlines.network/, ...
* extensions mechanisms provided by other chat applications

First status hackathon is also a good starting point. [Feedback](https://github.com/status-im/status-react/wiki/Hackathon-Feedback) has been shared and [winners](https://blog.status.im/announcing-winners-of-the-status-global-hackathon-a44fb54e98f7) give us a good hint on extensions potential.

Note that idea #96 might be the first guinea pig of extensions.

*Started*: 2018-26-03
*Goal Date*: 3 weeks after beginning
*Completed*: 2018-20-04

## Dates

### Pre-MVP

Validate technical feasibility.

* general mechanism (UI, conditionals, queries, events)
* HTML based
* stored in ipfs (via gateway)
* entry point: QR code scanner
* no concept of extension points
* no custom code
* extension are activated when loaded and cannot be deactivated
* simplified app-db

*Started* 2018-04-24
*Goal Date* 2018-05-09

### MVP

Pre-MVP integrated in Status.

*Duration* 1 week

### Iteration 1

Migrate old commands to extensions.

* implement extension points logic and proposed extension points
* isolated data per extension
* migrate existing commands send / request (message-type, update message)
* detail plan for persistent extensions

*Duration* 2 weeks

### Iteration 2

Add custom code support.

* implement security mechanism
* remote code loading
* JavaScript API

*Duration* 2 weeks

### Iteration 3

Persistent extensions.

* implement data persistency
* implement extension lifecycle
* extensions can be deactivated
* add extension registry UI

*Duration* 2 weeks

### Iteration 4

Support dependencies.

* add versioning support
* extensions can depend on others extensions
* implement WhoPays

*Duration* 2 weeks

## Exit criteria

* current API state is documented
* mockups and documentation of proposed changes of extensions are available and have been discused by team
* extensions API is implemented and documented
* an extensive example is available

## Success Metrics

* 2 internal extensions shipped 
* 3 external extensions shipped
* positive feedback (API, documentation) from 3 external ethereum projects
* quality of documentation recognized: few support request related to extensions

Copyright and related rights waived via [CC0](https://creativecommons.org/publicdomain/zero/1.0/).
