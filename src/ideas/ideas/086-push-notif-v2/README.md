---
id: 86-push-notif-v2
title: Push Notifications v2
status: Active
created: 2018-03-01
category: core
lead-contributor: PombeirP
contributors:
    - PombeirP
    - nastya
    - adriacidre
    - yenda
    - chad
exit-criteria: yes
success-metrics: yes
clear-roles: yes
future-iterations: yes
roles-needed:
okrs:
   - "[P0]: Objective: Messaging is reliable"
   - "[P0]: KR: >95% of 20+ people surveyed trust Status for messaging"
---

# Meta

    Idea: 086
    Title: Push Notifications v2
    Status: In Progress
    Created: 2018-03-01
    Q2 Objective: #core 1.2

## Summary

The [current push notification system](https://docs.google.com/document/d/1OgjnY8ps8lVA4dIohwkfGK9HVt0nZxEWbuNdb7BX5-o/edit#) is sort of in an MVP stage. It works, but there are lots of important improvements which we need to accomplish in order to meet basic expectations from end users, such as supporting group chats and previewing the message sender and content on the notification itself.

## Swarm Participants

- Lead Contributor: @PombeirP (~20h/week)
- Testing & Evaluation: @nastya
- Contributor (Go): @adriacidre (24h/week)
- Contributor (Clojure): @yenda (20h/week)
- PM: @chad

## Product Overview

The user expects push notifications to be reliable and helpful while maintaining security. Some familiar behaviors illustrate some fundamental inter-connected qualities:

- Reliable:
  - A user expects to receive a notification in the following scenarios:
    - His screen is turned off, and he receives a message.
    - His screen is on, and the app is running in the background.
    - The app is in the foreground, and he receives a new message in a chat room which is not visible.
  - A user expects his conversation partners to be notified in a timely manner.
  - A user expects that the notifications he finds on his device accurately reflect unread messages.
- Helpful:
  - A user expects that the notifications he finds on his device accurately reflect unread messages. If he has browsed through a conversation that included messages mentioned in a PN, the PN should be updated accordingly, so the user is not misled and can trust the notification system.
  - A user expects to be able to see message details from notifications without needing to open the app (perhaps configurable due to privacy concerns?).
  - A user expects to be able to be able to ping other users selectively user @-type addressing (individual and group notifications), to reduce unnecessary noise.
- Timely:
  - A user expects that relevant events are received as notifications in a timely fashion (i.e., < 15 seconds from actual event).
- Secure:
  - A user expects that by receiving PNs, he is not giving up the degrees of anonymity and plausible deniability which are afforded by the app's underlying communication protocol.

We want to end up with a notification system which works on as many devices as possible and is resistant to censorship. There are countries which block certain notification providers (an example being China blocking Google Cloud Messaging). For that to be possible, we need to allow users to select their notification provider (with a reasonable default being Pushy or FCM), and to incentivize the creation of a paid notification provider economy. Per Oskar’s description:
> “Alice and Bob are chatting with Eve the masternode/mailserver listening. Bob uses F-Droid / lives in Cuba and thus can't/doesn't want to use Firebase/GCM (see whitepaper). Eve sees this as an opportunity to host a node which can serve Bob. Alice does not know any of this. Bob registers their method of choice for PN with Eve, so that when Alice sends a message to Bob, Eve also picks it up and triggers a push notification on Bob's device. Eve cannot inspect the payload.”

We also want a solution that doesn’t involve talking directly to the notification provider, as that would require keeping authentication elements embedded in the app (currently the case), and might expose us to quota theft.

### Flow Diagram (1:1 chat)

![Alt text](https://g.gravizo.com/source/svg?https%3A%2F%2Fraw.githubusercontent.com%2Fstatus-im%2Fideas%2Fmaster%2Fideas%2F086-push-notif-v2%2F1to1.dot)

For group chats, the flow is similar, but it's the chat admin which ensures that each chat member gets a notification channel for every other chat member. Notifications from chat members who left/were removed should be ignored.

### Goals

At a high-level, we want to move up the current solution a notch regarding some of the critical qualities mentioned in the [Product Overview](#product-overview) which are currently lagging behind. The specific steps to reach that goal are:

- Improve usability of notifications by:
  - Allowing the app to match a push notification with a specific chat message. That way we can:
    - [`P1`] support navigating to the correct chat when the user taps on the message, and conversely.
    - [`P2`] support [dismissing notification when user has seen](https://github.com/status-im/status-react/issues/3487) the respective message in a chat.
  - [`P3`] adding "quick actions" buttons on mobile notifications, so the user can directly "mark as read" or "delete" specific messages.

- Improve helpfulness by:
  - [`P0`] [retrieving the message body and displaying it](https://github.com/status-im/status-react/issues/3451) in the notification body so that the user can see who is talking and what is being said without needing to unlock the phone or opening the app.
  - [`P2`] supporting simplistic deep-linking. Clicking on a push notification should take you to the specific chat that is mentioned. We could achieve this functionality by looking for the message in all chats that match the ID from the notification.
  - [`P3`] making Push Notifications work in group chats. By default, participants would only receive notifications when being mentioned with an `@` symbol, to avoid creating a noisy environment that would degrade the whole experience.
  - [`P3`] supporting full deep-linking. Support for it might require mapping screens to specific URLs. Useful beyond push notifications.

- Improve security of the solution by:
  - [`P0`] not [embedding FCM](https://github.com/status-im/status-go/issues/343) (Firebase Cloud Messaging) server key in source code or app binary.
  - [`P4`] building a pluggable model for Push Notifications so that 3rd party providers can have a convincing economic model to host their own Push Notification servers.

### Requirements & Dependencies

- [#143-Seamless Login](../143-seamless-login)
- [status-im/status-react#3451](https://github.com/status-im/status-react/issues/3451): Preview notifications using background app refresh.
- [status-im/status-react#3488](https://github.com/status-im/status-react/issues/3488): Clicking message notification does not open the chat.
- [status-im/status-react#3487](https://github.com/status-im/status-react/issues/3487): Notifications about previous messages should disappear when chat is opened.

### Security and Privacy Implications

- It is possible for a third-party to easily exhaust the capacity of a Push Notification server by organizing a distributed attack to consume all the available slots. This should be resolved with paid notification servers.

## Exit criteria

There are undoubtedly enough issues identified to span several months of effort, so it seems reasonable to have a Swarm that tackles the problems which have the most impact on the user in the short term and leave the rest for a future Swarm to form around. There is value however in documenting the shortcomings of the current implementation, even if they are too far away on the horizon to be addressed in this Swarm (e.g., 3rd party PN provider support).

With that in mind, the exit criteria are as follows:

- The swarm has addressed all `P0` and `P1` goals mentioned in the [goals](#goals) section.

## Success Metrics

KRs:

- 100% of the messages that should generate a notification on the receiving device do so within 15 seconds, under different network conditions (i.e. Wi-Fi, cellular).
- Network and battery consumption values are same or less than current values.

## Dates

### Minimum Viable Product

Goal Date: 2018-05-07

Description: Show more information on notification

- Send only envelope hash on PN to destination device so that it knows to refresh messages and to match them to the notification.
- Update message body from retrieved Whisper message ([status-im/status-react#3451](https://github.com/status-im/status-react/issues/3451)).

### Iteration 1

Goal Date: 2018-05-21

Description: Implement notification server mode on `statusd`

- Connects to FCM, only 1 instance, no load-balancing.
- Add logic to statusd (accept special P2P messages from clients, and trigger notifications in response).
- Deploy service with Ansible.
- Change client so that it communicates with notification server to send notifications indirectly to contact.
- Address [status-im/status-react#3488](https://github.com/status-im/status-react/issues/3488) and [status-im/status-react#3487](https://github.com/status-im/status-react/issues/3487).

### Iteration 2

Goal Date: 2018-06-04

Description: Support simple deep-linking

- Open respective chat when taping on a notification.
- Hide notification when the user views the respective message in chat.

### Iteration 3

Goal Date: TBD

Description: TBD

<!-- "Multiple notification provider" phase -->

## Supporting Role Communication

TBD

## Useful Links

- [v1 Push notifications proposal](https://docs.google.com/document/d/1OgjnY8ps8lVA4dIohwkfGK9HVt0nZxEWbuNdb7BX5-o/edit#)
- [Whisper Push Notifications wiki](https://github.com/status-im/status-go/wiki/Whisper-Push-Notifications)
- [Work notes](https://docs.google.com/document/d/1TCeTY77fCGHqAseWkH74P0mk7PF8DGXN7xvbYqodnTI/edit#)
- [Gorush, a push notification server written in Go](https://github.com/appleboy/gorush)

## Copyright

Copyright and related rights waived via [CC0](https://creativecommons.org/publicdomain/zero/1.0/).
