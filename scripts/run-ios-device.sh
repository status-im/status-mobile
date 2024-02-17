#!/usr/bin/env bash
set -euo pipefail
set -m # needed to access jobs

GIT_ROOT=$(cd "${BASH_SOURCE%/*}" && git rev-parse --show-toplevel)

# find the first connected iPhone's UUID
DEVICE_UUID=$(idevice_id -l)

# Check if any device is connected
if [ -z "$DEVICE_UUID" ]; then
    echo "No connected iPhone device detected."
    exit 1
else
    echo "Connected iPhone UDID: $DEVICE_UUID"
fi

BUILD_DIR="${GIT_ROOT}/build"
XCRUN_LOG_DIR="${GIT_ROOT}/build/XcrunLog"

#iOS build of debug scheme
xcodebuild -workspace "ios/StatusIm.xcworkspace" -configuration Debug -scheme StatusIm -destination id="$DEVICE_UUID" -derivedDataPath "${BUILD_DIR}" | xcbeautify

APP_PATH="${BUILD_DIR}/Build/Products/Debug-iphoneos/StatusIm.app"

# Install on the connected device
xcrun devicectl device install app --device "$DEVICE_UUID" "$APP_PATH" --json-output "$XCRUN_LOG_DIR"

# Extract installationURL
INSTALLATION_URL=$(jq -r '.result.installedApplications[0].installationURL' "$XCRUN_LOG_DIR")

# launch the app and put it in background
xcrun devicectl device process launch --no-activate --verbose --device "$DEVICE_UUID" "$INSTALLATION_URL" --json-output "$XCRUN_LOG_DIR"

# Extract background PID of status app
STATUS_PID=$(jq -r '.result.process.processIdentifier' "$XCRUN_LOG_DIR")

source "${GIT_ROOT}/scripts/lib/metro.sh"

trap cleanupMetro EXIT ERR INT QUIT
runMetro &
waitForMetro

# now that metro is ready, resume the app from background
xcrun devicectl device process resume --device "$DEVICE_UUID" --pid "$STATUS_PID"

# bring metro job to foreground
fg 'runMetro'
