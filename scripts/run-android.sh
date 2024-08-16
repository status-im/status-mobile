#!/usr/bin/env bash
set -euo pipefail

GIT_ROOT=$(cd "${BASH_SOURCE%/*}" && git rev-parse --show-toplevel)
ADB_INSTALL_LOG_FILE="${GIT_ROOT}/logs/adb_install.log"
ADB_SHELL_MONKEY_LOG_FILE="${GIT_ROOT}/logs/adb_shell_monkey.log"

# Generate android debug build.
export ANDROID_ABI_INCLUDE=$("${GIT_ROOT}/scripts/adb_devices_abis.sh")
export BUILD_ENV=debug
export BUILD_TYPE=debug
"${GIT_ROOT}/scripts/build-android.sh"

# Install the APK on running emulator or android device.
installAndLaunchApp() {
  adb install -r ./result/app-arm64-v8a-debug.apk > "${ADB_INSTALL_LOG_FILE}" 2>&1
  "${GIT_ROOT}/scripts/wait-for-metro-port.sh" 2>&1
  # connected android devices need this port to be exposed for metro
  adb reverse "tcp:${RCT_METRO_PORT}" "tcp:${RCT_METRO_PORT}"
  adb shell monkey -p im.status.ethereum.debug 1 > "${ADB_SHELL_MONKEY_LOG_FILE}" 2>&1
}

showAdbLogs() {
  cat "${ADB_INSTALL_LOG_FILE}" >&2;
  cat "${ADB_SHELL_MONKEY_LOG_FILE}" >&2;
}

trap showAdbLogs EXIT ERR INT QUIT
installAndLaunchApp &
exec "${GIT_ROOT}/scripts/run-metro.sh" 2>&1
