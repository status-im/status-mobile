#!/usr/bin/env bash
set -euo pipefail
set -m # needed to access jobs

GIT_ROOT=$(cd "${BASH_SOURCE%/*}" && git rev-parse --show-toplevel)
XCRUN_INSTALL_LOG_FILE="${GIT_ROOT}/logs/xcrun_install.log"
XCRUN_LAUNCH_LOG_FILE="${GIT_ROOT}/logs/xcrun_launch.log"
XCRUN_SIMULATOR_JSON_FILE="${GIT_ROOT}/logs/ios_simulators_list.log"

# Install on the simulator
installAndLaunchApp() {
  xcrun simctl install "$UDID" "$APP_PATH" > "${XCRUN_INSTALL_LOG_FILE}" 2>&1
  "${GIT_ROOT}/scripts/wait-for-metro-port.sh"  2>&1
  xcrun simctl launch "$UDID" im.status.ethereum.debug > "${XCRUN_LAUNCH_LOG_FILE}" 2>&1

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

# fetch available iOS Simulators
xcrun simctl list devices -j > "${XCRUN_SIMULATOR_JSON_FILE}"

SIMULATOR=${1}

# get the first available UDID for Simulators that match the name
read -r UDID SIMULATOR_STATE IS_AVAILABLE < <(jq --raw-output --arg simulator "${SIMULATOR}" '
  [ .devices[] | .[] | select(.name == $simulator) ] |
  map(select(.isAvailable)) + map(select(.isAvailable | not)) |
  first |
  "\(.udid) \(.state) \(.isAvailable)"
' "${XCRUN_SIMULATOR_JSON_FILE}")

if [ "${IS_AVAILABLE}" == false ] || [ "${UDID}" == null ]; then
    echo "Error: Simulator ${SIMULATOR} is not available, Please find and install them."
    echo "For help please refer"
    echo "https://developer.apple.com/documentation/safari-developer-tools/adding-additional-simulators#Add-and-remove-Simulators " >&2
    exit 1
fi

# sometimes a simulator is already running, shut it down to avoid errors
if [ "${SIMULATOR_STATE}" != "Shutdown" ]; then
    xcrun simctl shutdown "${UDID}"
fi

# boot up iOS for simulator
xcrun simctl boot "${UDID}"

# start the simulator
open -a Simulator --args -CurrentDeviceUDID "${UDID}"

BUILD_DIR="${GIT_ROOT}/build"

#iOS build of debug scheme
xcodebuild -workspace "ios/StatusIm.xcworkspace" -configuration Debug -scheme StatusIm -destination id="${UDID}" -derivedDataPath "${BUILD_DIR}" -verbose | xcbeautify

APP_PATH="${BUILD_DIR}/Build/Products/Debug-iphonesimulator/StatusIm.app"

trap showXcrunLogs EXIT ERR INT QUIT
installAndLaunchApp &
exec "${GIT_ROOT}/scripts/run-metro.sh" 2>&1
