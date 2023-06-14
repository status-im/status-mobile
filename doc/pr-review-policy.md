## Pull Request Review Policy

This document details the pull request policy of the Status Mobile repository / team.

- [Terms](#terms)
- [Policy](#policy)
  - [Overview](#overview)
  - [Full Policy](#fully-policy)
- [Rationale](#rationale)
  - [Front pressure and Demand control](#front-pressure-and-demand-control)
  - [Back pressure and Supply control](#back-pressure-and-supply-control)
  - [Honour System](#honour-system)
- [Review Support](#review-support)
- [Background](#background)

### Terms:

| Term                 | Definition                                                                                                    |
|----------------------|---------------------------------------------------------------------------------------------------------------|
| **Code owner**       | The team member submitting a pull request.                                                                    |
| **Reviewer**         | A team member asked by a code owner to review the code owner’s pull request.                                  |
| **Review bandwidth** | The number of reviews a team member has self-limited themselves to giving within their defined review period. |
| **Review period**    | The period of time over which a reviewer’s bandwidth is consumed                                              |

### Policy:

#### Overview

- 🔀 - You are responsible for getting your PR reviewed and approved.
- ✅ - You must give a review on a PR if you are asked to do so, unless your review limit has been reached.
- ✨ - The honour system underpins this policy, without acting in good faith it will not work.

#### Fully policy

1) The code owner has the responsibility for getting a PR reviewed and approved.
   1) The code owner has the right and responsibility to pursue code reviews.
   2) The code owner has the right and responsibility to make multiple requests of the same reviewer.
   3) The code owner will select a limited number of reviewers to review any of the code owner’s PRs.
2) Reviewers are obligated to give a review for all PRs they are requested to review, except when their review bandwidth has been exhausted:
   1) Reviewers have the right to refuse a code owner a review if the reviewer’s review bandwidth has been exhausted.
   2) Reviewers are expected to manage and track their review bandwidth.
   3) Reviewers can manage their review bandwidth via any means they feel works best for them. Examples include:
      1) Using the GitHub busy feature
      2) Declining a review on the PR
      3) Declining a review when pursued by a code reviewer
      4) Proactively declining to review a PR
3) All team members will set a review bandwidth that is reasonable.
4) All team members acknowledge that giving reviews is a requirement of membership of the team.
5) All team members will manage their review bandwidth honestly.
   1) The right to refuse a PR review is only eligible if a reviewer’s review bandwidth has actually been exhausted.
   2) Breaching the team’s honour is very bad form. Do not do it (please).
6) All team members acknowledge that without acting with honesty this review process will not work and jeopardises their own future ability to receive reviews.

### Rationale

#### Front pressure and Demand control

Foremost in the rationale for the above policy is that giving a PR review is a requirement. If a code owner requests
 a PR review of a team member, the reviewer is now obligated to review the PR within a reasonable timeframe.

This principle enforces that there is a pressure and demand that PR reviews must be given.

#### Back pressure and Supply control

Front pressure alone is not enough to balance the PR review load across the team. Because of this an exception is granted 
to reviewers that have exhausted their review bandwidth, the exemption permits these reviewers to refuse a PR review.

This principle gives the reviewer pool back-pressure, relieving the exhausted reviewer and increasing pressure / demand on 
the other reviewers.

#### Honour system

The two principles of demand and supply control are underpinned by an honour system and are useless without honest action.
If a team member does not honestly manage their review bandwidth this action seriously damages the collective balance of 
PR review load. Because of this potential damage a breach of the honour system is considered a deeply shameful act.

The honour system is the mechanism of control and enforcement, without it the policy would need to be controlled and enforced
using less dynamic and forgiving mechanisms. An example is a GitHub review counting tools with quotas and metrics. So just
be cool.

### Review Support

Sometimes reviewing is time-consuming or generally painful, to help make reviews as simple as possible consider the following suggestions: 
- Paired reviews: Ask an experienced reviewer to review with you.
- If a PR is the product of multiple developers only ask 1 or 2 other developers for their review.

### Background

The contents of this policy document is the product of 2 councils of the Status Mobile team, the below documents summarise the topics and outcomes of the councils:  

- [2023-05-15 - Council of Deadwater](https://docs.google.com/document/d/1TtUZCke2OzumjdWGu6TIECHEOUPwvaLrvQ8Re2nEGCo)
- [2023-06-13 - Mauve Council of Deadwater](https://docs.google.com/document/d/19hfr8xXrShLk9ROdQfOwb9_vwbTah9nOCqKSLIrgoSI)

The aims of the council were to allow developers that are relied on heavily for reviews to better balance their time 
between reviews and other responsibilities. Equally, the council aimed to give other team members better opportunity and 
understanding that giving PR reviews is a requirement and will help the team generally if more review burden is shared.
