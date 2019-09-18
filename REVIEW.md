This document MUST be updated everytime a PR has been identified has the source of a bug in develop. The post mortem analysis MUST include: the CAUSE of the bug, the RESOLUTION PROCESS and PREVENTION MEASURES.


# "sql: no rows in result set" error on iOS login

## Cause

There was errors in the iOS native code:
- "loginWithKeycard" method was not implemented
- "createAndLoginWithKeycard" method was actually used instead of "createAndLogin** which is used for regular account creation without keycard and doesn't have the same final argument

QA process was focused on account creation and login with keycard, and this bug would only appear specifically on iOS when creating an account without keycard and only during the next login. It was therefore a more complex path to reach that what could have been expected from this PR, and it was caused in develop by a developer working on iOS simulator because that is a common path during development.

## Resolution process

- the commit was reverted locally by the developer which fixed the issue localy and confirmed the faulty commit
- the bug was only reproducible on iOS which strongly hinted at a potential issue with native code
- the missing method was not the origin of the problem but was found at this point and further analysis lead to the shadowing error

## Prevention measures

- PRs that change native code should be checked for plateform parity:
  - same methods are created/modified for each platform
  - test cases in the PR should take these methods in consideration
