
#!/usr/bin/env bash

GIT_ROOT=$(cd "${BASH_SOURCE%/*}" && git rev-parse --show-toplevel)
source "${GIT_ROOT}/scripts/colors.sh"

METRO_PORT=8081
METRO_PID="$(lsof -i :${METRO_PORT} | awk 'NR!=1 {print $2}' | sort -u | tr '\r\n' ' ')"
if [ ! -z "$METRO_PID" ]; then
  echo -e "${YLW}TCP port ${METRO_PORT} is required by the Metro packager.\nThe following processes currently have the port open, preventing Metro from starting:${RST}"
  ps -fp $METRO_PID
  echo -e "${YLW}Do you want to terminate them (y/n)?${RST}"
  read -n 1 term
  [[ $term == 'y' ]] && kill $METRO_PID
fi

yarn start --reset-cache
