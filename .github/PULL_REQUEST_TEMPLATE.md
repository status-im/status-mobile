[comment]: # (Please replace ... with your information. Remove < and >)
[comment]: # (To auto-close issue on merge, please insert the related issue number after # i.e fixes #566)

If you submit PR for issue with bounty then write here Fixes #NN where NN is issue number

*otherwise*

fixes #...

### Summary

[comment]: # (Summarise the problem and how the pull request solves it)
...

<!-- (Optional, remove if no changes to documentation) -->
Documentation change PR (review please): https://github.com/status-im/status.im/pull/xxx

### Review notes
<!-- (Optional. Specify if something in particular should be looked at, or ignored, during review) -->

### Testing notes
<!-- (Optional) -->

#### Platforms
<!-- (Optional. Specify which platforms should be tested) -->

- Android
- iOS
- macOS
- Linux
- Windows

#### Areas that maybe impacted
<!-- (Optional. Specify if some specific areas has to be tested, for example 1-1 chats) -->

##### Functional

- 1-1 chats
- public chats
- group chats
- wallet / transactions
- dapps / app browsing
- account recovery
- new account
- user profile updates
- networks
- mailservers
- fleet
- bootnodes

##### Non-functional

- battery performance
- CPU performance / speed of the app
- network consumption

### Steps to test
<!-- (Specify exact steps to test if there are such) -->

- Open Status
- ...
- Step 3, etc.

<!-- (PRs will only be accepted if squashed into single commit.) -->

### Before and after screenshots comparison

| Figma (if available) | iOS (if available)    | Android (if available)
| --- | --- | --- |
| Please embed Image/Video here of the before and after.  | Please embed Image/Video here of the before and after.  | Please embed Image/Video here of the before and after. |

status: ready <!-- Can be ready or wip -->

<!-- Uncomment this section for status-go upgrade/dogfooding pull requests

- Specify potentially impacted user flows in _Areas that maybe impacted*.
- Ensure that _Steps to test_ is filled in.

### Risk

Described potential risks and worst case scenarios.

Tick **one**:
- [ ] Low risk: 2 devs MUST perform testing as specified above and attach their results as comments to this PR **before** merging.
- [ ] High risk: QA team MUST perform additional testing in the specified affected areas **before** merging.


-->
