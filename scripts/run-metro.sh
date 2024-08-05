#!/usr/bin/env bash

if pgrep -f "react-native start --port=${RCT_METRO_PORT:-8081}" > /dev/null; then
    echo "Info: metro is already running in another terminal"
else
    echo "Info: starting a new metro terminal"
    react-native start --port=${RCT_METRO_PORT:-8081} --reset-cache
fi
