---
id: 99-confidence
title: Confidence in Messaging
status: Active
created: 2018-03-22
category: core
contributors:
    - rasom
    - lukaszfryc
    - mandrigin
    - dmitryn
    - chadyj
    - nikitalukianov
    - jpbowen
    - hesterbruikman
exit-criteria: yes
success-metrics: yes
clear-roles: yes
future-iterations: yes
---

## Preamble

    Idea: 99
    Title: Confidence in Messaging
    Status: In Progress
    Created: 2018-03-22


## Summary
As end users we currently don't have confidence that messages are being sent, delivered and read. This swarm aims to address this.

## Swarm Participants
- Lead Contributor: @rasom
- Testing & Evaluation: @lukaszfryc
- Contributor: @cammellos
- Contributor: @mandrigin
- Contributor: @dmitryn
- PM: @chadyj
- UX: @nikitalukianov @jpbowen @hesterbruikman

## Product Overview

We currently don't have confidence in the reliability of messaging in Status. The reasons for this lack of confidence ranges all the way from how 'read' status is displayed in the app, to trivial - and non-trivial - bugs, to bad uptime of critical infrastructure, to how push notifications are decoupled from Whisper messaging, to a fundamental lack of (understanding of) reliability guarantees Whisper protocol provides.

What all these issues have in common is that they cause the end user to not have confidence in their messages being delivered and read. This swarm aims to attack this from a user point of view. It is expected additional swarms might be spawned for more core technical areas as these are identified (e.g. network visibility or push notifications v2).

### Product Description
This section is based on user feedback spawned as part of 75-status-everyday (https://docs.google.com/document/d/1pkfZWxr9I0AqidEuOfogzxEXrcg6ofs2bUkh-HSKD6o/edit)

1. Messages should always be sent.

    a. Generally: lack of trust in messages being sent
    b. Provide better feedback of when messages are or aren’t sent
    c. Allow re-send of individual messages a la Whatsapp

2. Messages are in wrong order.

    a. Confirm fixed in 1on1
    b. Make decision re migrations for old messages
    c. Understand and clarify work needed for private group / public (placeholders)
    d. Design: Deal with right order but “message arriving too late” (see sidebar)

3. Messages lack timestamps.

     a. This appears to contribute to a lack of confidence in messaging
     b. Design (then dev): Add this to UI

4. Messages don’t arrive despite being sent, or they arrive late.
    a. Disambiguate offline from mail server node copy
    b. Understand what is needed to make mail server HA
    c. Give user feedback while it is fetching messages (non-instant)
    d. Understand if there are more reasons messages would arrive late

5. Messages should always be delivered, 100% of the time.

    a. Similar to 1 but stronger
    b. What happens if mail server goes down?
    c. Can we implement read/re-send semantics at Whisper level (mailserver)?
    d. Look into how read receipts can be improved
    e. Clarify UI a la Whatsapp double-tick
    f. Consider tracking send/delivered ratio as a metric

6. Offline inboxing not working in public chat.

    a. Clarify perf dependency on smooth-ui swarm
    b. While perf is being worked on, educate user about offline only being 1-1

7. Perception of lack of notifications.
    a. UXR: understand expectation -  for non-contacts? Public chats? in-app?
    b. Separate PN swarm, clarify dependency with this swarm

8. Get notification but nothing visible in app.
    a. PN delivery being decoupled from Whisper delivery, see point 7 above
    b. Clarify dependency on PN v2 swarm and where work done

9. Perception of never received messages from other people.
    a. Investigate if same as filter while offline bug and current limitations
    b. More 7: Investigate non-contact PN possibility
    c. More 7: Public chat PN?
    d. UXR: Understand this problem and perception better

10. Sometimes need to change mail server manually to get messages working.
    a. Make failure of individual mail server apparent and prompt switching
    b. Automated switching?
    c. How ensure no switching is necessary? (HA, see 4)

### Requirements & Dependencies

Request messages history after background: https://github.com/status-im/status-react/pull/3493
New protocol https://github.com/status-im/status-react/pull/3273
https://github.com/status-im/ideas/issues/87

### Security and Privacy Implications

None currently known.

### Iteration 1

Complete several reliability improvements that have been identified so far:

- [#3827 Message reliability survey](https://github.com/status-im/status-react/issues/3827)
- [#3793 Improve timestamps in chat messages](https://github.com/status-im/status-react/issues/3793)
- [#3787 Improve network offline and mail server error messaging](https://github.com/status-im/status-react/issues/3787)
- [#3784 Provide users with delivery status feedback when sending messages](https://github.com/status-im/status-react/issues/3784)
- [#3792 Measure message send/receive ratio on internal builds ](https://github.com/status-im/status-react/issues/3792)
- [#828 Send an expiration signal when envelope wasn't delivered to any peer](https://github.com/status-im/status-go/pull/828)
- [#810 Notify clj side when the message actually "left" local node.](https://github.com/status-im/status-go/issues/810)
- [#3785 Remove "seen by everyone" from public chat](https://github.com/status-im/status-react/issues/3785)

### Iteration 2

After iteration 1 is complete the swarm will meet and discuss the results of the UXR survey, send/receive ratio results, review current UX and discuss future iterations. Work will then be planned for a future iteration, or the swarm will be closed.

## Exit Criteria

- >99% message deliverability from [#3792](https://github.com/status-im/status-react/issues/3792)

## Success Metrics

95% of a group of 100 users surveyed - who don't have additional context beyond Status providing a p2p IM capability - using the app for an extended period of time, should answer 'yes' to the question: "Do you trust Status to deliver messages for you?" (and possibly variants of this).

This is fundamentally a soft or qualitative goal. It is thus necessary but not necessarily sufficient, and additional harder numbers might be used as we develop the capability to measure this.

Zero instabug reports within 30 days of alpha release

## Copyright
Copyright and related rights waived via [CC0](https://creativecommons.org/publicdomain/zero/1.0/).
