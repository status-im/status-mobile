#!/bin/bash

echo no | android create avd --force --name test --target android-23 --abi x86
emulator -avd test -no-window -no-boot-anim -wipe-data &

adb wait-for-device

A=$(adb shell getprop sys.boot_completed | tr -d '\r')

while [ "$A" != "1" ]; do
        sleep 2
        A=$(adb shell getprop sys.boot_completed | tr -d '\r')
done

adb shell settings put global window_animation_scale 0 &
adb shell settings put global transition_animation_scale 0 &
adb shell settings put global animator_duration_scale 0 &
adb shell input keyevent 82 & # unlock
sleep 3 # wait a little
echo "Android Emulator Initialized"