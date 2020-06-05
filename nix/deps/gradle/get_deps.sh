#!/usr/bin/env bash

set -Eeu

GIT_ROOT=$(cd "${BASH_SOURCE%/*}" && git rev-parse --show-toplevel)
# Gradle needs to be run in 'android' subfolder
cd $GIT_ROOT/android

AWK_SCRIPT="${GIT_ROOT}/nix/deps/gradle/gradle_parser.awk"

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

# And clean up the output by:
# - keep only lines that start with \--- or +---
# - drop lines that end with (*) or (n) but don't start with (+)
# - drop lines that refer to a project
# - drop entries starting with `status-im:` like `status-go`
# - drop entries that aren't just the name of the dependency
# - extract the package name and version, ignoring version range indications,
#   such as in `com.google.android.gms:play-services-ads:[15.0.1,16.0.0) -> 15.0.1`

./gradlew --no-daemon --console plain \
    "${BUILD_DEPS[@]}" \
    "${NORMAL_DEPS[@]}" \
    | awk -f ${AWK_SCRIPT}
