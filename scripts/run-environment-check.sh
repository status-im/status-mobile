#!/bin/bash

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

load_nvm_if_available

if ! program_exists node || ! program_exists yarn; then
  echo -e "${YELLOW}********************************************************************************************"

  nvmrc="./.nvmrc"
  if [ -e "$nvmrc" ] && nvm_installed; then
    version_alias=$(cat "$nvmrc")
    echo -e "Please run 'nvm use $version_alias' in the terminal and try again."
  else
    echo -e "The current environment doesn't contain the expected versions of node and/or yarn"
    echo -e "  - node:\texpected\t${EXPECTED_NODE_VERSION}"
    echo -e "  \t\tfound\t\t$(node -v) ($(which node))"
    echo -e "  - yarn:\texpected\t${EXPECTED_YARN_VERSION}"
    echo -e "  \t\tfound\t\t$(yarn -v) ($(which yarn))"
    echo -e "Please open another console to reload the environment, and then run 'make setup' if necessary."
  fi

  echo -e "**********************************************************************************************${NC}"
  exit 1
fi

node_version=$(node -v)
if [[ $node_version != $EXPECTED_NODE_VERSION ]]; then
  echo -e "${YELLOW}+ node version $node_version is installed. node version $EXPECTED_NODE_VERSION is recommended.${NC}"
fi

yarn_version=$(yarn -v)
if [[ $yarn_version != $EXPECTED_YARN_VERSION ]]; then
  echo -e "${YELLOW}+ yarn version $yarn_version is installed. yarn version $EXPECTED_YARN_VERSION is recommended.${NC}"
fi

if [[ $PLATFORM == 'android' ]]; then
  _localPropertiesPath=./android/local.properties
  if [ ! -f $_localPropertiesPath ] || ! grep -Fq "ndk.dir" $_localPropertiesPath > /dev/null; then
    if [ -z $ANDROID_NDK_HOME ]; then
      echo -e "${GREEN}NDK directory not configured, please run 'make setup' or add the line to ${_localPropertiesPath}!${NC}"
      exit 1
    fi
  fi
fi

if [[ $PLATFORM == 'setup' ]]; then
  echo -e "${YELLOW}Finished! Please close your terminal, and reopen a new one before building Status.${NC}"
else
  echo -e "${GREEN}Finished!${NC}"
fi