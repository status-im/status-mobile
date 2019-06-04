#!/usr/bin/env bash

###################################################################################################
#
# Impure setup (any setup here should be minimized and instead be moved to Nix for a pure setup)
#
###################################################################################################

set -e

GIT_ROOT=$(cd "${BASH_SOURCE%/*}" && git rev-parse --show-toplevel)

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'
PLATFORM_FOLDER="desktop_files"

$GIT_ROOT/scripts/run-environment-check.sh desktop

if [ ! -f package.json ] || [ $(readlink package.json) != "${PLATFORM_FOLDER}/package.json.orig" ]; then
  if [ -d node_modules ]; then
    chmod -R u+w node_modules
    rm -rf node_modules
  fi

  echo "Creating link: package.json -> ${PLATFORM_FOLDER}/package.json.orig"
  ln -sf ${PLATFORM_FOLDER}/package.json.orig package.json

  echo "Creating link: yarn.lock -> ${PLATFORM_FOLDER}/yarn.lock"
  ln -sf ${PLATFORM_FOLDER}/yarn.lock yarn.lock

  echo "Creating link: metro.config.js -> ${PLATFORM_FOLDER}/metro.config.js"
  ln -sf ${PLATFORM_FOLDER}/metro.config.js metro.config.js
fi

yarn install --frozen-lockfile

echo -e "${GREEN}Finished!${NC}"
