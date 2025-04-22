#!/usr/bin/env bash
set -euo pipefail
set -m # needed to access jobs

GIT_ROOT=$(cd "${BASH_SOURCE%/*}" && git rev-parse --show-toplevel)
XCRUN_DEVICE_INSTALL_LOG_DIR="${GIT_ROOT}/logs/xcrun_device_install.log"
XCRUN_DEVICE_PROCESS_LAUNCH_LOG_DIR="${GIT_ROOT}/logs/xcrun_device_process_launch.log"
XCRUN_DEVICE_PROCESS_RESUME_LOG_DIR="${GIT_ROOT}/logs/xcrun_device_process_resume.log"

# Install on the connected device
installAndLaunchApp() {
  xcrun devicectl device install app --device "${DEVICE_UUID}" "${APP_PATH}" --json-output "${XCRUN_DEVICE_INSTALL_LOG_DIR}" 2>&1

  # Extract installationURL
  INSTALLATION_URL=$(jq -r '.result.installedApplications[0].installationURL' "${XCRUN_DEVICE_INSTALL_LOG_DIR}")

  # launch the app and put it in background
  xcrun devicectl device process launch --no-activate --verbose --device "${DEVICE_UUID}" "${INSTALLATION_URL}" --json-output "${XCRUN_DEVICE_PROCESS_LAUNCH_LOG_DIR}"

  # Extract background PID of status app
  STATUS_PID=$(jq -r '.result.process.processIdentifier' "${XCRUN_DEVICE_PROCESS_LAUNCH_LOG_DIR}")
  "${GIT_ROOT}/scripts/wait-for-metro-port.sh"  2>&1

  # now that metro is ready, resume the app from background
  xcrun devicectl device process resume --device "${DEVICE_UUID}" --pid "${STATUS_PID}" > "${XCRUN_DEVICE_PROCESS_RESUME_LOG_DIR}" 2>&1
}

showXcrunLogs() {
  cat "${XCRUN_DEVICE_INSTALL_LOG_DIR}" >&2;
  cat "${XCRUN_DEVICE_PROCESS_LAUNCH_LOG_DIR}" >&2;
  cat "${XCRUN_DEVICE_PROCESS_RESUME_LOG_DIR}" >&2;
}

# find the first connected iPhone's UUID
DEVICE_UUID=$(idevice_id -l)

# Check if any device is connected
if [ -z "${DEVICE_UUID}" ]; then
    echo "No connected iPhone device detected."
    exit 1
else
    echo "Connected iPhone UDID: ${DEVICE_UUID}"
fi

BUILD_DIR="${GIT_ROOT}/build"

#iOS build of debug scheme
xcodebuild -workspace "ios/StatusIm.xcworkspace" -configuration Debug -scheme StatusIm -destination id="${DEVICE_UUID}" -derivedDataPath "${BUILD_DIR}" -verbose | xcbeautify

APP_PATH="${BUILD_DIR}/Build/Products/Debug-iphoneos/StatusIm.app"

trap showXcrunLogs EXIT ERR INT QUIT
installAndLaunchApp &
exec "${GIT_ROOT}/scripts/run-metro.sh" 2>&1

