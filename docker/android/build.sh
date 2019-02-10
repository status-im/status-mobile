#!/usr/bin/env bash

set -e -x

export FASTLANE_DISABLE_COLORS=1
export REALM_DISABLE_ANALYTICS=1
export YARN_CACHE_FOLDER=/var/tmp/yarn
export NPM_CONFIG_CACHE=/var/tmp/npm
export HOME=/tmp

make clean
## Prep
bundle install --quiet
make prepare-android
## Lint
lein cljfmt check
## Test
lein test-cljs
## Build
lein prod-build-android
# Compile
cd android
./gradlew assembleDebug -Dorg.gradle.daemon=false
