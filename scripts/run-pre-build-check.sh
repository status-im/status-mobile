#!/bin/bash

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'
PLATFORM=""

EXPECTED_NODE_VERSION="v9.3.0" # note the 'v' in front, that is how node does versioning
EXPECTED_YARN_VERSION="1.12.3" # note the lack of 'v' in front. inconsistent. :(

#if no arguments passed, inform user about possible ones

if [ $# -eq 0 ]; then
  echo -e "${GREEN}This script should be invoked with platform argument: 'android', 'ios' or 'desktop'${NC}"
  exit 1
else
  PLATFORM=$1
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
  if ! grep -Fq "ndk.dir" $_localPropertiesPath; then
    if [ -z $ANDROID_NDK_HOME ]; then
      echo -e "${GREEN}NDK directory not configured, please run 'make setup' or add the line to ${_localPropertiesPath}!${NC}"
      exit 1
    fi
  fi
fi

echo -e "${GREEN}Finished!${NC}"
