#!/usr/bin/env bash

# This script generates a list of dependencies for the main project and its
# sub-projects defined using Gradle config files. It parses Gradle output of
# 'dependencies' and 'buildEnvironment` tasks using AWK.

set -Eeuo pipefail

GIT_ROOT=$(cd "${BASH_SOURCE%/*}" && git rev-parse --show-toplevel)
GRADLE_REPORTS_DIR="${GIT_ROOT}/android/build/reports/dependency-graph-snapshots"
# Gradle needs to be run in 'android' subfolder.
cd "${GIT_ROOT}/android"

# Show Gradle log in case of failure.
GRADLE_LOG_FILE='/tmp/gradle.log'
function show_gradle_log() { cat "${GRADLE_LOG_FILE}" >&2; }
trap show_gradle_log ERR

./gradlew -I init.gradle \
  --dependency-verification=off \
  --no-configuration-cache --no-configure-on-demand \
 :ForceDependencyResolutionPlugin_resolveAllDependencies > "${GRADLE_LOG_FILE}" 2>&1

# skip org.webkit:android-jsc, its provided by react-native
# remove when new architecture is enabled
jq -r '.[].dependency | select(startswith("org.webkit:android-jsc") | not)' "${GRADLE_REPORTS_DIR}/dependency-resolution.json"
