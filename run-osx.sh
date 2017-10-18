#!/bin/sh

[ `uname -s` != "Darwin" ] && return

function tab () {
    local cmd=""
    local cdto="$PWD"
    local args="$@"

    if [ -d "$1" ]; then
        cdto=`cd "$1"; pwd`
        args="${@:2}"
    fi

    if [ -n "$args" ]; then
        cmd="; $args"
    fi

    iterm_exists=`osascript -e "id of application \"iterm2\""`
    if [ ! -z iterm_exists ]; then
    osascript &>/dev/null <<EOF
        tell application "iTerm2"
            tell current window
                set newTab to (create tab with default profile)
                tell newTab
                    tell current session
                        write text "cd \"$cdto\"$cmd"
                    end tell
                end tell
            end tell
        end tell
EOF
    else
    osascript &>/dev/null <<END
tell app "Terminal" to do script "cd \"$cdto\"$cmd"
END
    fi
}

if [ ! -z $1 ]
then
 device_type="$1"
else
 device_type="genymotion"
fi

if [ ! -z $2 ]
then
 cljs_build="$2"
else
 cljs_build="android"
fi

if [ "$device_type" = "genymotion" ]
then
# Find Device based on Android version 6.0.0
device=$(/Applications/Genymotion\ Shell.app/Contents/MacOS/genyshell -c "devices list" | grep "6.0.0\|7.0.0")
#echo ${device##*| }
# Launch device in Genymotion
open -a /Applications/Genymotion.app/Contents/MacOS/player.app --args --vm-name "${device##*| }"
fi

# Install deps, prepare for genymotion and figwheel
lein deps && ./re-natal deps && ./re-natal use-android-device "${device_type}" && ./re-natal use-figwheel

# open figwheel in new tab
tab "BUILD_IDS=${cljs_build} lein repl"

# open react-native package in new tab
tab "react-native start"

# echo "Press any key when emulator, figwheel and packager are ready" && read -n 1
sleep 10s

adb reverse tcp:8081 tcp:8081 && adb reverse tcp:3449 tcp:3449

react-native run-android

if [ ! -z $3 ]
then
 tab "appium"
 lein test
 lein doo node test once
fi
