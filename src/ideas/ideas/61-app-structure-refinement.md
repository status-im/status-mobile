## Preamble

    Idea: #61-app-structure
    Title: App structure refinement
    Status: In Progress
    Created: 2017-12-14


## Summary

For the beta launch there needs to be some app structure refinements and UI tweaks in order to simplify the experience and orientate users on clear actions and expectations. 

## Swarm Participants

- Lead Contributor: @jeluard
- Testing & Evaluation: @asemiankevich , @Serhy
- Contributor: @flexsurfer
- Contributor: @goranjovic
- PM: @chadyj
- UX: @andmironov

## Product Overview

The current alpha build is an expression of many ideas at various stages of completion that showcases the longer term potential of Status. However, not all features are optimized and polished with the user in mind. An app structure update will re-focus the app around the core flows and provide a solid platform for future updates.

The new app structure will focus the user around DApps, Chat, Wallet, and profile/settings. From a users perspective this presents a clear, focused, and unambiguous explanation of what Status does without the need to hunt around in the app.

Lastly, a simplified and flatter structure will have a smaller surface area for bugs/testing/etc that will enable us to reach out beta goal sooner.

### Product Description

The app will have 3 tabs: Home, Wallet, Profile (copy tbc).

Home consolidates chats, contacts, and discover in the current release. This removes a lot of redundancy.

The sidebar is removed and contents merged into Profile.

Each screen is cleaned up and focused slightly.

<img width="679" alt="screen shot 2017-12-14 at 4 04 15 pm" src="https://user-images.githubusercontent.com/116099/34014328-c02a3e4c-e0e8-11e7-9784-c5495d48d438.png">
(sample screenshots for illustration)

### Minimum Viable Product

Description: migrate `wallet` and `profile` tabs
Goal Date: 2 weeks after start (target start date: 2018-01-02)
Started: 2018-01-06
Completed: 2018-02-09

__WARNING__ Related mockups are a dependency for this iteration.

* migrate existing profile to the `profile` tab (according to MVP mockups)
* update `wallet` tab (according to MVP mockups)
* migrate to tab based navigation (as described in #33)
* remove tabs `contacts` and `discover`
* prepare a detailed plan for `home` tab

### Iteration 1

Description: migrate `home` tab
Goal Date: 3 weeks after start
Started: 2018-02-12
Completed: 2018-03-22

* Restyle profile
* Update home tab
* Rework home "add" 
* Refine chats
* Remove old commands

__WARNING__ Related mockups are a dependency for this iteration.

## Post-Mortem

The scope of the iteration was very broad. Probably our biggest set of changes so far!

Nonetheless this swarm took significantly more time than expected.
We identified a number of area that could be improved:

* better scoping and time spent upfront to detail atomic tasks
* assign task per iteration and let assignees estimate them
* have a fixed scope per iteration: no new issue can be added during an iteration
* have a fixed team, at least per iteration (limit turnaround)
* improve test automation and ensure PRs do not introduce regression
* improve communication related to technical cost / hidden complexity

## Copyright
Copyright and related rights waived via [CC0](https://creativecommons.org/publicdomain/zero/1.0/).
  