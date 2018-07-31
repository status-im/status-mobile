# 0009. Mobile App Release Process

| Date | Tags |
|---|---|
| Tue Jul 24  | process, release |


## Status

accepted


## Context

Classical release approach: we cut off the release branch, stabilize it, fix every critical issue and release. Some changes are cherry-picked to the release branch.

It has a couple of downsides:

- Resources are scattered across two branches;

- Unique fixes and unique combination of commits in the release branch;

- Unpredictable release schedule (any day can be a release day!).

## The Process

We do "failable releases" approach instead, when the release either happens on
a specific day, or doesn't happen.
We aim for a weekly cadence.
But even more, we aim to be frank about the release state, give ourselves
a permission to fail a release.


### The Release Checklist
_☝️  a release blocker is a GHI with “release” tag on it_

1. Do a release testing on a nightly.
1. If it is good enough (no release blockers), cut a release branch, like `releases/0.9.23`. If there is an existing branch from the previous unsuccessful release, rebase it to the current state of `develop`.
1. 🔄 Test the release branch, fix release blockers.
1. Cherry-pick only release blockers to the release branch.
1. Check-up with other teams (@go, #core-infra).
1. Mobile releases should not happen at the same time as cluster upgrades.
1. *After* the release branch is cut 
    - update status-go on `develop` (NOT the release branch);
    - bump the app version on `develop` (NOT the release branch).
1. Update release notes, and app descriptions in GP and App Store (see [this section](#release-notes)).
1. If [“go/no go” assessment](#go-no-go) is negative (“no go”), just abandon the release branch.


### Failable Releases
Failable release philosophy:
- We track potential release blockers as early as possible (based on testing of nightlies);
- We cut off the release branch when there are no big blockers (wednesday morning the latest);
- We fix remaining release blockers on `develop` and cherry-pick fixes to the release branch;
- If we aren’t able to fix all release blockers in time™, leaving enough time for QA to thoroughly test the release, we mark this release as failed and focus on releasing next week;
- Next week we just rebase the release branch on `develop`.

### What is this failed release anyway?
1. Nothing is published to our users;
1. We don’t keep the release branch around, next week we force-rebase it to the latest state of `develop`.

### Is it bad to fail a release?
Nope. One of the nice side-effects of the failable release approach that it shows the real state of the develop branch. 
Trying too hard to release anyway might paint a picture that is better than the reality.
If there are too many failed releases, it is an indication that something is wrong with our `develop` or PR intakes, not with the release process. Don’t shoot the messenger :)

### <a name="go-no-go"></a> “Go/No-Go” decision
The “no-go” decision can be make:
1. If there is a huge blocker on develop that we are not sure we will be able to fix in time;
1. If there is no time left for QA to make thorough testing;
1. If we don’t feel confident in some critical feature of the app, even if there is no critical issues found there;
1. If one of the teams (status-go, cluster, etc) isn’t ready for this release (check with the `#core-infra` or `@go`).

### Schedule
- We aim to submit an iTC build every Friday to have time for Apple to review it.
- We aim to publish a release every Monday.

Note, that to aim is a key word there. If we fail to release in time, we just skip this week’s slot and try to release next week.

So, schedule might look like that:
```
May, 11: Release 1
May, 18: failed release, nothing is published
May, 25: failed release, nothing is published
June, 1: Release 2
...
```

So, as you can see releases happen only on Mondays.
They might or might not happen, but the schedule stays consistent.

### <a name="release-notes"></a>Release Notes
We keep the file [`CHANGELOG.md`](../../CHANGELOG.md) in the repository.
We also have an ongoing document with them.

#### iOS Test Flight Release
**Upload to AppStore Connect**
Use [this Jenkins job](https://jenkins.status.im/job/status-react/job/upload_release_ios/)

**“What to test” field**
When AppStore Connect asks you to fill in the field called “what to test”, just copy the release notes there.

**Submitting to the review**
We submit it on Friday, fix the compliance and add the group called “External Testers”. Don’t make the group name scare you, the real testers are in the group called “testflight-boarding”. 

Don’t forget to update screenshots if necessary!

Then we submit it to Apple review.

**Releasing to our beta-testers**
If reviewed successfully, we can share it to our users by adding “testflight-boarding” group to our build. As soon as it is added, invitations to upgrade are sent to our beta-testers!

### GP Release
The uploaded release is **immediately available**! 
Do it only if the iOS build is approved by Apple!

Use [this Jenkins job](https://jenkins.status.im/job/status-react/job/upload_release_android/)

Don’t forget to update the screenshots if necessary!

**App Description**
App description needs to be updated. It is much shorter than the release notes, so it is important to trim them down for GP.

### Flexing & Planning
With this approach we don’t plan features for release. We plan features for priorities.
What is the difference?
When we plan features for release, a feature A absolutely has to be included in release 0.2. That means, that if feature is not ready yet, the release 0.2 is not happening.

In features for priorities approach we plan which feature comes before or after which. Say, if we have features A, B and C and we know that A is more important than B and C then we will try to release it earlier. 

In that case releases history might look like these:
```
Week 1: 0.1 - failed
Week 2: 0.1 - feature A released
Week 3: 0.2 - polishing of feature A and bugfixes
Week 4: 0.3 - feature B
Week 5: 0.4 - feature C
...
```

So we keep releasing cadence even if there are no features to release.

### Retrospective
Each release ends with a short retrospective/planning session for the next week.



