---
id: 167-automated-testing
title: Functional automated testing as a part of Continuous Delivery process
status: Active
created: 2018-04-12
category: core
lead-contributor: antdanchenko
contributors:
    - antdanchenko
    - yevh-berdnyk
    - lukaszfryc
    - Serhy
    - asemiankevich
exit-criteria: yes
success-metrics: yes
clear-roles: yes
future-iterations: yes
roles-needed:
---

## Preamble

    Idea: 167
    Title: Functional automated testing as a part of Continuous Delivery process
    Status: In Progress
    Created: 2018-04-12

## Summary
For automated testing to be a part of CD process we need to:
- increase coverage
- increase reliability
- provide visible results on daily basis

## Swarm Participants
- Lead Contributor: @antdanchenko
- Testing & Evaluation: @antdanchenko
- Contributor: @yevh-berdnyk
- Contributor: @lukaszfryc
- Contributor: @Serhy
- Contributor: @asemiankevich
- PM:

## Product Overview
Extended set of automated tests which will run against each nightly build in order to provide results via TestRun in TestRail + short summary via Slack notification
As result during nightly build testing a tester should execute only those tests which are not covered by automation in the same TestRun
In case of failure in automated test a tester should add an appropriate status to the test (see `An option to review results should be added for each test via adding 4 statuses to TestRail` in `Product Description` section)
Actual sanity test case which is running against each PR will keep running without changes


### Product Description
- Each automated test suite should be based on existing set of cases from TestRail for tracking percents of functional test coverage
- Each automated test should be the reflection of a test case from TestRail
- Results of each automated test from a build should be provided via TestRun in TestRail and linked to the appropriate case
- An option to review results should be added for each test via adding 4 statuses to TestRail:
  - feature changed (for changed product behavior)
  - known bug (link to the issue)
  - false failure (bug in test)
  - infrastructure issue

### Minimum Viable Product
Goal Date: 2018-05-01

- New test suite is add to TestRail (`Functional tests for Nightly Build`)
- 80% of test cases for nightly build test suite from TestRail are covered by automated tests
- Extended set of automated tests is running against each nightly build
- Detailed test results are provided in TestRail via TestRuns + short summary via Slack notification

Testing Days required: 10

## Success Metrics
- 80% of functional test cases in `Functional tests for Nightly Build` are covered
- not more than 10% of tests are failing due to an issue with a test
- results are visible and understandable for all status-react team members

## Exit criteria
Extended set of automated tests covers 80% of functional nightly test cases from TestRail
The set is running against each nightly build
Results are visible in TestRail as TestRuns, also summary is visible in Slack notifications

## Copyright
Copyright and related rights waived via [CC0](https://creativecommons.org/publicdomain/zero/1.0/).
