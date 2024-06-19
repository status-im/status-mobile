#!/usr/bin/env bash
set -euo pipefail

GIT_ROOT=$(cd "${BASH_SOURCE%/*}" && git rev-parse --show-toplevel)
ADB_INSTALL_LOG_FILE="${GIT_ROOT}/logs/adb_install.log"
ADB_SHELL_MONKEY_LOG_FILE="${GIT_ROOT}/logs/adb_shell_monkey.log"

# Generate android debug build.
export BUILD_ENV=debug
export BUILD_TYPE=debug
export BUILD_NUMBER=99999
export ANDROID_ABI_SPLIT=false
export ANDROID_ABI_INCLUDE="armeabi-v7a;arm64-v8a;x86;x86_64"
"${GIT_ROOT}/scripts/build-android.sh"

# Install the APK on running emulator or android device.
installAndLaunchApp() {
  adb install -r ./result/app-debug.apk > "${ADB_INSTALL_LOG_FILE}" 2>&1
  "${GIT_ROOT}/scripts/wait-for-metro-port.sh" 2>&1
  # connected android devices need this port to be exposed for metro
  adb reverse "tcp:8081" "tcp:8081"
  adb shell monkey -p im.status.ethereum.debug 1 > "${ADB_SHELL_MONKEY_LOG_FILE}" 2>&1
}

showAdbLogs() {
  cat "${ADB_INSTALL_LOG_FILE}" >&2;
  cat "${ADB_SHELL_MONKEY_LOG_FILE}" >&2;
}

trap showAdbLogs EXIT ERR INT QUIT
installAndLaunchApp &
exec "${GIT_ROOT}/scripts/run-metro.sh" 2>&1
