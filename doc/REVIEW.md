This document MUST be updated everytime a PR has been identified has the source of a bug in develop. The post mortem analysis MUST include: the CAUSE of the bug, the RESOLUTION PROCESS and PREVENTION MEASURES.


# "sql: no rows in result set" error on iOS login

## Cause

There were errors in the iOS native code:
- "loginWithKeycard" method was not implemented
- "createAndLoginWithKeycard" method was actually used instead of "createAndLogin** which is used for regular account creation without keycard and doesn't have the same final argument

QA process was focused on account creation and login with keycard, and this bug would only appear specifically on iOS when creating an account without keycard and only during the next login. It was therefore a more complex path to reach than what could have been expected from this PR, and it was caused in develop by a developer working on iOS simulator because that is a common path during development.

## Resolution process

- the commit was reverted locally by the developer which fixed the issue localy and confirmed the faulty commit
- the bug was only reproducible on iOS which strongly hinted at a potential issue with native code
- the missing method was not the origin of the problem but was found at this point and further analysis lead to the shadowing error

## Prevention measures

- PRs that change native code should be checked for platform parity:
  - same methods are created/modified for each platform
  - test cases in the PR should take these methods in consideration

# Missing labels for en translation

https://github.com/status-im/status-react/issues/9003

## Cause

Unused labels in the application have been listed by QA and an issue created for their removal. The PR was made by an external contributor and it had to be rebased many time because it touched many files and other PRs got merged before this one could be tested. After a last rebase the PR was merged with 100% e2e test but no manual testing.

The reason some translations got deleted despite the fact that they were used is because the `message status` related labels were not explicitly mentioned in the code unlike every other labels. They were built from the actual `message status` and the prefix `status-`.

## Resolution process

The mistake was quickly noticed in develop and an issue created. The fix was to recover the 3 labels that were accidentally deleted. 

## Prevention measures

The code has been changed so that the labels are no longer generated and appear in the code like other labels.
