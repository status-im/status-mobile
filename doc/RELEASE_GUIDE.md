# Description

This document explains some of the steps that are involved in relseases.

## PlayStore Metadata Updates

You can update Play Store releae metadata using `fastlane android upload_metadata`.

But that requires credentials necessary for accessing Play Store API. The simpler way is to edit files contained within [`fastlane/metadata`](metadata) and run the following CI job:

https://ci.status.im/job/status-tools/job/update-playstore-metadata/
