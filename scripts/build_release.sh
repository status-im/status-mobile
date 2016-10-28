#!/usr/bin/env bash

# TODO check tests passed
echo "STATUS_TEST_FUNC: $STATUS_TEST_FUNC"
echo "STATUS_TEST_UI: $STATUS_TEST_UI"

# cleanup
for pid in $(jps -lm | grep clojure | cut -d ' ' -f 1); do kill -9 $pid; done
adb devices | grep emulator | cut -f1 | while read line; do adb -s $line emu kill; done
rm -rf ~/.android/avd/*
killall node && sleep 1

lein prod-build
cd android && ./gradlew assembleRelease

# TODO release and upload to github
# zip -r artifacts.zip artifacts_folder # ipa & apk
# github-release release --user ${GITHUB_ORGANIZATION} --repo ${GITHUB_REPO} --tag ${VERSION_NAME} --name "${VERSION_NAME}"
# github-release upload --user ${GITHUB_ORGANIZATION} --repo ${GITHUB_REPO} --tag ${VERSION_NAME} --name "${PROJECT_NAME}-${VERSION_NAME}.zip" --file artifacts.zip
# https://github.com/aktau/github-release