# Description

This directory is the destination of logs created by Fastlane and React Native Xcode script.

# Logs

* `react-native-xcode.log` - Output from `node_modules/react-native/scripts/react-native-xcode.sh`.
  - Created by redirecting output of `shellScript` in `ios/StatusIm.xcodeproj/project.pbxproj`.
* `Status PR-StatusImPR.log - Created by [Fastlane Gym](https://docs.fastlane.tools/actions/gym/).
  - Configred via [`Fastfile`](../Fastfile) using `buildlog_path` argument for `build_ios_app`.

# CI

These log files are uploaded to Jenkins as CI job artifacts on failure using `archiveArtifacts`.
