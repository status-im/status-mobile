#!/usr/bin/env bash

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'
PLATFORM=""

_current_dir=$(cd "${BASH_SOURCE%/*}" && pwd)
source "$_current_dir/lib/setup/path-support.sh"

source_lib "packages.sh"

EXPECTED_NODE_VERSION="v$(toolversion node)" # note the 'v' in front, that is how node does versioning
EXPECTED_YARN_VERSION="$(toolversion yarn)" # note the lack of 'v' in front. inconsistent. :(

# if no arguments passed, inform user about possible ones
if [ $# -eq 0 ]; then
  echo -e "${GREEN}This script should be invoked with platform argument: 'android', 'ios' or 'desktop'${NC}"
  exit 1
else
  PLATFORM=$1
fi

if [ -z "$IN_NIX_SHELL" ]; then
  if ! program_version_exists node $EXPECTED_NODE_VERSION || ! program_version_exists yarn $EXPECTED_YARN_VERSION; then
    echo -e "${YELLOW}********************************************************************************************"

    echo -e "The current environment doesn't contain the expected versions of node and/or yarn"
    echo -e "  - node:\texpected\t${EXPECTED_NODE_VERSION}"
    echo -e "  \t\tfound\t\t$(node -v) ($(which node))"
    echo -e "  - yarn:\texpected\t${EXPECTED_YARN_VERSION}"
    echo -e "  \t\tfound\t\t$(yarn -v) ($(which yarn))"
    echo -e "Please open another console to reload the environment, and then run 'make setup' if necessary."

    echo -e "**********************************************************************************************${NC}"
    exit 1
  fi
fi

if [ "$PLATFORM" == 'android' ]; then
  if [ ! -d $ANDROID_SDK_ROOT ]; then
    echo -e "${GREEN}NDK setup not complete, please run 'make setup'!${NC}"
    exit 1
  fi
  if [ ! -d $ANDROID_NDK_ROOT ]; then
    echo -e "${GREEN}NDK setup not complete, please run 'make setup'!${NC}"
    exit 1
  fi
elif [ "$PLATFORM" == 'ios' ] && [ "$(uname)" != "Darwin" ]; then
  echo -e "${RED}iOS builds are only possible on macOS hosts${NC}"
  exit 1
fi

if [[ $PLATFORM == 'setup' ]]; then
  echo -e "${YELLOW}Finished! Please close your terminal, reopen a new one and type 'nix-shell' before building Status.${NC}"
else
  echo -e "${GREEN}Finished!${NC}"
fi