#!/usr/bin/env bash

if pgrep -f "react-native start --port=$RCT_METRO_PORT" > /dev/null; then
    echo "Info: metro is already running in another terminal"
else
    echo "Info: starting a new metro terminal"
    react-native start --port=$RCT_METRO_PORT --reset-cache
fi
