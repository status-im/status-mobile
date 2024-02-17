#!/usr/bin/env bash
set -euo pipefail
set -m # needed to access jobs

GIT_ROOT=$(cd "${BASH_SOURCE%/*}" && git rev-parse --show-toplevel)

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

# Install on the simulator
xcrun simctl install "$UUID" "$APP_PATH"

source "${GIT_ROOT}/scripts/lib/metro.sh"

trap cleanupMetro EXIT ERR INT QUIT
runMetro &
waitForMetro

# launch the app when metro is ready
xcrun simctl launch "$UUID" im.status.ethereum.debug

# bring metro job to foreground
fg 'runMetro'
