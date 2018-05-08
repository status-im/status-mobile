---
id: 001
title: Offline inboxing and persistent messaging
status: Completed
created: 2017-10-25
category: core
lead-contributor: oskarth
contributors:
    - oskarth
    - annadanchenko
    - adambabik
    - naghdy
    - denis-sharypin
    - b00ris
    - JekaMas
    - dmitryn
exit-criteria: yes
success-metrics: yes
clear-roles: yes
future-iterations: no
roles-needed:
---

## Preamble

    Idea: DEV#001
    Title: Offline inboxing and persistent messaging
    Status: Done
    Created: 2017-10-25
    Started: 2017-11-14
    Ended: 2018-01-04

## Summary
Make it possible for users to see messages sent to them while they were offline.

## Vision
No messaging app is complete without persistent messaging. Unlike servers, end users with mobile phones as endpoint go in and out of connectivity. When a user comes back online, the expectation is that they can see messages that was sent while they were gone. How can we solve this without compromising on decentralization and anonymity? There are two basic scenarios we want to solve.

Basic user story A: User A and B are chatting. User A goes offline for a longer period of time. In the meantime, User B sends a message to A and goes offline as well. When User A opens app they see the message from B.

Basic user story B: User A enters public chat room X. User A goes offline and people keep chatting. When User A comes back they instantly see all the missed messages in X.

We want to make it easy to run a Whisper mail server (part of Whisper v5 API). This will archive messages and forward them to a peer upon request. You can run this yourself if you want to, but we’ll also provide default mail servers through our clusters, and make it easy for anyone to set one up. This can be paid using SNT, which aligns incentives in terms of Quality of Service.

## Swarm Participants

- Lead Contributor: @oskarth (20/w)
- Testing & Evaluation: @annadanchenko (20h)
- Go Contributor: @adambabik (20h/w)
- PM: @naghdy (5h/w)
- Design: @denis-sharypin (30h/w)
- Go Contributor: @b00ris (40h/w)
- Go Contributor: @JekaMas (?)
- Clojure Contributor: @trybeee (?)

Squad size: 5-8 people.  Squad channel: **#1-offline-inboxing**

### Update on time commitments after MVP

- Go contributors available until Christmas, after this on retainer in background
- @trybeee just joined swarm
- Design, no new designs right now - on retainer for iteration 2 (MVP sketch from design POV)

## Requirements

From a user point of view, anything that solves one or both of the user stories seems like a good start. From a 10 000 feet technical view for the initial version:

1. status-go: Ability to run a standalone wnode with Whisper Mail Server from the terminal
2. status-go: `RequestHistoricMessages` binding exposed that status-react can call
3. status-react: put these messages in their appropriate place
4. status-react: handshake with wnode to get it to receive messages on its behalf

Side note: Exactly what amount of granularity do we want these requirements to be at? And should it be for MVP or for future iterations as well?

Ultimate goal: Decentralized marketplace where people host and connect to wnodes/mailboxes, informed by their reputation, and pay/get compensated with SNT in a fluid fashion. Seamless end user experience in terms of reliability of offline messages showing up when they start the app.

## Goals & Implementation Plan


### Minimum Viable Product

Initial Goal Date: 2017-11-24
Updated Goal Date: 2017-12-08
Completed: 2017-12-08
Passed Full QA: TBD
Description: Run a wnode from the command line; let A send message to B who is offline, and then A goes offline again. When B comes online it should call wnode which should reply with some payload that contains A's message. This payload should be visible in A-B's chat. The wnode can be hardcoded.

### Iteration 1 - Public chat, MVP retainer, and laundry
Goal Date: 22 December
Started: 2017-11-08 (MVP end) or 2017-11-12 (retro meeting done)
Completed: 2017-12-22 (main user story)
Description: Main user story: User A enters public chat room X. User A goes offline and people keep chatting. When User A comes back they instantly see all the missed messages in X.

Additionally, MVP retainer and laundry: QA of MVP complete and all MVP work merged into develop on status-react and status-go. MVP bug fixing. Basic error handling, refactoring and logging. Documentation for updating wnode-status and API calls introduced. Identify and make bounties of some nice-to-have / upcoming issues. Planning and swarm participants re-organization: likely one more Clojure dev and identify Go dev needs. Clarification on interface (enode). Specify group chat workflow and tests thereof.

Uncertainty: Swarm availability, both w.r.t. other ideas and holidays (seems OK). Availability normal for 2 weeks: Adam 2w 22 Dec, Eugene, Boris, Dmitry, Oskar After Christmas no more Go dev necessary.

### Iteration 1.5/2 - Joe Blockchain goes to town (multiple wnodes)
Goal Date: TBD - est. +~2w, due to holidays possibly delayed till mid January
State: Aborted
Completed: 2018-01-14 (very partial; read only screen under flag)
Description: Ability to toggle between different wnodes in the app. Show basic connectivity to wnode in interface. The wnodes identifiers are hardcoded in the app. Add by enode (?). Multiple wnodes enabled for redundancy (?).

Uncertainty: Go dev work necessary. Seems docs and options for wnode-status but other than that not. Only on retainer.

## Success metrics and exit criteria

Post-hoc: solving the two user stories above.

## Supporting Role Communication

## Post-Mortem

As of today (Jan 4) the swarm is closing down. There might be small things left and stuff to bounty etc, but we can take these on a an ad hoc basis. The main user stories have been solved, no major bugs found during retainer period, and it is probably time to focus on other things.

No formal retrospective has been performed. Retrospective from ~1m ago still holds the most potent lessons: https://docs.google.com/document/d/14XZoqI0t28WD4qBKA7jnf_c99R94jSlCLwK4XpaFPvc/edit

### Timeline and focus
The idea was created on 2017-10-25, started 2017-11-14 (+20d), MVP delivered 2017-12-08 (+24d) and ended 2018-01-04 (+27d). A total of 71 days from idea conception to completion, or 51 days from idea start to completion.

Focus: Lacking precise numbers, but a very rough estimate is 5 times 20 times8=800h (~5+-1 ppl avg active any given time, ~20h+-10h/w avg time, 8w+-2w, probably a bit less). Swarm participants ranged from 4 to 8 on a weekly basis (@adambabik @annadanchenko @b00ris @denis-sharypin @dmitryn @JekaMas @naghdy @oskarth).

### Main user stories solved are
1. User A and B are chatting. User A goes offline for a longer period of time. In the meantime, User B sends a message to A and goes offline as well. When User A opens app they see the message from B.

2. User A enters public chat room X. User A goes offline and people keep chatting. When User A comes back they instantly see all the missed messages in X. This also works for group chats.

### Issues solved

https://github.com/search?q=org%3Astatus-im+label%3A1-offline-inboxing

### Additionally
- A read only screen for changing wnode/master node has been implemented (not part of MVP scope and under flag)

[image](https://user-images.githubusercontent.com/1552237/34560596-b0dcf1a8-f189-11e7-858f-3f1227541568.png)

- It's straightforward to change wnode/master node in code (not exposed in UI or build settings)

- A wnode/master node is running persistently in status-cluster

Future work
In future swarms there are a few that will be touched on:

- Integrating push notifications into wnode/master node
- Allow people to change to their own wnode/master node in UI, as well as in Desktop (might be bounty work only)
- SNT payment layer for usage of wnode/masternode

Finally Thanks for all your hard work these last two months, especially @adambabik @b00ris @dmitryn and @JekaMas!

## Copyright
Copyright and related rights waived via [CC0](https://creativecommons.org/publicdomain/zero/1.0/).

## Resources
- https://github.com/ethereum/go-ethereum/wiki/Whisper-Overview#mail-server
- https://github.com/ethereum/go-ethereum/tree/master/whisper/mailserver


## Appendix: archive of future work

Things that were considered but cut.

### Design

Consider how to surface and solve the following challenges in our UI. wnode and mailbox used interchangeably. This list should probably be split into MVP concerns and later concerns as there is quite a lot.

- How will user select and deselect a wnode?
- How do we communicate more "trusted" nodes, like the ones we operate initially?
- How do we communicate and quantify reputation such as uptime and reliability? Start ad hoc (upvotes? number of connected nodes? "official" Status nodes with guaranteed uptime?)
- How will the cost of using node be determined? Initial setup and running costs?
- What happens if we fail to connect to our mailbox? How do we communicate this in a chat?
- How communicate to the sender that a message was ACKed by a mail box but not by end user?
- What about if a user wants to use multiple mailboxes?
- Where can a user get an overview of the mail server / contracts they are using?
- How will ongoing payment signing work?
- How do we communicate a certain wnode is dead or can't be trusted for some reason?
- How do we communicate capacity of a wnode to ensure quality of service?
- How does a wnode operator accept or deny a request to act as mail server? How is this communicated to end user?
- Initially a wnode will probably just be a CLI thing. How do we surface options like how much compensation we want? And what about when this can be run through Status Desktop? How do you decide how much capacity you have as a wnode?

### Code

Smorgasbord of desirable features in the future. Revisit this.

- Discovery of wnodes
- Ability to select wnodes
- Running wnodes in cluster and making it possible for others to run them
- Giving feedback in chat about state of messages
- Using multiple wnodes
- Cryptoeconomics to incentive running of nodes
- Basic reputation system to signal quality of service

