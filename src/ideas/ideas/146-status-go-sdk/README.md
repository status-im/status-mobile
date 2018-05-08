## Preamble

    Idea: 146-status-go-sdk
    Title: Status GO-SDK
    Status: Draft
    Created: 2018-04-03


## Summary

Provide an easy to use GO-SDK library to allow community developers easily interact with status messaging.


## Swarm Participants
- Lead Contributor: [@adriacidre](https://github.com/adriacidre)
- Contributor: [@pilu](https://github.com/pilu)
- PM: [@rachelhamlin](https://github.com/rachelhamlin)
- Testing & Evaluation: [@lukaszfryc](https://github.com/lukaszfryc)


## Product Overview

### Product Description

One of the main goals of status is to grow its community, however is not easy to work on status-go project as a community developer.
Is not easy to set up an environment

- Identify where to start
- Interact with status messaging system.
- Test your changes

The idea behind this swarm is to simplify this by allowing community developers to interact with status, based uniquely on status-go library.

### Requirements & Dependencies

- *Easy to use SDK* : Using a sdk should be as easy as using any other messaging system, allowing you to subscribe or publish to specific chats.
- *The less dependencies the better* : ATM and in order to interact with status messaging system we need to interact with code on status-go and status-react repos. This should be simplified to allow devs work only on top of status-go
- *Quick dev environment setup* : Following the previous point, if we’re only based on status-go, dev environment should be trivial. Should be easy to build apps and distribute them as binaries for different platforms.


### Minimum Viable Product

MVP should allow a developer to accomplish some minimal actions:

- Connect : creates a new connection
- Close : closes active connection
- Auth : login as a specific user
- Subscribe : subscribe to a channel
- Unsubscribe : unsubscribe from a channel
- Publish : send a message.

_All methods should work for 1to1, groups and public chats._

With this methods building a simple `autoresponder bot` should be as simple as:

**TO BE DEFINED**
```
// Connect to the default status url and defer closing the connection
st, _ := status.Connect(st.DefaultAddr) // -> localhost:51515
defer st.Close()

// Authenticate as an specific user
st, _ = status.Auth([]byte(“S3cretT0ken”))

// Subscribes to any message on chat chatID
sub, _ := st.Subscribe("chatID", func(m *st.Msg) {
    // Replies the received message
    m.Reply([]byte("I can help!”))
    sub.Unsubscribe()
})

// Simple message publisher
st.Publish(“buddy_token", []byte("Hello World"))

```

SDK should be provided with different, easy to follow and well documented examples _(TBD)_:

- Autoresponder bot (nice for testing)
- Google assistant integration
- Slack proxy (use status from slack)
- Reminders (set and get reminders)

Description: <!-- Description of Deliverables-->

### Useful links & Dependencies

- http://offsite.chat
- [https://github.com/mandrigin/status-go-bots](#humans-need-not-apply bots)
- [Pre-existing issue](https://github.com/status-im/ideas/issues/131)
- [New protocol spec](https://docs.google.com/document/d/1Qh2h07T_qepzEJ7IytmxwIdQAOsGHrvhXwZxuZtbwgc/edit#)
- [transit-format](https://github.com/cognitect/transit-format)
- [Sniffing app messages](howto_Sniffing_app_messages.md)


### Blockers
New Status App communication protocol - https://github.com/status-im/ideas/issues/87

## Dates
Goal Date: TBD

Description: TBD

Testing Days required: TBD

## Success Metrics

#### Milestone 1: [Status messaging basic interaction](BasicSDK.md)
  - [ ] [Setup a new connection](BasicSDK.md#setup-a-new-connection)
  - [ ] [Ability to close a specific connection](BasicSDK.md#ability-to-close-a-specific-connection)
  - [ ] [Ability to change connection configuration](BasicSDK.md#ability-to-change-connection-configuration)
  - [ ] [Ability to sign up on the platform](BasicSDK.md#ability-to-sign-up-on-the-platform)
  - [ ] [Ability to login as a specific account](BasicSDK.md#ability-to-login-as-a-specific-account)
  - [ ] [Documented API for basic sdk interaction](BasicSDK.md#documented-api-for-basic-sdk-interaction)

#### Milestone 2: [Public channels interaction](PublicChannels.md)
  - [ ] [Ability to join public channels](PublicChannels.md#ability-to-join-public-channels)
  - [ ] [Ability to publish messages on public channels](PublicChannels.md#ability-to-publish-messages-on-public-channels)
  - [ ] [Ability to subscribe to public channels](PublicChannels.md#ability-to-subscribe-to-publc-channels)
  - [ ] [Ability to unsubscribe from a public channel](PublicChannels.md#ability-to-unsubscribe-from-a-public-channel)
  - [ ] [Documented API for public channels interaction](PublicChannels.md#documented-api-for-public-channels-interaction)
  - [ ] [Working examples for public channel interaction](PublicChannels.md#working-examples-for-public-channel-interaction)

### Milestone 3: [Contact management](ContactManagement.md)
  - [ ] [Invite new contact](ContactManagement.md#invite-new-contact)
  - [ ] [Accept a contact request](ContactManagement.md#accept-contact-request)


#### Milestone 4: [1 to 1 messages interaction](1to1Channels.md)
  - [ ] TBD : [Ability to send 1 to 1 conversation](1to1Channels.md#ability-to-send-1-to-1-conversation)
  - [ ] TBD : [Ability to subscribe to 1 to 1 conversation](1to1Channels.md#ability-to-subscribe-1-to-1-conversation)
  - [ ] TBD : [Ability to unsubscribe from a 1 to 1 conversation](1to1Channels.md#ability-to-unsubscribe-1-to-1-conversation)
  - [ ] TBD : [Documented API for 1 to 1 conversation](1to1Channels.md#documented-api-for-1-to-1-conversations)
  - [ ] TBD : [Working examples for 1 to 1 conversations](1to1Channels.md#working-examples-for-1-to-1-conversations)

#### Milestone 5: [Private groups interaction](PrivateChannels.md)
  - [ ] TBD : [Ability to publish messages on private groups](PrivateChannels.md#ability-to-publish-messages-on-private-groups)
  - [ ] TBD : [Ability to subscribe to private groups](PrivateChannels.md#ability-to-subscribe-to-private-groups)
  - [ ] TBD : [Ability to unsubscribe from a private groups](PrivateChannels.md#ability-to-unsubscribe-to-private-groups)
  - [ ] TBD : [Documented API for private groups interaction](PrivateChannels.md#documented-api-for-private-groups)
  - [ ] TBD : [Working examples for private groups interaction](PrivateChannels.md#working-examples-for-private-groups-interaction)

#### Milestone 6: Extra methods
  - [ ] Ability to recover your account
  - [ ] Ability to log out.
  - [ ] ...


## Exit criteria:

This swarm will be deemed successful and be closed upon the delivery of the above milestones.

## Success Metrics

A dev who is unfamiliar with the SDK is able to set up his environment and build a simple chatbot in under 2 hours, just by following the documentation.

## Supporting Role Communication
<!-- Once Requirements and Goals are fleshed out, then it should be communicated to supporting organelles if required -->

## Copyright
Copyright and related rights waived via [CC0](https://creativecommons.org/publicdomain/zero/1.0/).
