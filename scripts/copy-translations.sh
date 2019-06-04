#!/usr/bin/env bash

set -e

use_chmod="$1"

GIT_ROOT=$(cd "${BASH_SOURCE%/*}" && git rev-parse --show-toplevel)

if [ ! -d "${GIT_ROOT}/node_modules" ]; then
  echo "node_modules directory is missing, aborting!"
  exit 1
fi

# 1. copy translations to node_modules
# 2. touch node_modules/.copied~ to avoid copying node_modules again during build

cp -R "${GIT_ROOT}/translations" "${GIT_ROOT}/status-modules/"
[ "$use_chmod" == 'chmod' ] && chmod u+w "${GIT_ROOT}/node_modules"
cp -R "${GIT_ROOT}/status-modules" "${GIT_ROOT}/node_modules/"
[ -f "${GIT_ROOT}/node_modules/.copied~" ] && touch "${GIT_ROOT}/node_modules/.copied~"
[ "$use_chmod" == 'chmod' ] && chmod u-w "${GIT_ROOT}/node_modules"

set +e
