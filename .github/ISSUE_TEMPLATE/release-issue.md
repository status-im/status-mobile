---
name: Release umbrella issue
about: Release Issue
title: 'Mobile release '
labels: release
assignees: ''

---
Issue to manage mobile release <version>

### Scope
[comment]: # (Add milestone from github)
https://github.com/status-im/status-react/milestone/

- release notes: 
- release branch: 


### Testing info 
[comment]: # (TestRail info, added by QA)
Main test suite: 
Upgrade (e2e): 

### Builds in TestFlight and PlayStore to push as a release
[comment]: # (Will be added by QA after testing approving of build)

### Process
#### 1) Kick-off
- [ ] 1. Translation PRs is merged before cutting branch (Jinho)
- [ ] 2. Prepare scope of visible changes based on commits history, started from previous release branch cut ([guide](https://notes.status.im/how-to-prepare-release-notes))
- [ ] 3. Update [RELEASES.md](https://github.com/status-im/status-react/blob/develop/RELEASES.md) with release notes (make PR based on previous step)
- [ ] 4. Create release branch using this [guide](https://github.com/status-im/status-react/blob/develop/doc/RELEASE_GUIDE.md), bump VERSION (merge created PR ([example](https://github.com/status-im/status-react/pull/12504) ) to develop) and get successful release builds (so, testing can be started)
- [ ] 5. Based on release scope, ask for comms (Jonny) 
- [ ] 6. Make sure that assets (screenshots, video) in stores are up to date. If should be updated, ask @cammellos  for help

#### 2) Testing
[comment]: # (Section will be maintained by QA)
- [ ] Dogfooding (add notes, if it was held) 
- [ ] Regression testing for release candidate build
- [ ] Upgrade tests
- [ ] Fix known blocker(s) found during regression testing

##### Blockers
[comment]: # (Section for blocker issues; should be fixed and merged by release-manager. Remove if no blockers found)
- [ ] 1 blocker:

=============================================================================================

- [ ] Cherry-pick fix(es) to the release branch
- [ ] Verify fix(es) on new release candidate build
- [ ] Retest
- [ ] Keep release build forever (button in Jenkins)

#### 3) Submission

- [ ] 1. Release notes written and added (500 character limit for updates (Android) and PR for [RELEASES.md](https://github.com/status-im/status-react/blob/develop/RELEASES.md) is ready to be merged (from 1.3 step)
- [ ] 2. Submit beta version for Android (Open Testing in Play Store)
- [ ] 3. Submit iOS build to Apple review ([instruction](https://drive.google.com/file/d/10Cl7PBB7TFPkZiVbfzdFGpfMRP9bxXuq/view?usp=sharing), be careful - low quality of audio)
- [ ] 4. Play store content reviewed and updated
- [ ] 5. Privacy policy reviewed and updated (ask in channel)
- [ ] 6. Countries to be excluded (ask in channel) 
- [ ] 7. Changes to our FAQ made (should be checked and if any changes needed, ask Jakub to made them)
- [ ] 8. Prepare PR for F-droid build ([instruction]( https://github.com/status-im/status-react/blob/develop/doc/FDROID.md))

#### 4) Before publishing
- [ ] 1. Translations for comms available (marketing, ask in the channel)
- [ ] 2. Promotional content ready (marketing, ask in channel)
- [ ] 3. Upload builds for DO store,  https://dev.status.im/ (Jakub)

#### 5) At time of publishing
- [ ] 1. Notify marketing to push comms and blog post (Jonny + get review on blog from QA team)
- [ ] 2. Publish release to app store and playstore
- [ ] 3. Final push to update prod site https://status.im/,  [instructions](https://github.com/status-im/infra-docs/blob/master/articles/status_release_upload.md) (Jakub - requires credentials to upload) 
- [ ] 4. Add the link to the post about the release and publish the release: https://github.com/status-im/status-react/releases (Jakub)
- [ ] 5. Merge PR for updating [RELEASES.md](https://github.com/status-im/status-react/blob/develop/RELEASES.md) (from 1.3 step)

