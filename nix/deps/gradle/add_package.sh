#!/usr/bin/env bash

# This script allows for adding a package by providing a Maven full name.
# Such name consists of 3 sections separated by the colon character.
# Example: com.android.tools.build:gradle:3.5.3

set -Eeuo pipefail

if [[ $# -ne 1 ]]; then
    echo "Usage: add_package.sh <package>" >&2
    exit 1
fi
if ! command -v go-maven-resolver &> /dev/null; then
   echo "Use 'make shell TARGET=gradle' for this script" >&2
    exit 1
fi

GIT_ROOT=$(cd "${BASH_SOURCE%/*}" && git rev-parse --show-toplevel)
source "${GIT_ROOT}/scripts/colors.sh"

# Add missing package to dependencies.
echo "${1}" | go-maven-resolver >> nix/deps/gradle/deps.urls

# Remove duplicates and sort.
sort -uVo nix/deps/gradle/deps.urls nix/deps/gradle/deps.urls

echo -e "${GRN}Changes made:${RST}" >&2
git diff --stat nix/deps/gradle/deps.urls
echo

# Re-generate dependencies JSON.
"${GIT_ROOT}/nix/deps/gradle/generate.sh" gen_deps_json

echo -e "${GRN}Successfully added:${RST} ${BLD}${1}${RST}" >&2
echo
echo -e "${YLW}NOTICE:${RST} Running '${BLD}make nix-update-gradle${RST}' in a new shell is recommended."
