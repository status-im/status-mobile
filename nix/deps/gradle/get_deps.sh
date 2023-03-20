#!/usr/bin/env bash

# This script generates a list of dependencies for the main project and its
# sub-projects defined using Gradle config files. It parses Gradle output of
# 'dependencies' and 'buildEnvironment` tasks using AWK.

set -Eeuo pipefail

GIT_ROOT=$(cd "${BASH_SOURCE%/*}" && git rev-parse --show-toplevel)
# Gradle needs to be run in 'android' subfolder.
cd "${GIT_ROOT}/android"

# Show Gradle log in case of failure.
GRADLE_LOG_FILE='/tmp/gradle.log'
function show_gradle_log() { cat "${GRADLE_LOG_FILE}" >&2; }
trap show_gradle_log ERR

# Run the gradle command for a project:
# - ':buildEnvironment' to get build tools
# - ':dependencies' to get direct deps limited those by
#   implementation config to avoid test dependencies
DEPS=("${@}")
declare -a BUILD_DEPS
declare -a NORMAL_DEPS
for i in "${!DEPS[@]}"; do
    BUILD_DEPS[${i}]="${DEPS[${i}]}:buildEnvironment"
    NORMAL_DEPS[${i}]="${DEPS[${i}]}:dependencies"
done

# And clean up the output using AWK script.
AWK_SCRIPT="${GIT_ROOT}/nix/deps/gradle/gradle_parser.awk"

./gradlew --no-daemon --console plain \
    "${BUILD_DEPS[@]}" \
    "${NORMAL_DEPS[@]}" \
    | tee "${GRADLE_LOG_FILE}" \
    | awk -f "${AWK_SCRIPT}"
