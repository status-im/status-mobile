---
id: 87-new-protocol
title: New Status App communication protocol
status: Limbo
created: 2018-03-08
category: core
contributors:
    - yenda
    - asemiankevich
    - serhy
    - janherich
    - cammellos
    - chadyj
exit-criteria: yes
success-metrics: yes
clear-roles: yes
future-iterations: yes
roles-needed:
---

## Preamble

    Idea: 087-new-protocol
    Title: New Status App communication protocol 
    Status: In progress
    Created: 2018-03-08


## Summary

The goal of this swarm is to define and implement a new protocol on top of whisper for the purpose of instant messaging within Status and make sure we separate the transport layer from the application code as much as possible. The new protocol should be more efficient in terms of bandwith, CPU and battery consumption, and be easier to implement for third parties.


Communication between Status App instances is done through the whisper p2p protocol on lower level, and we created our own communication protocol on top of it, serving following functionalities (only listing things relevant for MVP):

* Contact exchange
* Message exchange (1-1, private group and public group chats)
* Message status exchange (delivered/seen)
* Private chat moderation (creating chats, adding/removing users, joining/leaving chats)
* Online status broadcasting

All those functionalities are developed as specific payload format for whisper message envelopes, with status app being in charge of creating them & correctly interpreting them after receiving.

However, there are multiple problematic points in current protocol:

* There is no protocol versioning -> we are just updating the protocol code when new features are added and sometimes try to support older app instances (sending outdated protocol messages) by writing defensive receive code. There is no version tag in actual message, so we have to do multiple `if`/`when` checks on message keys which get's messy very quickly and it's hard for us to map protocol changes to released app versions.
* There are deep security flaws in way how protocol code is implemented, using hard-coded keys/passwords as symetric keys for whisper communication + storing private keys in not encrypted persistence storage (RealmJS), compromising the security.
* Implementation of message acknowledgments + message status exchanges is adding a lot of unnecessary traffic -> we never group messages and send them together in payload as one whisper message, even in situations where we very well can do it (for example opening chat with N new messages and sending acknowledgement about seeing all of them to the sender).
* Code quality, with protocol code being very "noisy", often doing a lot of unnecessary data transformations, requiring arguments from UI handlers which should be completely transparent to them (like extracting & passing `web3` object), not expressing actions in a clear way (for example request handshake is implemented as one message type, even though contact request & contact request confirmation are different steps where slightly different data need to be exchanged)

As it's very doable to actually extract the protocol code to separate namespaces (later even a separate library), we propose to tackle this problem as an rewrite of the whole protocol layer, instead of addressing all the points above by improving old protocol step by step.
Reasons for that are following:
* It provides more room to think about correct design from ground up, instead of maneuvering around current implementation details
* Basically everything in old protocol would need to be improved and modified, barely any part would be left untouched
* Even after addressing all the "real" user reaching issues like fixing security flaws, improving protocol efficiency + introducing protocol versioning, we would be left with less then ideal code quality
* All the issues listed above would needed to be tackled serially, otherwise it would be very demanding & error prone to address merge conflicts. This together with complete QA regression testing for each "step" would need significantly more time to actually fix everything compared to new protocol code, where everything could be tackled in parallel.

## Swarm Participants
- Lead Contributor: @yenda
- Testing & Evaluation: @asemiankevich @Serhy
- Contributor: @yenda 
- Contributor: @janherich
- Contributor: @cammellos
- PM: @chadyj

### Requirements & Dependencies
* Describe the new protocol code in a clear and easy to understand way ("birdview" with just each distinct message type and what they represent in term of actions explained)
* Describe each message type in detail, including all the information encapsulated in it (update whenever new features requiring protocol version inc are added)
* Describe security mechanism of the new protocol, where the sym/asym keys are used, how they are updated/refreshed, how do we choose whisper topics, etc.
* Deliver working code for testing, if we desire we can do that in 2 steps and first implement only contact exchange + 1-1 chats via new protocol, with old protocol code handling private + public group chats, and the second and final step will implement that + erase all obsolete old protocol code.

### Minimum Viable Product
Goal Date: 2018-04-10

Description: The new protocol is merged and supports 1-1 and public group chats. Documentation is finalized in the document https://docs.google.com/document/d/1Qh2h07T_qepzEJ7IytmxwIdQAOsGHrvhXwZxuZtbwgc/edit

### Iteration 1
Goal Date: 2018-04-17 

Description: Group chats are implemented and all bugs fixed

### Iteration 2
Goal Date: 2018-04-24

Description: Support for versionning is confirmed and tested experimentally. A benchmark for bandwith consumption in advanced features allows testers to verify performance claims with real conditions. Crypto libraries have been removed and traces of transport layer concerns have all been removed from the application code and database schemas

## Success Metrics
* We have protocol versioning, so we can say that for example release 0.9.15 and 0.9.16 are using the same protocol version v1, so are fully compatible and people can communicate with each other.
* Cutting the app data consumption by significant amount (optimistic but not unrealistic proposal is 1/3 of the current data consumption)
  KR: Reduce data consumption to <10Mb/day
* All crypto libraries can be removed on status-react side

## Exit criteria

Same as success metrics.

## Supporting Role Communication

## Copyright
Copyright and related rights waived via [CC0](https://creativecommons.org/publicdomain/zero/1.0/).
