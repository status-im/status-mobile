# Description

This document explains some of the steps that are involved in releases.

# Release process

## Coordination

All the coordination is done in the `#release` channel on discord.

Release readiness can be tracked by watching an appropriate milestone at [milestones](https://github.com/status-im/status-mobile/milestones) section in github.

When release is building you can check the progress in a testrail. Ask @churik for access if you don't have it

## Preparing branches
Special branches should be created at the start of a release process. That can be done manually or with the script

### Prepare branches with the script

You can use the script to cut a release branch. No arguments needed, the new release version will be deducted automatically:

`scripts/cut-release.sh`

Upon script call two branches will be created:
- `release/a.b.x`
- `chore/update-release-to-$version`

The first one is the main release branch. All bugfixes should be cherry-picked from `develop` to it.  Jenkins treats that branch specially and prepares release builds from it. 

The second branch only updates the `VERSION` file and all you need to do is to merge it to `develop`. If you have the `gh` command installed, `cut-release` script will automatically create a PR for this.

### Prepare branches manually

#### Create a branch

First, we need to create a release branch cut from develop.
The name of the branch should be `release/a.b.x` where `x` is actually `x`, not a variable, and `a` and `b` are the `MAJOR` and `MINOR` version of the app.

For example, valid branch names are:

`release/1.12.x` or `release/3.12.x`

#### Update version

We need to update the `VERSION` file to reflect the release we want to build.


## Building 

Jenkins will automatically build this branch and create release candidates.
Those will go through the QA process and the QA team will let us know when
tested successfully.

## Bugfixes before an app is released

Because both `status-mobile` and `status-go` might have newer commits that we don't want to include, bug fixes should be applied separately to develop and the release branch.
The best workflow is to send a PR to develop, get it merged, and the cherry-pick on the
release branch.

It is a release manager's responsibility to cherry-pick bugfix from `develop` to release branch. In order to do this, you should be a part of a `release group` on github. Contact @jakubgs to be added.

If cherry-picking not practical for some reason (i.e develop has diverged significantly from release) 
other workflows are possible.

To change status-go, a similar workflow applies.
First checkout the tag used in `status-go-version.json` of release branch.

`git checkout v0.62.3`

Create a branch on status-go named: `release/vx.y.z+hotfix.w` where (make sure branch name is not the same as the tag)

`x` is the `MAJOR` version
`y` is the `MINOR` version
`z` is the `PATCH` version
`w` is the `HOTFIX` version

For example valid names are:

`release/v0.62.3+hotfix.2`
`release/v1.3.4+hotfix.4`

Once the branch is ready to use and tested successfully, tag the branch:

`git tag v0.62.3+hotfix.2` 

Push the branch and then the tag to origin:
`git push --set-upstream origin release/v0.62.3+hotfix.2`
`git push origin v0.62.3+hotfix.2`

Switch to status-mobile release branch, and cherry pick the commit you need.

Once that's done, update the status-mobile release branch with the new tag:

`make shell`

`scripts/update-status-go.sh v0.62.3+hotfix.2`

And commit and push the changes

# Hotfix process

The hotfix process is basically identical to the workflow of a bugfix:

1) Add the changes on status-mobile
2) Make sure you use a hotfix branch for status-go (unless we are happy to release from develop)
3) Update the `VERSION` in status-mobile

# PlayStore Metadata Updates
This section is relevant only for the marketing team, no developer participation is required.

You can update Play Store release metadata using `fastlane android upload_metadata`.

But that requires credentials necessary for accessing Play Store API. The simpler way is to edit files contained within [`fastlane/metadata`](metadata) and run the following CI job:

https://ci.status.im/job/status-mobile/job/tools/job/update-playstore-metadata/
