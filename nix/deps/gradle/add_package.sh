#!/usr/bin/env bash

set -Eeu

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
sort -uo nix/deps/gradle/deps.urls nix/deps/gradle/deps.urls

# Re-generate dependencies JSON.
"${GIT_ROOT}/nix/deps/gradle/generate.sh" gen_deps_json

# Re-generate dependencies list.
"${GIT_ROOT}/nix/deps/gradle/generate.sh" gen_deps_list

echo -e "${GRN}Successfully added:${RST} ${BLD}${1}${RST}" >&2
