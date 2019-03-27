#!/usr/bin/env bash

set -e

GIT_ROOT=$(git rev-parse --show-toplevel)

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'
PLATFORM=""
PLATFORM_FOLDER=""

DO_SPACE_URL=https://status-go.ams3.digitaloceanspaces.com
GITHUB_URL=https://github.com/status-im/status-go/releases
STATUS_GO_VER="$(cat STATUS_GO_VERSION)"

ANDROID_LIBS_DIR="$GIT_ROOT/android/app/libs"
STATUS_GO_DRO_ARCH="${ANDROID_LIBS_DIR}/status-go-${STATUS_GO_VER}.aar"

RCTSTATUS_DIR="$GIT_ROOT/modules/react-native-status/ios/RCTStatus"
STATUS_GO_IOS_ARCH="${RCTSTATUS_DIR}/status-go-ios-${STATUS_GO_VER}.zip"

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
fi

yarn install --frozen-lockfile

if [ "$PLATFORM" == 'mobile' ]; then
  if [ "$1" == 'android' ]; then
    outputPath=$STATUS_GO_DRO_ARCH
    ext='.aar'
  else
    outputPath=$STATUS_GO_IOS_ARCH
    ext='.zip'
  fi

  statusGoSentinelFile="$(dirname $outputPath)/.download.log"
  if [ -f "$statusGoSentinelFile" ] && [ "$(cat $statusGoSentinelFile)" == "$STATUS_GO_VER" ]; then
	  echo "status-go artifact already downloaded!"
  else
    echo "Downloading status-go artifact from DigitalOcean Bucket to $outputPath"

    set +e
    mkdir -p $(dirname $outputPath)
    curl --fail --silent --location \
      "${DO_SPACE_URL}/status-go-$1-${STATUS_GO_VER}${ext}" \
      --output "$outputPath"
    set -e
    if [ $? -ne 0 ]; then
      echo "Failed to download from DigitalOcean Bucket, checking GitHub..."
      set +e
      curl --fail --silent --location \
        "${GITHUB_URL}/download/${STATUS_GO_VER}/status-go-$1.zip" \
        --output "$outputPath"
      set -e
      if [ $? -ne 0 ]; then
        echo "Failed to download from GitHub!"
        echo "Please check the contents of your STATUS_GO_VERSION are correct."
        echo "Verify the version has been uploaded:"
        echo " * ${DO_SPACE_URL}/index.html"
        echo " * $GITHUB_URL"
        exit 1
      fi
    fi
    echo "$STATUS_GO_VER" > $statusGoSentinelFile
  fi
fi

case $1 in
  android)
    set -e
    if [ ! -d $GIT_ROOT/node_modules/react-native/android/com/facebook/react/react-native/ ]; then
      cd $GIT_ROOT/android && ./gradlew react-native-android:installArchives
    fi
    ;;
  ios)
    if [ ! -d "$RCTSTATUS_DIR/Statusgo.framework" ]; then
      unzip -q -o "$STATUS_GO_IOS_ARCH" -d "$RCTSTATUS_DIR" && rm $STATUS_GO_IOS_ARCH
      if [ "$(uname)" == 'Darwin' ]; then
        # TODO: remove this patch when we upgrade to RN 0.57+
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
