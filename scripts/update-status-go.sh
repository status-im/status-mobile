#!/usr/bin/env bash

set -eof pipefail

if [ $# -eq 0 ]
then
    echo "Need to supply a status-go version"
    exit 1
fi

STATUSGO_VERSION=$1

sed -i '' -e "s/\(<version>\).*\(<\/version>\)/\1$STATUSGO_VERSION\2/" modules/react-native-status/ios/RCTStatus/pom.xml
sed -i '' -e "s/\(statusGoVersion = '\).*\('\)/\1$STATUSGO_VERSION\2/" modules/react-native-status/android/build.gradle
