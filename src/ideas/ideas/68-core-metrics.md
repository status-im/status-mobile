---
id: 68-core-metrics
title: Core Metrics
status: Limbo
created: 2018-01-10
category: core
contributors:
    - chadyj
    - lukaszfryc
    - jeluard
    - adambabik
exit-criteria: yes
success-metrics: yes
clear-roles: yes
future-iterations: yes
roles-needed:
---

## Preamble

    Idea: #68-core-metrics
    Title: Core Metrics
    Status: In progress
    Created: 2018-01-10


## Summary

Determine and implement a set of user-related core performance metrics that measure the goal of driving mass-adoption of Ethereum through Status use, and do so in a way that respects users privacy.

Project board: [status-im/projects/14](https://github.com/orgs/status-im/projects/14)

## Swarm Participants

- Lead & PM: @chadyj
- Testing & Evaluation: @lukaszfryc
- Contributor: @jeluard 
- Contributor: @adambabik  

## Product Overview

User-related performance metrics will help orient the team towards driving Ethereum adoption by being data-informed, and provide a way to assess if the team is making progress towards this goal.  The most important metric driving this goal is **daily transacting users** (DTU). In support of DTU's there are sub-goals related to engagement, adoption, retention, and task success for the app as a whole and individual features such as wallet, chat, and Dapp's.  

This swarm should determine the best core metrics to measure our goals and progress, then create methods to track, store and view them. 

Metric collection will be opt-in only to ensure that users privacy is respected.  Any metrics gathered will be strictly in the service of making Status better for users and never for any advertising or monetisation reasons, and be completely anonymous.


### Product Description

This is not a user-facing Swarm. The product work is for Status org consumption.

Core goals are not intended to measure for measurements sake, but to provide product insight, create feedback loops that can accelerate decision making and product development, and let us know how users are using Status at scale. After determining the metrics, the next step is collecting the metrics and establishing a baseline that future quantitative goals can be measured against. 

This draft has the start of proposed metrics that would give us deep insight into Status and Ethereum usage, and is to be revised with team feedback. Each future swarm can also pick metric(s) from this list and track them as part of their success metrics.

**Goals**
- Daily transacting users (DTU)
- Monthly transacting users (MTU)
- Daily SNT transaction volume
- Monthly SNT transaction volume
- Daily ETH and ERC20 token transaction volume
- Monthly ETH and ERC20 token transaction volume

**Engagement Metrics**
_Level of user involvement_
* Daily active users (DAU)
* Monthly active users (MAU)
* Daily messages sent 
* Monthly messages sent 
* Daily messaging users 
* Monthly messaging users 
* Daily DApp Users 
* Monthly DApp Users 

**Adoption Metrics**
_New users of a product or feature_
* Daily downloads
* Monthly downloads
* Daily signups
* Monthly signups
* Percentage of 1d/7d/30d users that do a transaction
* Percentage of 1d/7d/30d users that send a message
* Percentage of 1d/7d/30d users that open a DApp

**Retention**
_the rate at which existing users are returning_
* Percentage of new users using Status after 1d/7d/30d/3m/6m/12m

Healthy stats would show increasing DTU, engaged users across the core of the app, the widespread adoption of core features, and returning users. 

There are two main types of metric tracking available: in-app tracking and external tracking.  In-app tracking is instrumented from within the app, and will need to adhere to strict guidelines including being opt-in, honest and clear, and anonymous. External tracking can use public or available sources, such as Play/App store accounts, or the blockchain.  A complete picture will involve both in-app and external tracking.



### Iterations

**Iteration 1**
<!-- Mandatory, completes the Idea in the fastest route possible, can be hacky, needed to feel progress. See https://imgur.com/a/HVlw3 -->
Goal Date: 2018-02-28
Status: Complete.

Description: Collect chat message volume as a fast an non-intrusive way to measure app engagement. This will tell us daily/monthly sent messages and we can get a sense for how app use is growing.  

* [x] [Collect Whisper metrics in MailServer](https://github.com/status-im/status-go/issues/536)
* [x] [Setup a dedicated server for Prometheus and Grafana that can handle data retention time of 30 days.](https://github.com/status-im/status-cluster/issues/36)
* [x] Weekly chart of sent messages



**Iteration 2**
Goal Date: 2018-03-30 
Status: In Progress.

Description: Track basic product events to start measuring engagement

* [x] [Add Mixpanel client library](https://github.com/status-im/status-react/issues/3537)
* [x] [Track engagement events](https://github.com/status-im/status-react/issues/3545)
* [x] Opt-in screen during signup
* [ ] [Opt-in toggle in settings](https://github.com/status-im/status-react/issues/3538)


### Success metrics

This swarm is considered successful if it empowers 4 other swarms to use core metrics to measure their OKR's.

### Exit Criteria

When iteration 2 is complete this swarm will end. Any new metric instrumentation is to be completed by user-facing swarms that implement new screens or functionality by updating the [mixpanel_events.edn](https://github.com/status-im/status-react/blob/develop/src/status_im/utils/mixpanel_events.edn) file.
