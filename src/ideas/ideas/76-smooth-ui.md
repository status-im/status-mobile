<!-- Please Review https://docs.google.com/document/d/1CaFM2ZXGOKf05_LXMPJeNNy5qJOdAq91EF2Gn2QUBFI/edit# for more details -->
<!-- in PR the document should be named as`DEV#1-title.md` -->

## Preamble

    Idea: 76-smooth-ui
    Title: Make UI fast and smooth
    Status: Aborted
    Created: 2018-01-24
    Requires (*optional): 87-new-protocol


## Summary
<!-- "If you can't explain it simply, you don't understand it well enough." Provide a simplified and layman-accessible explanation of the Idea. -->
Status app should be fast enough to provide user smooth and nice experience - no lags and UI freezes.


## Swarm Participants
<!-- Each contributor pledges to the idea with their FOCUS value. (hours per week) -->
<!-- Here all roles in swarm are defined and filled, one of the contributors should responsibility of the Idea as Lead. -->

<!-- Testing/Evaluation support role is also mandatory to check in on specified Goal dates or earlier. -->

<!-- Lead Contributor is the Owner of the Idea. If required, they can get support from a PM, but should be responsible for end to end execution of the Idea. This includes ensuring appropriate resources are allocated, setting realistic timelines and milestones, and any post-launch metrics or bug fixes that are attributed to the Idea -->
<!-- A swarm requires at minimum 3 contributors and 1 evaluator/tester -->
<!-- 'Contributor' should be replaced with a descriptive role type. -->
- Lead Contributor: @dmitryn
- Testing & Evaluation: @Serhy
- Contributor: @rasom 
- Contributor: @janherich 
- Contributor: @yenda
- Contributor: @mandrigin 
- Contributor: @flexsurfer 
- UX: tba
<!-- - Contributor: @username -->

## Product Overview
<!-- A short (~200 word) description and motivation of the Idea. Without clear explanation the Idea should not proceed. Can include User Stories -->
<!-- Testing/Evaluation role accepts responsbility to checkin at Goal dates, -->
<!-- forces discussion to continue implementation or recommend disband and post-mortem. -->
App should be fast from user standpoint - switching screens, sending/receiving messages, dapp browsing. Every UI component should be responsible and provide almost immediate feedback to the user. For long-running operations spinner should be shown.

User stories to be resolved:
- https://github.com/status-im/status-react/issues/3095
- https://github.com/status-im/status-react/issues/3108
- https://github.com/status-im/status-react/issues/3397

<!-- ### Product Description -->
<!-- What functionality are you adding? What will this look like from a user perspective? Why is this important? -->

### Requirements & Dependencies
<!-- Are there bugs or feature requests in other repositories that are part of this Idea? -->
<!-- There is no approval unless the idea requires to be reviewed by supporting organelles (Financial, Hiring, or Design). -->
<!-- The Swarm must develop a fully fleshed out Requirements document for the idea to proceed, to the satisfaction of participants. -->
Somewhat depends on 87-new-protocol idea to be finished to validate perfomance user story about getting many messages from offline (app freezes while doing that) https://github.com/status-im/status-react/issues/3095

### Minimum Viable Product
<!-- Mandatory, completes the Idea in the fastest route possible, can be hacky, needed to feel progress. See https://imgur.com/a/HVlw3 -->
Goal Date: 2018-05-01

Description: Perf user stories have been resolved

<!-- ## Dates -->
<!-- Goal Date: 2018 -->

<!-- Description: https://github.com/status-im/status-react/issues/3095 -->

<!-- Days required at the end of development for testing -->
<!-- Testing Days required:  -->

## Success Metrics
<!-- Assuming the idea ships, what would success look like? What are the most important metrics that you would move? -->

App UI perfomance is good enough when using low-end and middle-end devices like iPhone 6 and Samsung Galaxy S4.
Perfomance testing to be made by QA team, then by real-world users from focus group (needs UXR help on that).

## Supporting Role Communication
<!-- Once Requirements and Goals are fleshed out, then it should be communicated to supporting organelles if required -->

## Copyright
Copyright and related rights waived via [CC0](https://creativecommons.org/publicdomain/zero/1.0/).
