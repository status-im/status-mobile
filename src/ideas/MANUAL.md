# Status Swarm Manual

This document attempts to outline a broad set of guidelines so we can operate swarms effectively as an organization.

What we are working towards is an entirely decentralized organization, where ideas and swarms are funded by any user, stakeholder, in the system. This is a non-trivial problem in terms of incentive design. To get there we are currently using a two-pronged approach. This document focuses purely on the first point:

1.  Formalize swarm lead process and come to a shared agreement about role expectations

2.  Run incentive/reputation design experiments through Open Bounty

Additionally, there will likely be some changes to ideas repository to
facilitate the running of a swarm.

### Swarm lifecycle

A swarm can be in the following states: draft, in progress, completed or aborted:

- A swarm starts off as an idea in draft mode

- Once an idea has been well-formulated and has enough interest, it is in progress

- By default, each swarm is removed from in progress at the end of each iteration period

- If the swarm is still active, the swarm lead can mark it as in progress again

- If after the next period the swarm is still not in progress, it will be marked as aborted

- Once a swarm is determined to be done, it gets closed and marked as completed.

Additionally, a swarm that isn’t in progress should not be actively worked on. This ensures we communicate things properly before doing them and we have a critical mass of development on the active swarms.

### Swarm status updates

A public status update that answers the following questions:

- What has been done since last time?

- Lessons learned

- What is the set of deliverables for the next period?

This is a way of ensuring accountability and providing a clear interface to the rest of the organization. It is also helpful for people who want to participate in a swarm and want an overview of what is going on.

## Swarm Lead

Thank you for being a swarm lead! This is a new type of role which we are still defining as we go and learn things, as well as refining the whole swarm concept. Being a swarm lead is different from being a traditional team lead in at least two ways:

- Swarm leads are self-nominated and based around a specific idea

- Swarms are ephemeral and have a specific lifecycle

### Swarm Lead Responsibilities

As a swarm lead, you are responsible for:

#### Being in an interface to the rest of the world.

- Reporting the status of the Swarm at the bi-weekly Town Hall using the report template.

- Update Github Issue with this status (simple copy/paste).

- Interact with other swarm leads to ensure integration points are dealt with.

#### Create a clear process and expectations for swarm members.

- Overseeing prioritization of issues and work being done by swarm members.

- Creating a systemic cadence (e.g. weekly sync) for swarm members to sync.

- Have clear onboarding steps for new people who join the swarm.

#### Make sure there are clear goals for the swarm.

- Defining success metrics and exit criteria for the swarm.

- Set clear milestone for Swarm progress.

- Clarifying which OKR(s) the Swarm is tackling.

#### Engage people necessary to successfully complete the swarm.

- Engaging additional roles that are useful to the swarm, such as PM/UX/QA.

- Ensure there’s an evaluator who is responsible for verifying exit criteria is met.

- Ensure people that join the swarm have the time available to dedicate.

#### Managing and communicating lifecycle of a swarm properly.

- Don’t start swarm until it is out of draft mode.

- Make sure a swarm that is active is marked as in progress.

- Close swarm with a final written report, even if swarm is abandoned.

## FAQ

### Should everyone be a swarm lead?

We will encourage and empower people to lead, but not expect or force them to.

### How do we deal with failure?

Silent failure is worst case. Capture the upside of failure, what the swarm learned (as in a retrospective).

### Does delaying dates signify failure?

Swarm evaluators are responsible for determining if a swarm is ‘dragging on’ without progress, or if delays are justified. Use health checks and frank discussion along the way on how to improve.

### What is the difference between success metric and exit criteria?

Exit Criteria example: Launch new onboarding UI flow

Success Metric example: Onboarding conversion rate. Target >30% full funnel.

If in the above example, the new onboarding flow doesn’t improve the conversion rate. The swarm should then disband, and create a new swarm to tackle the problem from a different angle.

One example of measuring success is to ensure there are Mixpanel events setup/created that will aid in measuring the impact of the swarm.

### What does it mean for a swarm to be well-defined?

- It contains clear success metrics and exit criteria

- Roles are clear and enough people are committed to it - a single swarm lead, a designated evaluator and a minimum of three contributors

- It has a clear description of future iteration (updated each Town Hall)

### How can I make sure there’s a regular cadence to my swarm?

It’s up to you! It's often useful to keep a public Google Doc with meeting notes that can be referred to later. As a swarm leader, it can be helpful to get someone else to act as note taker while you drive the agenda. This note taker role can also rotate among participants in the swarm.

Another idea is to record decisions more formally, a la Architecture Recording Decisions.

### Who can merge an idea?

Anyone, as soon as they feel the idea has gone through sufficient review by the
relevant people.

### What does it mean for an idea to be approved and merged as a draft?

It just means an initial draft looks good enough to continue working on. It
doesn't mean the idea is necessarily ready to start yet.

### When does an idea turn into progress?

Once the idea is well-defined, create an updated PR that reflects this state
change and get buy-in from relevant peers. This depends on the specifics of the
idea.

For user facing swarms, this means PM/UX(R)/Eng consensus.

### Who is the evaluator?

For core contributors: by default, swarm lead acts as the evaluator.

This role is evolving, especially in the context of #121.

### How can we ensure PRs don't drag out and stay open for too long?

General policy: 5-7 days means it is time to either merge or close a PR.

Lazy consensus mechanism: If reviewers come with feedback that isn't addressed
within a reasonable time frame, the PR can be closed. E.g.: feedback (48h) ping
(48h) => close PR.

### I have more questions / feedback

=> #swarms in Slack.

## Additional reading

- Organizational design: [https://wiki.status.im/Status\_Organisational\_Design](https://wiki.status.im/Status_Organisational_Design)

- Swarmwise book: [https://falkvinge.net/files/2013/04/Swarmwise-2013-by-Rick-Falkvinge-v1.1-2013Sep01.pdf](https://falkvinge.net/files/2013/04/Swarmwise-2013-by-Rick-Falkvinge-v1.1-2013Sep01.pdf)

- Swarmwise summary [https://docs.google.com/document/d/12Qema_8hPWuzDhqb8Tim66QZleVFcMXkVWICyH4nIT8/edit](https://docs.google.com/document/d/12Qema_8hPWuzDhqb8Tim66QZleVFcMXkVWICyH4nIT8/edit)

- EIP process: [https://github.com/ethereum/EIPs/blob/master/EIPS/eip-1.md](https://github.com/ethereum/EIPs/blob/master/EIPS/eip-1.md)
