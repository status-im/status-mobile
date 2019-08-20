#!/usr/bin/env bash

set -e

TIMEOUT=${1}
shift
PGREP_OPTS=${@}
SLEEP_SEC=5
STEPS=$((TIMEOUT / SLEEP_SEC))

if [[ -z ${PGREP_OPTS} ]]; then
    echo "No pgrep options name specified!" >&2
    exit 1
fi

echo "Checking for process: '${PGREP_OPTS}'"
for ((i = 0; i < ${STEPS}; i += 1)); do
    if pgrep ${PGREP_OPTS} > /dev/null; then
        echo "Process found. Sleeping ${SLEEP_SEC}..." >&2
        sleep ${SLEEP_SEC}
    else
        echo "Process '${PGREP_OPTS}' gone." >&2
        exit 0
    fi
done

echo "Timeout reached! (${TIMEOUT}s) Process still up: ${PGREP_OPTS}" >&2
exit 1
