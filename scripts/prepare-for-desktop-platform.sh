#!/usr/bin/env bash

# WARNING: Impure setup, should be minimized.
# TODO: Replace this with yarn2nix.

set -e

GIT_ROOT=$(cd "${BASH_SOURCE%/*}" && git rev-parse --show-toplevel)
source "${GIT_ROOT}/scripts/colors.sh"

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

echo -e "${GRN}Finished!${RST}"
