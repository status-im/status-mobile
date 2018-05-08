---
id: 121-sob-testing-process
title: Improve OpenBounty Testing and make it easy to contribute
status: Active
created: 2018-04-02
category: openbounty
contributors:
    - churik
    - annadanchenko
    - asemiankevich
exit-criteria: yes
success-metrics: yes
clear-roles: yes
future-iterations: yes
---

## Preamble

    Idea: 127
    Title: Improve OpenBounty Testing and make it easy to contribute
    Status: In Progress
    Created: 2018-04-02

## Summary
- make QA process in SOB team easy to understand and support; 
- in order  to reduce time spent on regression testing automate main use cases;

## Swarm Participants
- Lead Contributor:@churik
- Testing & Evaluation: @annadanchenko 
- Contributor: @asemiankevich 

## Product Overview
Full and transparent QA process in Status Open Bounty team will help to involve new people into the testing process more quickly (if needed) and provide developers with more info about what was tested in particular PR or in `develop` branch. Ideally process should be integrated with TestRail, but initially, a smoke checklist should be defined and then automated.

All regression test cases should be automated eventually in order to reduce time for SOB testing and to find issues faster.
In future would be nice to integrate GitHub pull requests with automation testing thus after PR will be built and deployed,  it will trigger end-to-end tests and publish test results in PR comment.
Hence this idea is a placeholder for current and future iterations of improving QA process in SOB team.

## Iterations

### Iteration 1 - `2018-04-30`

#### Documentation 
- [ ] Tutorial video or docs about QA process in SOB team
- [ ] Basic use cases (smoke checklist) are defined in TestRail
#### Involvement
- [ ]  one more QA can test SOB
#### Automation 
- [ ] automate PR creating, new claim check in `Activity` feed
- [ ] define function for PR merging, `Manage Payouts` check
- [ ] all tests are running successfully in [Jenkins](https://jenkins.status.im/job/end-to-end-tests/job/sob-end-to-end-tests/)

### Iteration 2 (WIP, can be modified) - `2018-05-30`
 
#### Automation (1 full test for SOB, which contains all bounty stages)
- [ ] define function for SNT transfer 
- [ ] define function for signing transaction and payout confirmation
- [ ] 1 full end-to-end test is running successfully in [Jenkins](https://jenkins.status.im/job/end-to-end-tests/job/sob-end-to-end-tests/)

## Exit Criteria
- The smoke checklist is fully automated and autotests are running through each PR which comes to `To test` column
- Test artifacts (screenshots, log etc.) are integrated with GitHub and put to PR comments

## Success Metrics
This idea should affect the following:
- Reduced timeframe for testing
- Speed up feedback provided on PRs
