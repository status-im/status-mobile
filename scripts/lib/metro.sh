#!/usr/bin/env bash
set -euo pipefail

GIT_ROOT=$(cd "${BASH_SOURCE%/*}" && git rev-parse --show-toplevel)
METRO_TMUX_SESSION="metro-server"
# Use tmux to run Metro in background.
cleanupMetro() {
  tmux kill-session -t $METRO_TMUX_SESSION 2>/dev/null || true
  tmux send-keys -t $METRO_TMUX_SESSION 'exit' C-m
  rm -f "${GIT_ROOT}/metro-server-logs.log"
}

# Start Metro in a new tmux session.
runMetro() {
  tmux new-session -d -s $METRO_TMUX_SESSION
  tmux send-keys -t $METRO_TMUX_SESSION "react-native start" C-m
  tmux pipe-pane -t $METRO_TMUX_SESSION 'tee metro-server-logs.log > /dev/null'
}

waitForMetro() {
  set +e # Allow grep command to fail in the loop.
  TIMEOUT=5
  echo "Waiting for Metro server..." >&2
  while ! grep -q "Welcome to Metro" "${GIT_ROOT}/metro-server-logs.log"; do
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
