#!/usr/bin/env bash
source ~/.bash_profile

# cleanup
for pid in $(jps -lm | grep clojure | cut -d ' ' -f 1); do kill -9 $pid; done
adb devices | grep emulator | cut -f1 | while read line; do adb -s $line emu kill; done
rm -rf ~/.android/avd/*
killall node && sleep 1

# refresh dependences
lein deps && npm install && ./re-natal deps

./scripts/setup_android_emu.sh
./scripts/figwheel_background.sh
./scripts/reactnative_background.sh
./scripts/appium_background.sh

adb reverse tcp:8081 tcp:8081
adb reverse tcp:3449 tcp:3449
react-native run-android
lein test
lein doo node test once

# cleanup
for pid in $(jps -lm | grep clojure | cut -d ' ' -f 1); do kill -9 $pid; done
adb devices | grep emulator | cut -f1 | while read line; do adb -s $line emu kill; done
rm -rf ~/.android/avd/*
killall node && sleep 1