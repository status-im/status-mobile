---
id: 140-sob-improve-onboarding
title: OpenBounty Improved Onboarding
status: Limbo
created: 2018-04-04
category: openbounty
contributors:
    - martinklepsch
    - Churik
    - EugeOrtiz
    - arash009
    - rcullito
exit-criteria: yes
success-metrics: yes
clear-roles: yes
future-iterations: yes
---

## Preamble

    Idea: #XX-sob-improve-onboarding
    Title: OpenBounty Improved Onboarding
    Status: In progress
    Created: 2018-04-04

## Summary

Currently users wanting to publish a bounty need to go through various manual steps, including us whitelisting them for using the platform. This step forces users to wait for us and might make them drop out of the process even if they were committed to create a bounty.

This swarm aims to remove the need for whitelisting users and revamp the onboarding flow at the same time.

## Swarm Participants

- Lead Contributor: @martinklepsch (10hr for MVP, 30hr+ from MVP onwards)
- Testing & Evaluation: @Churik
- UX Contributor: @EugeOrtiz
- Dev Contributor: @siphuel or @rcullito
- Contracts Contributor: @3esmit (need not fully clear)
- Product Manager Contributor: @arash009

## Product Description

Currently all users wanting to issue a bounty need to go through a whitelisting process. This is required because we currently pay the gas for deploying the bounty contract. In order to make it easier for users to get started we should remove this whitelisting requirement, allowing users to onboard at their own speed.

There are multiple ways the need for whitelisting could be removed:

- Allow (or force) users to pay gas for contract deployment themselves
- Pay gas for new users but implement some rate-limiting or similar guards to protect from bad actors

These and other alternatives will be evaluated as part of this swarm, whatever decision is taken will be documented in a [Decision Record](https://github.com/status-im/open-bounty/blob/develop/doc/decisions/0001-record-decisions.md). 

## Iterations

### Iteration 1 — Concept/Design (MVP) `2018-04-20`

- Design (wireframes) onboarding and bounty-issuing flow that properly accounts for the removed need to get whitelisted and guides users through the creation of their first bounty.
- Decide on the best way to eliminate whitelisting requirement for immediate future.

**Artifacts:**  

- [ ] mockup of onboarding flow for bounty issuers
- [ ] mockup of onboarding flow for bounty hunters
- [ ] Updated welcome email text for both
- [ ] Decision Record for removal of whitelisting requirement

### Iteration 2 — Implementation `2018-05-10`

Implement the UI for the new flows and make it functional. Once `122-sob-metrics` has laid
the necessary groundwork, start integrating some metrics using the tools defined by that
swarm.

**Artifacts:** 

- [ ] Functioning onboarding UI and bounty issuing flow that does not require getting whitelisted.
- [ ] Emails that are sent to new users are updated

### Iteration 3 — Cleanup `2018-05-30`

Devise steps to fully migrate to new onboarding flow, refactor relevant code, delete code, clean up UI.

## Exit Criteria
- Whitelisting barrier has been removed
- Any user can sign up to SOB and publish a bounty on a repository they own
- The onboarding experience (UI, Emails, etc) has been revised to match the new process

## Success Metrics
We don't have proper metrics for Open Bounty yet but this should affect the following:

- Value transacted through SOB
- Increase number of bounty issuers
- Increase number of bounties issued
- Overall conversion/funnel improvements

## Appendix / Notes

### Token Whitelisting

Swarm Compensation Experimentse previously had issues with tokens being added to bounty contracts that have no value/are spam. While not explicitly the goal of this swarm this would be worth solving as well if it turns out to be low-hanging fruit.

### `repository.user_id` column

Currently when users sign up and install the OpenBounty app into their repo their GitHub `user_id` is stored as `repository.user_id`. This ID is later used to look up which address should be able to confirm a payout (specified at bounty deployment).

This is later often referred to as `repo_owner`. This concept of a `repo_owner` is insufficient as it does not reflect the realities of GitHub. Multiple people should be able to publish/pay out bounties on the same repo — as they can add bounty labels.

