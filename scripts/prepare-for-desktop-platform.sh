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
PLATFORM_FOLDER="desktop/js_files"

if [ ! -f package.json ] || [ $(readlink package.json) != "${PLATFORM_FOLDER}/package.json" ]; then
  rm -rf node_modules

  echo "Creating link: package.json -> ${PLATFORM_FOLDER}/package.json"
  ln -sf ${PLATFORM_FOLDER}/package.json package.json

  echo "Creating link: yarn.lock -> ${PLATFORM_FOLDER}/yarn.lock"
  ln -sf ${PLATFORM_FOLDER}/yarn.lock yarn.lock

  echo "Creating link: metro.config.js -> ${PLATFORM_FOLDER}/metro.config.js"
  ln -sf ${PLATFORM_FOLDER}/metro.config.js metro.config.js
fi

mkdir -p "$GIT_ROOT/node_modules/"
# Leverage flock (file lock) utility to create an exclusive lock on node_modules/ while running 'yarn install'
flock "$GIT_ROOT/node_modules/" yarn install --frozen-lockfile

echo -e "${GREEN}Finished!${NC}"
