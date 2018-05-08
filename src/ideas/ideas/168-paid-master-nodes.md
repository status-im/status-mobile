## Preamble

    Idea: 167-paid-master-node
    Title: Paid master nodes
    Status: Draft
    Created: 2018-04-13

## Summary
In Status, users are stakeholders. This means we pay and get paid based on
services provided. This includes high-connectivity nodes, aka master node, which
are used for things like offline inboxing. We choose to let users pay other
peers for this service, as opposed to providing it for free and selling our
users.

## Swarm Participants

- Lead Contributor: @oskarth
- QA: TBD
- Evaluation: TBD
- Clojure Contributor: TBD
- Go Contributor: TBD
- PM: @rachelhamlin
- UX(R): @jpbowen
- Designer: TBD
- Community: @andytudhope

## Product Overview

This idea takes of where https://github.com/status-im/ideas/blob/master/ideas/1-offline-inboxing.md ended. It provides an SNT payment layer for usage of master nodes.

Users want basic services such as offline inboxing to just work, while at the
same time paying for the resources they are using, as opposed to being a product
sold to other businesses. Users also want to make SNT so they can use it in the
app.

Currently offline inboxing is a free resource, subsidised and run by us. This is
sub-optimal for many reasons, and goes against our vision of letting users pay
for things they use, as well as incentivizing this capability to be
decentralized.

Offline inboxing is run on mail server aka master nodes. In the scope of e.g.
86-push-notifications push notifications will likely be served through that as
well.

### User stories

User story 1: Bobby Blockchain runs a master node and gets paid by peers in SNT for services provided.

User story 2: Alice pays SNT to a master node in order to get offline inboxing for some period of time.

Out of scope for this idea:

- (User story 3: Decentralized discovery mechanism for master nodes)

- (User story 4: Onboarding mechanism to seed closed beta users with SNT)

User story 4 is sufficiently different in focus to warrant a separate swarm.
Since it might be a pre-requsite from a UX point of view, the behavior in user
story 1 and 2 should be toggle-able with a flag. I.e. we make sure we can still
provide free service for the time being.

In addition to these defined user-stories, additional ones might be carved out,
such as communication of quality of service and price discovery mechanisms. It
is unlikely these will be relevant until user story 1 and 2 are solved, but this
swarm will decide if it makes sense to continue working to solve additional user
stories after 1 and 2 are done.

### Product Description

Currently we have a screen that shows mail servers available. Each mail server
will have a basic monthly fee attached to it. This can start off as static but
evolve to being dynamically communicated through our application protocol. In
auction terms, this is an, initially static, bid process.

> Comment from review: Are there any selection criteria for to show users other
> than price? Eg users/transaction, ratings, reviews, quality?

As a user picks a mail server they are asked to sign a transaction in order to
pay for offline inboxing for 1..N months. Sending this transaction counts as
paying for this mail server. This is similar to paying for an app or service in
the Apple store.

Mail server needs to be able to process this transaction and have logic for
whitelisting this user's enode for some period of time.

How re-newal will happen is not specified yet.

> Comment from review: May be out of scope for this idea but perhaps we'll want to experiment with different payment models for time - e.g. Alice pays until she runs out of SNT OR N offline messages OR set period of time.

Solving the initial user stories don't require any proof of message delivery or
quality of service. Reputation of specific mail servers thus happens completely
outside of the app, e.g. through Riot, Reddit, etc.

#### Note on Status Desktop

Status Desktop isn't a requirement for this idea as a user can run a mail server
from the command line. It is an additional user story that shouldn't be too
difficult to implement, but it isn't clear how desirable it is, assuming that
high availabity of mail servers is a requirement.

#### Terminology

Mail server / wnode / master node are used interchangably. Master mode is the
"soft" user term (invented by the community!), mail server and wnode are
technical terms on status-go side.

#### Relevant reading

- https://status.im/whitepaper.pdf
- http://nakamotoinstitute.org/static/docs/micropayments-and-mental-transaction-costs.pdf

### Requirements & Dependencies

(Lack of consensus on this point: Possibly onboarding SNT give-away logistics.)

### Minimum Viable Product
<!-- Mandatory, completes the Idea in the fastest route possible, can be hacky, needed to feel progress. See https://imgur.com/a/HVlw3 -->
Goal Date: TBD - May 1 + 2w

Description: Spike out most basic proof of concept possible for payment / service on or off in Clojure and Go, using SNT test token.

## Dates
Separate tracks:
- UX(R) / design
- Go dealing with app protocol / payment

Goal Date: <!-- Date for evaluation in ISO 8601 (yyyy-mm-dd) format --> 

Description: <!-- Description of Deliverables-->

Testing Days required: <!-- Days required at the end of development for testing -->

## Success Metrics

- Users running mail servers and transacting. Target: 10

- Users paying for a mail server. Target: 50% of D7 retained users, or 20% of all users.

- Daily Transacting Users. Target: 35 ((5000 DAU TARGET*0.2 TX%)/30 PAY-MONTHLY).

OKRs:
2.4: >20% of users send a transaction (P1:P3)
3.1: 2x launched SNT use cases (P2:P0)

## Exit criteria

Solving the two initial user stories. If the swarm deems it useful to continue,
new specific user stories will be specified and inform the exit criteria.

## Supporting Role Communication

- Community for recruiting users to run mail server.

- Finance for SNT utility approval.

- Marketing for 'you are not the product' rationale communication.

## Appendix: Constraints / objections

- No one will pay for it.

This is a free resource. If we want to subsidise it and the concern is people
  not wanting to stay / top up to use basic funcrtionality we can solve it by
  giving free SNT as part of SNT onboarding.
  
- Free SNT?!

Yes. For a closed beta limited to 40k people, we can limit the free amount to 5
SNT per person, which limits the downside risk this to 200k SNT ($20k at the
time of this writing).

- What about fraud?

We can use already collected emails as identity and dedup by it. Max downside
per email (pre-signed up) is 5 SNT, and to get it out would still be a
transaction, as well as a signal to us.

- How does this scale?

For open beta we can figure out how to deal with SNT onboarding by e.g. using
trustlines.

---

For more on rationale of SNT utility, see the whitepaper. Providing onboarding
SNT also furthers distribution of tokens as well as transactions on their own, a
good.

This is a hypothesis, but this is the whole basis of Status.

## Copyright
Copyright and related rights waived via [CC0](https://creativecommons.org/publicdomain/zero/1.0/).
