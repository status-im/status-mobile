#!/usr/bin/env bash
set -euo pipefail

GIT_ROOT=$(cd "${BASH_SOURCE%/*}" && git rev-parse --show-toplevel)

# We run Metro in background while calling adb.
cleanupMetro() {
    pkill -f run-metro.sh
    rm -f metro-server-logs.log
}

# Using function gives a neater jobspec name.
runMetro() {
   nohup "${GIT_ROOT}/scripts/run-metro.sh" 2>&1 \
        | tee metro-server-logs.log
}

waitForMetro() {
    set +e # Allow grep command to fail in the loop.
    TIMEOUT=5
    echo "Waiting for Metro server..." >&2
    while ! grep -q "Welcome to Metro" metro-server-logs.log; do
      echo -n "." >&2
      sleep 1
      if ((TIMEOUT == 0)); then
        echo -e "\nMetro server timed out, exiting" >&2
        set -e # Restore errexit for rest of script.
        return 1
      fi
      ((TIMEOUT--))
    done
    set -e # Restore errexit for rest of script.
}
