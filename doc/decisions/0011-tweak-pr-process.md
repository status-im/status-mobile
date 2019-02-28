# 0011. Tweak PR process

| Date | Tags |
|---|---|
| 2019-02-26 | process, pull-requests |


## Status

accepted

## Context

There was a generally dissatisfaction with our PR flow process from multiple stakeholders, including devs, QA and design. These largely centered around size, speed of integration and quality of PRs.

For more details, please see pain points in the meeting notes held end of February here: https://notes.status.im/C5pj8g7gQOu9Wo8PtDZsMw?edit# as well as the preceeding Discuss thread: https://discuss.status.im/t/better-pull-requests-process/1044

Also see conversations in Core Dev Call #12 and #13: https://github.com/status-im/pm/

## Decision

These specific policy changes were agreed upon (a la 'rough consensus' model). These are heuristics, and should be more of a guide than strict rules. Exception to these should be minimal though.

**1. Reject PRs that don't have tests**
As a rule of thumb, coverage should increase.

**2. Don't use or promote nightly as something for end users.**

**3. Reject PRs that don't write what tests the author have done and what platforms you used to test, including screenshots, videos or logs**
This means QA and dev have to spend less time catching obvious problems.

**4. Run tests when a PR is at "Review" stage on https://github.com/status-im/status-react/projects/7 (or add new 'e2e stage' between review and `TO TEST`)**

**5. Dev asks for 2-3 reviewers, including a designer if the change affects UI, and these reviewers review within a day.**
This ensures we don't have *diffusion of responsibility* and that PRs are reviewed quickly.

## Consequences

This means we need to some form of release schedule up for Desktop. It also means we need to standardize tests, at least on Clojure side.

Additionally, the following two medium term projects can't be fixed immediately but will be started:

- Look into integration tests, headless tests. Andrea is looking into.
- Look into iOS automated test and more platform. Anton will look into.

Follow up on this will happen at a later Core Dev call.
