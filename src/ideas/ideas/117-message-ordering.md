## Preamble

    Idea: #117-message-ordering
    Title: Message ordering
    Status: Draft
    Created: 2018-03-29

## Summary

Addresses point 2 and 3 of https://github.com/status-im/ideas/issues/99 .

Define, document and implement message ordering for 1-to-1, group and public chats so that we can give the best UX experience to the end user.

## Swarm Participants

- Lead & PM: @cammellos
- Testing & Evaluation: 
- UX/Design: 

## Product Overview

Because status is a fully decentralized app, message ordering is non-trivial and needs careful thinking in terms of UX, as some constrainsts needs to be taken into consideration.

Currently message ordering in the chat is by the time the message has been received which results in out-of-order messages and potentially different order on different devices.

The primary aim of this swarm is to define which message ordering gives users the best UX experience and implement the changes necessary (if any).

**Goals**
- Define, document and implement ordering of messages for 1-to-1/group/public chats

### Exit criteria

- Message ordering is clearly defined and documented, including the rationale behind the choices made
- Code reflecting the choice made is written and released
- Message ordering implementation is covered by integration tests (at least 80% coverage, as per OKR)

### Success metrics

- A/B testing of different implementations clearly shows a preference for the chosen ordering
- Ordering of messages can be understood by the end-user through documentation (survey)
- Implementation of ordering can be understood by developers just by reading the code/tests (survey)
