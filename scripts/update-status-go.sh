#!/usr/bin/env bash

set -eof pipefail

GIT_ROOT=$(git rev-parse --show-toplevel)

usage() {
    printf "%s is a tool for upgrading status-go to a given version.\n" "$(basename "$0")"
    printf "The given version must be uploaded to Artifactory first.\n\n"
    printf "Usage:\n\n"
    printf "    %s version\n\n" "$(basename "$0")"
    printf "Example:\n\n"
    printf "    %s develop-g12345678\n" "$(basename "$0")"
}

if [ "$1" = "-h" ] || [ "$1" = "--help" ]; then
    usage
    exit 1
fi

if [ $# -eq 0 ]; then
    echo "Need to provide a status-go version"
    exit 1
fi

STATUSGO_OWNER="$(cat ${GIT_ROOT}/STATUS_GO_OWNER)"
STATUSGO_VERSION="v${1#"v"}"
if [ "$STATUSGO_OWNER" == 'status-im' ] && [ "$STATUSGO_VERSION" != "$1" ]; then
  echo "status-go release branches should include the v prefix!"
  echo "Please create a new branch called $STATUSGO_VERSION"
  exit 1
fi
STATUSGO_VERSION=$1
STATUSGO_SHA256=$(nix-prefetch-url --unpack https://github.com/${STATUSGO_OWNER}/status-go/archive/${STATUSGO_VERSION}.zip)

echo $STATUSGO_VERSION > ${GIT_ROOT}/STATUS_GO_VERSION
echo $STATUSGO_SHA256 > ${GIT_ROOT}/STATUS_GO_SHA256
