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

#iOS build of debug scheme
xcodebuild -workspace "ios/StatusIm.xcworkspace" -configuration Debug -scheme StatusIm -destination id="$UUID" | xcbeautify

trap cleanupMetro EXIT ERR INT QUIT
runMetro &
waitForMetro

# launch the app when metro is ready
xcrun simctl launch "$UUID" im.status.ethereum.debug

# bring metro job to foreground
fg 'runMetro'
