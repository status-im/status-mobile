#!/usr/bin/env bash

set -e

PROC_NAME=${1}
TIMEOUT=${2:-600}
SLEEP_SEC=5
STEPS=$((TIMEOUT / SLEEP_SEC))

if [[ -z ${PROC_NAME} ]]; then
    echo "No process name specified!" >&2
    exit 1
fi

for ((i = 0; i < ${STEPS}; i += 1)); do
    if pgrep -f ${PROC_NAME} > /dev/null; then
        echo "Process found. Sleeping ${SLEEP_SEC}..." >&2
        sleep ${SLEEP_SEC}
    else
        echo "Process '${PROC_NAME}' gone." >&2
        exit 0
    fi
done

echo "Timeout reached! (${TIMEOUT}s) Process still up: ${PROC_NAME}" >&2
exit 1
