#!/usr/bin/env bash
set -euo pipefail

TIMEOUT=10 # Metro should not take this long to start.

while [ "${TIMEOUT}" -gt 0 ]; do
  if ! lsof -i:"${RCT_METRO_PORT:-8081}" &> /dev/null; then
    echo "."
    sleep 1
    ((TIMEOUT--))
  else
    break
  fi
done
