#!/usr/bin/env bash
set -euo pipefail
set -m # needed to access jobs

GIT_ROOT=$(cd "${BASH_SOURCE%/*}" && git rev-parse --show-toplevel)
XCRUN_INSTALL_LOG_FILE="${GIT_ROOT}/logs/xcrun_install.log"
XCRUN_LAUNCH_LOG_FILE="${GIT_ROOT}/logs/xcrun_launch.log"

# Install on the simulator
installAndLaunchApp() {
  xcrun simctl install "$UUID" "$APP_PATH" > "${XCRUN_INSTALL_LOG_FILE}" 2>&1
  "${GIT_ROOT}/scripts/wait-for-metro-port.sh"  2>&1
  xcrun simctl launch "$UUID" im.status.ethereum.debug > "${XCRUN_LAUNCH_LOG_FILE}" 2>&1

}

showXcrunLogs() {
  cat "${XCRUN_INSTALL_LOG_FILE}" >&2;
  cat "${XCRUN_LAUNCH_LOG_FILE}" >&2;
}

# Check if the first argument is provided
if [ -z "${1-}" ]; then
    echo "Error: No simulator name provided." >&2
    exit 1
fi

SIMULATOR=${1}

# get our desired UUID
UUID=$(xcrun simctl list devices | grep -E "$SIMULATOR \(" | head -n 1 | awk -F '[()]' '{print $2}')

# get simulator status
SIMULATOR_STATE=$(xcrun simctl list devices | grep -E "$SIMULATOR \(" | head -n 1 | awk '{print $NF}')

# sometimes a simulator is already running, shut it down to avoid errors
if [ "$SIMULATOR_STATE" != "(Shutdown)" ]; then
    xcrun simctl shutdown "$UUID"
fi

# boot up iOS for simulator
xcrun simctl boot "$UUID"

# start the simulator
open -a Simulator --args -CurrentDeviceUDID "$UUID"

BUILD_DIR="${GIT_ROOT}/build"

#iOS build of debug scheme
xcodebuild -workspace "ios/StatusIm.xcworkspace" -configuration Debug -scheme StatusIm -destination id="$UUID" -derivedDataPath "${BUILD_DIR}" | xcbeautify

APP_PATH="${BUILD_DIR}/Build/Products/Debug-iphonesimulator/StatusIm.app"

trap showXcrunLogs EXIT ERR INT QUIT
installAndLaunchApp &
exec "${GIT_ROOT}/scripts/run-metro.sh" 2>&1
