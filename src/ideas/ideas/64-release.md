---
id: 64
title: Release swarm for continuous delivery
status: Aborted
created: 2017-12-27
category: core
lead-contributor: oskarth
contributors:
    - oskarth
    - annadanchenko
    - asemiankevich
    - rasom
    - adambabik
    - andytudhope
    - pablanopete
    - chadyj
    - jeluard
    - yenda
    - sergii
    - flexsurfer
    - goran
    - lukaszfryc 
    - churik
exit-criteria: no
success-metrics: yes
clear-roles: yes
future-iterations: no
roles-needed:
---

## Preamble

    Idea: PROC064
    Title: Release swarm for continuous delivery
    Status: Aborted
    Created: 2017-12-27
    Started: 2018-01-04
    Ended: 2018-03-22

## Summary
A swarm dedicated to making sure we do regular releases, so we keep improving the app for end users and other core contributors aren't needlessly distracted by release work. A cadence for the team.

A secondary goal is to improve the release process when it comes to automation, changelogs, testing, and having more people who can do this.

## Swarm Participants

- Lead Contributor: @oskarth (10h/w)
- Testing & Evaluation: @anna (20h/w) @asemiankevich (20h/w)
- Clojure Contributor: @roman (10h/w)
- QA Automation Contributor: @antdanchenko (15h/w January)
- Go Contributor: adambabik (10h/w+)
- Community/marketing: @andytudhope (5h/w)
- Community/marketing: @pablanopete (5h/w)
- PM: @chad
- Clojure Contributors: Julien, Eric
- Testing & Evaluation: Sergii

Swarm size: estimated 4-10 people.

## Product Overview

Regular releases are like exercise, it's easy to get out of shape. In the last six months or so we have been quite bad at this. Each release has dragged out on time, and we have compromised release dates for scope creep. Getting back onto a regular release schedule is in the interest of many groups of people, as can be seen in the user stories below.

### User stories

1. As a developer, I want to not have to worry about when a release is going to happen and just keep working so I can be more productive.

2. As a swarm leader, I want releases to be in some cadence so I can somewhat plan iterations in relating to releases.

3. As a PM, I want releases to happen on a clockwork so I can see clear progress being made on a reasonable time frame.

4. As someone responsible for releases, I want to have an established procedure for releases, with as much automation and instructions as possible, so I can either do less work or someone else can do this, and I can focus on more important things.

5. As a tester, I want to have a process for when to do thorough testing and establish procedures for quickly testing release candidates.

6. As an end user, I want to use the latest version of Status so I can get benefits and give feedback early.

7. As a developer, I want to have regular releases so I feel motivated by the prospects of things I have worked on being included.

This is a process and a cross-functional team we are introducing. Once a few releases have gone by we expect this process to be considered "installed" and the group will continue to exist.

### Requirements & Dependancies


- 1) Swarm autonomy to decide on scope of release, including when to cut release branch, revert commits, etc. Releases aren't based on scope but on dates, i.e. scopes will likely be cut.

- 2) Account details for Google Play Store and Apple equivalent.

- 3) Bug hunting day at appropriate timing to release branch.

- 4) Release branch cut from develop ~1w before release and only bug fixes are cherry-picked to this.

- 5) Known availablity of dev/QA since iterations are short and we currently have some SPOF.

### Minimum Viable Product

Goal Date: 2017-01-14 (0.9.13)
Actual end date: 2017-01-30 (2w delayed due to regressions)
Description: Main deliverable: First release. Additionally: Cut release branch one week before on appropriate day. Oskar doing release with help from Roman and Anna as they have both done this. Develop rudimentary functional spec, i.e. supported user stories that are tested. Parallel track is decentralizing testing of these releases too. TBD and up to Anna. Start writing checklist for wiki;
start thinking about automation; setup Jenkins RC build; release notes to hutch/Chad.

END: All done except: Develop rudimentary functional spec, i.e. supported user stories that are tested. Parallel track is decentralizing testing of these releases too.

### Iteration 1

Goal Date: 2017-02-15
Actual goal date: 2017-02-22

Description: Release 0.9.14 out and cut by Eric (3rd person). Automated test: Successful build of PR will trigger automated tests on each PR and post results in comment in Github. (Also possibly after each merge to develop). User flows: 10-20 specified user stories.

Didn’t do: Fastlane Jenkins setup pipeline (auto build Android/iOS).

### Iteration 2
Goal Date: 2017-03-01

Initial Description: Once these 3 releases have been done, hopefully reasonably according to plan, this idea can be put as closed and swarm continues to exist in background. At some point we might want to get a release engineer to off-load some routine work.

Updated Description: Release 0.9.15 out and cut by Julien (4th person). Fastlane Jenkins setup pipeline (auto build Android/iOS) - Andy. Automated test: TBD. User flows: TBD.

Update Mar 1: Iteration 2 delayed due to x2 release blockers that @goranjovic is currently working on. Since offsite is next week 0.9.15 is likely to be delayed until after (15/3, say). This might change if we get good next before EOD. Fastlane to be looked at IRL during offsite due to key/Jenkins access.

Update Mar 23: 0.9.15 released a few days ago: https://blog.status.im/status-alpha-0-9-15-release-notes-63c41eb90eac

Shutting down this swarm per https://status-im.slack.com/archives/C8KB9DR60/p1521009678000295 (internal). Additionally, no clear exit criteria which makes this an infinite loop and less of a swarm. Post-mortem TBD.

## Success Metrics

- 1) Releases happen on a known cadence, like a clockwork.

- 2) A process involving as few people as possible to ensure most core contributors keep shipping.

- 3) Automation, checklists etc which minimizes work required and uncertainty for a release.

- 4) Release work is largely a decentralized capability and doesn't depend on a SPOF (Roman, Anna).


## Exit criteria

Not clearly specified, hence aborting. Informally: When three releases done.

## Supporting Role Communication

- Marketing / PM / Community.

## Post-mortem

### What went well?
 -We actually started doing releases on continuous basis
- Improved a lot since the 1st release (release communication was well prepared, lot of bugs fixed)
- Release process included all key stakeholders (devs, QA, product, marketing)
- Release process was clarified (managing scope, branching, fixing bugs)
- Automated tests for PRs

### What could’ve gone better?
- Automate creating iOS build (without need of Roman) / fastlane integration
- External weekly 
- Do Bug Fixing Days more often
- Do final QA checks before creating release branch
- Have a place that list down all things that will be in a release. Make sure everyone uses it
- Improve RELEASE column in Pipeline QA?
- Increase coverage of e2e automated tests
- Do root cause analysis for regression bugs
- Release more consistently (goal was to release every 2 weeks)

## Copyright
Copyright and related rights waived via [CC0](https://creativecommons.org/publicdomain/zero/1.0/).
