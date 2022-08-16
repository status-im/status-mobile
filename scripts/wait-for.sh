#!/usr/bin/env bash

set -e

TIMEOUT=${1}
shift
PGREP_FILTER=${@}
SLEEP_SEC=5
STEPS=$((TIMEOUT / SLEEP_SEC))
# use UID to restrict search
PGREP_OPTS="-U ${UID} -f"

if [[ -z ${PGREP_OPTS} ]]; then
    echo "No pgrep options name specified!" >&2
    exit 1
fi

echo "Checking for process: '${PGREP_FILTER}'"
for ((i = 0; i < ${STEPS}; i += 1)); do
    if pgrep ${PGREP_OPTS} "${PGREP_FILTER}" > /dev/null; then
        echo "Process found. Sleeping ${SLEEP_SEC}..." >&2
        sleep ${SLEEP_SEC}
    else
        echo "Process '${PGREP_FILTER}' gone." >&2
        exit 0
    fi
done

echo "Timeout reached! (${TIMEOUT}s) Process still up: ${PGREP_OPTS}" >&2
echo "Following processes matched:"
# show what is still up
ps u $(pgrep -l ${PGREP_OPTS} "${PGREP_FILTER}" | cut -d' ' -f1 | xargs -I{} echo -n "-p {} ")
exit 1
