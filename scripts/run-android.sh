#!/usr/bin/env bash
set -euo pipefail
set -m # needed to access jobs

GIT_ROOT=$(cd "${BASH_SOURCE%/*}" && git rev-parse --show-toplevel)

# Generate android debug build.
export ANDROID_ABI_INCLUDE=$("${GIT_ROOT}/scripts/adb_devices_abis.sh")
export BUILD_ENV=debug
export BUILD_TYPE=debug
"${GIT_ROOT}/scripts/build-android.sh"

# Install the APK on running emulator or android device.
adb install ./result/app-debug.apk

source "${GIT_ROOT}/scripts/lib/metro.sh"

trap cleanupMetro EXIT ERR INT QUIT
runMetro &
waitForMetro

# Start the installed app.
adb shell monkey -p im.status.ethereum.debug 1

# bring metro job to foreground
fg 'runMetro'
