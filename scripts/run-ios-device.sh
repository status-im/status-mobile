#!/usr/bin/env bash
set -euo pipefail
set -m # needed to access jobs

GIT_ROOT=$(cd "${BASH_SOURCE%/*}" && git rev-parse --show-toplevel)

# We run Metro in background while calling adb.
cleanupMetro() {
    pkill -f run-metro.sh
    rm -f metro-server-logs.log
}

# Using function gives a neater jobspec name.
runMetro() {
   nohup "${GIT_ROOT}/scripts/run-metro.sh" 2>&1 \
        | tee metro-server-logs.log
}

waitForMetro() {
    set +e # Allow grep command to fail in the loop.
    TIMEOUT=5
    echo "Waiting for Metro server..." >&2
    while ! grep -q "Welcome to Metro" metro-server-logs.log; do
      echo -n "." >&2
      sleep 1
      if ((TIMEOUT == 0)); then
        echo -e "\nMetro server timed out, exiting" >&2
        set -e # Restore errexit for rest of script.
        return 1
      fi
      ((TIMEOUT--))
    done
    set -e # Restore errexit for rest of script.
}

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

trap cleanupMetro EXIT ERR INT QUIT
runMetro &
waitForMetro

# now that metro is ready, resume the app from background
xcrun devicectl device process resume --device "$DEVICE_UUID" --pid "$STATUS_PID"

# bring metro job to foreground
fg 'runMetro'
