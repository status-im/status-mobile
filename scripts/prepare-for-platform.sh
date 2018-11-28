#!/bin/bash

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'
PLATFORM=""
PLATFORM_FOLDER=""

#if no arguments passed, inform user about possible ones

if [ $# -eq 0 ]; then
  echo -e "${GREEN}This script should be invoked with platform argument: 'android', 'ios' or 'desktop'${NC}"
  echo "If invoked with 'mobile' argument it will link: "
  echo "package.json.mobile -> package.json"
  echo "etc.."
  exit 1
else
  case $1 in
    android | ios)
      PLATFORM='mobile'
      ;;
    *)
      PLATFORM=$1
      ;;
  esac
  PLATFORM_FOLDER="${PLATFORM}_files"
fi

scripts/run-pre-build-check.sh $1

echo "Creating link: package.json -> ${PLATFORM_FOLDER}/package.json.orig "
ln -sf  ${PLATFORM_FOLDER}/package.json.orig package.json

echo "Creating link: yarn.lock -> ${PLATFORM_FOLDER}/yarn.lock"
ln -sf  ${PLATFORM_FOLDER}/yarn.lock yarn.lock

echo "Creating link: VERSION -> ${PLATFORM_FOLDER}/VERSION"
ln -sf  ${PLATFORM_FOLDER}/VERSION VERSION

echo -e "${GREEN}Finished!${NC}"
