#!/usr/bin/env bash

# This script generates a list of Gradle sub-projects by parsing the output
# of Gradle 'projects' task using grep and sed. It is necessary in order to
# collect list of dependencies for main project and its sub-projects.

set -Eeuo pipefail

GIT_ROOT=$(cd "${BASH_SOURCE%/*}" && git rev-parse --show-toplevel)
# Gradle needs to be run in 'android' subfolder.
cd "${GIT_ROOT}/android"

# Show Gradle log in case of failure.
GRADLE_LOG_FILE='/tmp/gradle.log'
function show_gradle_log() { cat "${GRADLE_LOG_FILE}" >&2; }
trap show_gradle_log ERR

# Print all our sub-projects
./gradlew projects --no-daemon --console plain 2>&1 \
    | tee "${GRADLE_LOG_FILE}" \
    | grep "Project ':" \
    | sed -E "s;^.--- Project '\:([@_a-zA-Z0-9\-]+)';\1;"
