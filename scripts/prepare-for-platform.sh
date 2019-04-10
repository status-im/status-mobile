#!/usr/bin/env bash

set -e

GIT_ROOT=$(git rev-parse --show-toplevel)

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'
PLATFORM=""
PLATFORM_FOLDER=""

RCTSTATUS_DIR="$GIT_ROOT/modules/react-native-status/ios/RCTStatus"

#if no arguments passed, inform user about possible ones

if [ $# -eq 0 ]; then
  echo -e "${GREEN}This script should be invoked with platform argument: 'android', 'ios', or 'desktop'${NC}"
  echo "If invoked with 'android' argument it will link: "
  echo "mobile_files/package.json.orig -> package.json"
  echo "etc.."
  exit 1
else
  case $1 in
    android | ios)
      PLATFORM='mobile'
      ;;
    *)
      PLATFORM=$1
      ;;
  esac
  PLATFORM_FOLDER="${PLATFORM}_files"
fi

$GIT_ROOT/scripts/run-environment-check.sh $1

if [ ! -f .babelrc ] || [ $(readlink .babelrc) != "${PLATFORM_FOLDER}/.babelrc" ]; then
  echo "Creating link: package.json -> ${PLATFORM_FOLDER}/package.json.orig"
  ln -sf ${PLATFORM_FOLDER}/package.json.orig package.json

  echo "Creating link: yarn.lock -> ${PLATFORM_FOLDER}/yarn.lock"
  ln -sf ${PLATFORM_FOLDER}/yarn.lock yarn.lock

  echo "Creating link: VERSION -> ${PLATFORM_FOLDER}/VERSION"
  ln -sf ${PLATFORM_FOLDER}/VERSION VERSION

  echo "Creating link: .babelrc -> ${PLATFORM_FOLDER}/.babelrc"
  ln -sf ${PLATFORM_FOLDER}/.babelrc .babelrc

  echo "Creating link: .babelrc -> ${PLATFORM_FOLDER}/.babelrc"
  ln -sf ${PLATFORM_FOLDER}/metro.config.js metro.config.js
fi

yarn install --frozen-lockfile

case $1 in
  android)
    set -e
    if [ ! -d $GIT_ROOT/node_modules/react-native/android/com/facebook/react/react-native/ ]; then
      cd $GIT_ROOT/android && ./gradlew react-native-android:installArchives
    fi
    ;;
  ios)
    targetBasename='Statusgo.framework'
    # Compare target folder with source to see if copying is required
    if [ -d "$RCTSTATUS_DIR/$targetBasename" ] && \
       diff -q --no-dereference --recursive $RCTSTATUS_DIR/$targetBasename/ $RCTSTATUS_FILEPATH/ > /dev/null; then
      echo "$RCTSTATUS_DIR/$targetBasename already in place"
    else
      sourceBasename="$(basename $RCTSTATUS_FILEPATH)"
      echo "Copying $sourceBasename from Nix store to $RCTSTATUS_DIR"
      rm -rf "$RCTSTATUS_DIR/$targetBasename/"
      cp -a $RCTSTATUS_FILEPATH $RCTSTATUS_DIR && chmod -R 755 "$RCTSTATUS_DIR/$targetBasename"
      if [ "$sourceBasename" != "$targetBasename" ]; then
        mv "$RCTSTATUS_DIR/$sourceBasename" "$RCTSTATUS_DIR/$targetBasename"
      fi
      if [ "$(uname)" == 'Darwin' ]; then
        # TODO: remove this patch when we upgrade to a RN version that plays well with the modern build system
        git apply --check $GIT_ROOT/ios/patches/ios-legacy-build-system.patch 2> /dev/null && \
          git apply $GIT_ROOT/ios/patches/ios-legacy-build-system.patch || \
          echo "Patch already applied"
        # CocoaPods are trash and can't handle other pod instances running at the same time
        $GIT_ROOT/scripts/wait-for.sh pod 240
        pushd $GIT_ROOT/ios && pod install; popd
      fi
    fi
    ;;
esac

echo -e "${GREEN}Finished!${NC}"
