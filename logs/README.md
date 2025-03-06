# Description

This directory is the destination of logs created during build time of debug builds and logs created by Fastlane on CI.

# Logs

* `xcrun_device_install.log` - Output from `status-mobile/scripts/run-ios-device.sh`.
    - Created by redirecting output of `xcrun simctl install "$UDID" "$APP_PATH"`.
* `xcrun_device_process_launch.log` - Output from `status-mobile/scripts/run-ios-device.sh`.
    - Created by specifying `--json-output` flag for `xcrun devicectl device process launch --no-activate --verbose --device "${DEVICE_UUID}" "${INSTALLATION_URL}"`.
* `xcrun_device_process_resume.log` - Output from `status-mobile/scripts/run-ios-device.sh`.
    - Created by redirecting output of `xcrun devicectl device process resume --device "${DEVICE_UUID}" --pid "${STATUS_PID}"`.
* `adb_install.log` - Output from `scripts/run-android.sh`.
    - Created by redirecting output of `adb install -r ./result/app-debug.apk`.
* `adb_shell_monkey.log` - Output from `status-mobile/scripts/run-android.sh`.
    - Created by redirecting output of `adb shell monkey -p im.status.ethereum.debug 1 >`.
* `ios_simulators_list.log` - Output from `status-mobile/scripts/run-ios.sh`.
    - Created by redirecting output of `xcrun simctl list devices -j`.

# CI

These log files are uploaded to Jenkins as CI job artifacts on failure using `archiveArtifacts`.

* `react-native-xcode.log` - Output from `node_modules/react-native/scripts/react-native-xcode.sh`.
  - Created by redirecting output of `shellScript` in `ios/StatusIm.xcodeproj/project.pbxproj` for "Bundle React Native code and images" build Phase.
* `set_xcode_version.log` - Output from `node_modules/react-native/scripts/react-native-xcode.sh`.
  - Created by redirecting output of `shellScript` in `ios/StatusIm.xcodeproj/project.pbxproj` for "Run Script" build Phase.
* `Status PR-StatusImPR.log - Created by [Fastlane Gym](https://docs.fastlane.tools/actions/gym/).
  - Configured via [`Fastfile`](../Fastfile) using `buildlog_path` argument for `build_ios_app`.
