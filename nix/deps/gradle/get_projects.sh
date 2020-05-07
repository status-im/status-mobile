#!/usr/bin/env bash

set -Eeu

GIT_ROOT=$(cd "${BASH_SOURCE%/*}" && git rev-parse --show-toplevel)
# Gradle needs to be run in 'android' subfolder
cd $GIT_ROOT/android

# Print all our sub-projects
./gradlew projects --no-daemon --console plain 2>&1 \
    | grep "Project ':" \
    | sed -E "s;^.--- Project '\:([@_a-zA-Z0-9\-]+)';\1;"
