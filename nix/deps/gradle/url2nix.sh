#!/usr/bin/env bash

#
# This script takes a deps.list file and builds a Nix expression
# that can be used by maven-repo-builder.nix to produce a path to
# a local Maven repository.
#

CUR_DIR=$(cd "${BASH_SOURCE%/*}" && pwd)

# sources REPOS associative array
source ${CUR_DIR}/repos.sh

function nix_fetch() {
    nix-prefetch-url --print-path --type sha256 "${1}" 2>/dev/null
}

function get_nix_path() {
    nix_fetch "${1}" | tail -n1
}

function get_nix_sha() {
    nix_fetch "${1}" | head -n1
}

function get_sha1() {
    sha1sum "${1}" | cut -d' ' -f1
}

# Assumes REPOS from repos.sh is available
function match_repo_url() {
    for REPO_URL in "${REPOS[@]}"; do
        if [[ "$1" = ${REPO_URL}* ]]; then
            echo "${REPO_URL}"
            return
        fi
    done
    echo " ! Failed to match a repo for: ${1}" >&2
    exit 1
}

if [[ -z "${1}" ]]; then
    echo "Required argument not given!" >&2
    exit 1
fi

OBJ_REL_URL=${1}

echo -en "\033[2K - Nix entry for: ${1##*/}\r" >&2

REPO_URL=$(match_repo_url "${OBJ_REL_URL}")

if [[ -z "${REPO_URL}" ]]; then
    echo "\r\n ? REPO_URL: ${REPO_URL}" >&2
fi
# Get the relative path without full URL
OBJ_REL_NAME="${OBJ_REL_URL#${REPO_URL}/}"

# Dependency might be a JAR or an AAR
if nix_fetch "${OBJ_REL_URL}.jar" &>/dev/null; then
    OBJ_TYPE="jar"
elif nix_fetch "${OBJ_REL_URL}.aar" &>/dev/null; then
    OBJ_TYPE="aar"
else
    OBJ_TYPE="pom"
fi

# Some deps have only a POM, nor JAR or AAR
if [[ "${OBJ_TYPE}" != "pom" ]]; then
    OBJ_PATH=$(get_nix_path "${OBJ_REL_URL}.${OBJ_TYPE}")
    OBJ_SHA256=$(get_nix_sha "${OBJ_REL_URL}.${OBJ_TYPE}")
    OBJ_SHA1=$(get_sha1 "${OBJ_PATH}")
fi

POM_PATH=$(get_nix_path "${OBJ_REL_URL}.pom")
if [[ -z "${POM_PATH}" ]]; then
    echo " ! Failed to fetch: ${OBJ_REL_URL}.pom" >&2
    exit 1
fi

POM_SHA256=$(get_nix_sha "${OBJ_REL_URL}.pom")
POM_SHA1=$(get_sha1 "${POM_PATH}")

# Format into a Nix attrset entry
echo -n "
  \"${OBJ_REL_NAME}\" =
  {
    host = \"${REPO_URL}\";
    path = \"${OBJ_REL_NAME}\";
    type = \"${OBJ_TYPE}\";"
if [[ -n "${POM_SHA256}" ]]; then
    echo -n "
    pom = {
      sha1 = \"${POM_SHA1}\";
      sha256 = \"${POM_SHA256}\";
    };"
fi
if [[ -n "${OBJ_SHA256}" ]]; then
    echo -n "
    jar = {
      sha1 = \"${OBJ_SHA1}\";
      sha256 = \"${OBJ_SHA256}\";
    };"
fi
echo -n "
  };
"
