#!/usr/bin/env bash

set -euf

TARGET=${1:-debug}

CURRENT_DIR="$( cd "$( dirname "$0" )" && pwd )"
. "$CURRENT_DIR/lib/setup/path-support.sh"
source_lib "output.sh"
source_lib "properties.sh"

cecho "Building @b@green[[${TARGET}]] environment"
echo

GRADLE_PROPERTIES="--daemon --parallel -q -b android/build.gradle"

scripts/locked-npm-install.sh

case $TARGET in
  debug)
    lein do clean, cljsbuild once android && ./android/gradlew ${GRADLE_PROPERTIES} assembleDebug
    echo "Generated android/app/build/outputs/apk/debug/app-debug.apk"
    exit 1
    ;;
  release)
    STORE_FILE=$(property_gradle 'STATUS_RELEASE_STORE_FILE')
    [[ ! -e "${STORE_FILE/#\~/$HOME}" ]] && echo "Please generate keystore first using ./generate-keystore.sh" && exit 0
    lein do clean, with-profile prod cljsbuild once android && ./android/gradlew ${GRADLE_PROPERTIES} assembleRelease
    cecho "Generated @b@blueandroid/app/build/outputs/apk/release/app-release.apk"
    echo
    exit
    ;;
  *)
    echo "Only debug and release targets are supported"
    exit 1
esac
