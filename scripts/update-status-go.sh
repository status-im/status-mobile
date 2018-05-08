#!/usr/bin/env bash

set -eof pipefail

usage() {
    printf "%s is a tool for upgrading status-go to a given version.\n" "$(basename "$0")"
    printf "The given version must be uploaded to Artifactory first.\n\n"
    printf "Usage:\n\n"
    printf "    %s version\n\n" "$(basename "$0")"
    printf "Example:\n\n"
    printf "    %s develop-g12345678\n" "$(basename "$0")"
}

if [ "$1" = "-h" ] || [ "$1" = "--help" ]; then
    usage
    exit 1
fi

if [ $# -eq 0 ]; then
    echo "Need to provide a status-go version"
    exit 1
fi

STATUSGO_VERSION=$1

sed -i '' -e "s/\(<version>\).*\(<\/version>\)/\1$STATUSGO_VERSION\2/" modules/react-native-status/ios/RCTStatus/pom.xml
sed -i '' -e "s/\(statusGoVersion = '\).*\('\)/\1$STATUSGO_VERSION\2/" modules/react-native-status/android/build.gradle
