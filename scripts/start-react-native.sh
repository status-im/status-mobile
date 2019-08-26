#!/usr/bin/env bash

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

METRO_PORT=8081
METRO_PID="$(lsof -i :${METRO_PORT} | awk 'NR!=1 {print $2}' | sort -u | tr '\r\n' ' ')"
if [ ! -z "$METRO_PID" ]; then
  echo -e "${YELLOW}TCP port ${METRO_PORT} is required by the Metro packager.\nThe following processes currently have the port open, preventing Metro from starting:${NC}"
  ps -fp $METRO_PID
  echo -e "${YELLOW}Do you want to terminate them (y/n)?${NC}"
  read -n 1 term
  [[ $term == 'y' ]] && kill $METRO_PID
fi

react-native start
