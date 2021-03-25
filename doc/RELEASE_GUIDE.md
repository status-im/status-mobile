# Description

This document explains some of the steps that are involved in relseases.

## PlayStore Metadata Updates

You can update Play Store releae metadata using `fastlane android upload_metadata`.

But that requires credentials necessary for accessing Play Store API. The simpler way is to edit files contained within [`fastlane/metadata`](metadata) and run the following CI job:

https://ci.status.im/job/status-react/job/tools/job/update-playstore-metadata/

# Release process

## Coordination

All the coordination is done in the #release channel on discord.

## Using the script

You can use the script to cut a release branch:

`scripts/cut-release.sh`

This will create a branch:

`release/a.b.x`

Create a branch:

`chore/update-release-to-$version`

And if you have the `gh` command installed, it will automatically create a PR for you.

## Manual steps

### Create a branch

First we need to create a release branch cut from develop.
The name of the branch should be `release/a.b.x` where `x` is actually `x`, not 
a variable, and `a` and `b` are the `MAJOR` and `MINOR` version of the app.

For example, valid branch names are:

`release/1.12.x` or `release/3.12.x`


### Update version

We need to update the `VERSION` file to reflect the release we want to build.


## Building 

Jenkins will automatically build this branch and create release candidates.
Those will go through the QA process and the QA team will let us know when
tested successfully.

## Bugfixes before app is released

Because both `status-react` and `status-go` might have newer commits that we don't 
want to include, bug fixes should be applied separately to develop and the release branch.
The best workflow is to send a PR to develop, get it merged and the cherry-pick on the
release branch.

If that's not practical for some reason (i.e develop has diverged significantly from release) 
other workflows are possible.

In order to change status-go, a similar workflow applies.
First checkout the tag used in `status-go-version.json`.

Create a branch on status-go named: `release/vx.y.z+hotfix.w` where

`x` is the `MAJOR` version
`y` is the `MINOR` version
`z` is the `PATCH` version
`w` is the `HOTFIX` version

For example valid names are:

`release/v0.62.3+hotfix.2`
`release/v1.3.4+hotfix.4`

Once the branch is ready to use and tested successfully, tag the branch:

`git tag v0.62.3+hotfix.2` 

and push the tag to origin:

`git push origin v0.62.3+hotfix.2`

Once that's done, update the status-react branch with the new tag:

`make shell`

`scripts/update-status-go.sh v0.62.3+hotfix.2`

And commit and push the changes

# Hotfix process

The hotfix process is basically identical to the workflow above:

1) Add the changes on status-react
2) Make sure you use a hotfix branch for status-go (unless we are happy to release from develop)
3) Update the `VERSION` in status-react
