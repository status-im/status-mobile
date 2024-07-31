#!/usr/bin/env bash

if pgrep -f 'shadow-cljs watch mobile' > /dev/null; then
    echo "Error: make run-clojure is already running in another terminal" >&2
    echo "Please close that terminal before running this command." >&2
    exit 1
fi

if pgrep -f "react-native start --port=${RCT_METRO_PORT:-8081}" > /dev/null; then
    echo "Error: make run-metro is already running in another terminal" >&2
    echo "Please close that terminal before running this command." >&2
    exit 1
fi

exit 0
