#!/bin/bash

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'
PLATFORM=""

#if no arguments passed, inform user about possible ones

if [ $# -eq 0 ]
  then
    echo -e "${GREEN}This script should be invoked with platform argument: 'mobile' or 'desktop'${NC}"
    echo "When called it replaces platform-specific files"
    echo "If invoked with 'mobile' argument it will make a copying: "
    echo "package.json.mobile -> package.json"
    echo ".re-natal.mobile -> .re-natal"
    echo "etc.."
    exit 1
  else
    PLATFORM=$1
fi

echo "Removing node_modules"
rm -rf node_modules

echo "copying package.json.${PLATFORM} -> package.json"
cp "package.json.${PLATFORM}" package.json

echo "copying package-lock.json.${PLATFORM} -> package-lock.json"
cp "package-lock.json.${PLATFORM}" package-lock.json

echo "copying .re-natal.${PLATFORM} -> .re-natal"
cp ".re-natal.${PLATFORM}" .re-natal

echo "copying VERSION.${PLATFORM} -> VERSION"
cp "VERSION.${PLATFORM}" VERSION

echo -e "${GREEN}Finished!${NC}"
