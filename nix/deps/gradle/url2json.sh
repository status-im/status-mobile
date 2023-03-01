#!/usr/bin/env bash

#
# This script takes a deps.list file and builds a Nix expression
# that can be used by maven-repo-builder.nix to produce a path to
# a local Maven repository.
#

CUR_DIR=$(cd "${BASH_SOURCE%/*}" && pwd)

# This defines URLs of Maven repos we know about and use.
declare -a REPOS=(
  "https://repo.maven.apache.org/maven2"
  "https://dl.google.com/dl/android/maven2"
  "https://plugins.gradle.org/m2"
  "https://jitpack.io"
)

function nix_fetch() {
    nix-prefetch-url --print-path --type sha256 "${1}" 2>/dev/null
}

function get_nix_path() { echo "${1}" | tail -n1; }
function get_nix_sha() { echo "${1}" | head -n1; }
function get_sha1() { sha1sum "${1}" | cut -d' ' -f1; }

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

function pom_has_nodeps_jar() {
    grep '<shadedClassifierName>nodeps</shadedClassifierName>' $1 \
        2>&1 >/dev/null
}

if [[ -z "${1}" ]]; then
    echo "Required argument not given!" >&2
    exit 1
fi

POM_URL=${1}
# Drop the POM extension
OBJ_REL_URL=${POM_URL%.pom}

echo -en "${CLR} - Nix entry for: ${1##*/}\r" >&2

REPO_URL=$(match_repo_url "${OBJ_REL_URL}")

if [[ -z "${REPO_URL}" ]]; then
    echo "\r\n ? REPO_URL: ${REPO_URL}" >&2
fi
# Get the relative path without full URL
OBJ_REL_NAME="${OBJ_REL_URL#${REPO_URL}/}"

# Both JARs and AARs have a POM
POM_NIX_FETCH_OUT=$(nix_fetch "${OBJ_REL_URL}.pom")
POM_PATH=$(get_nix_path "${POM_NIX_FETCH_OUT}")
if [[ -z "${POM_PATH}" ]]; then
    echo " ! Failed to fetch: ${OBJ_REL_URL}.pom" >&2
    exit 1
fi
POM_SHA256=$(get_nix_sha "${POM_NIX_FETCH_OUT}")
POM_SHA1=$(get_sha1 "${POM_PATH}")

# Identify packaging type, JAR, AAR, or just POM.
OBJ_TYPE_RAW=$(grep -oP '<packaging>\K[^<]+' "${POM_PATH}")
# Bundle is a JAR made using maven-bundle-plugin.
case "${OBJ_TYPE_RAW}" in
    ''|'bundle') OBJ_TYPE=jar;;
    'aar.asc')   OBJ_TYPE=aar;;
    *)           OBJ_TYPE="${OBJ_TYPE_RAW}"
esac

# Some deps are Eclipse plugins, and we don't need those.
if [[ "${OBJ_TYPE}" == "eclipse-plugin" ]]; then
    exit 0
fi

# Some deps are just POMs, in which case there is no JAR to fetch.
if [[ "${OBJ_TYPE}" != "pom" ]]; then
    OBJ_NIX_FETCH_OUT=$(nix_fetch "${OBJ_REL_URL}.${OBJ_TYPE}")
    # If type was a JAR or other, not getting one is an error.
    if [[ ${?} -ne 0 ]]; then
        # POMs without packaging type defined can still include JARs.
        if [[ "${OBJ_TYPE_RAW}" != "" ]]; then
            echo " ! Failed to fetch: ${OBJ_REL_URL}.${OBJ_TYPE}" >&2
            exit 1
        fi
    else
        OBJ_PATH=$(get_nix_path "${OBJ_NIX_FETCH_OUT}")
        OBJ_SHA256=$(get_nix_sha "${OBJ_NIX_FETCH_OUT}")
        OBJ_SHA1=$(get_sha1 "${OBJ_PATH}")
    fi
fi

# Some deps include nodeps JARs which include.
if pom_has_nodeps_jar "${POM_PATH}"; then
    NODEPS_NIX_FETCH_OUT=$(nix_fetch "${OBJ_REL_URL}-nodeps.jar")
    if [[ ${?} -eq 0 ]]; then
        NODEPS_PATH=$(get_nix_path "${NODEPS_NIX_FETCH_OUT}")
        NODEPS_SHA256=$(get_nix_sha "${NODEPS_NIX_FETCH_OUT}")
        NODEPS_SHA1=$(get_sha1 "${NODEPS_PATH}")
    fi
fi

# Format into a Nix attrset entry
echo -ne "
  {
    \"path\": \"${OBJ_REL_NAME}\",
    \"host\": \"${REPO_URL}\",
    \"type\": \"${OBJ_TYPE}\","
if [[ -n "${POM_SHA256}" ]]; then
    echo -n "
    \"pom\": {
      \"sha1\": \"${POM_SHA1}\",
      \"sha256\": \"${POM_SHA256}\"
    }";[[ -n "${OBJ_SHA256}" ]] && echo -n ","
fi
if [[ -n "${OBJ_SHA256}" ]]; then
    echo -n "
    \"jar\": {
      \"sha1\": \"${OBJ_SHA1}\",
      \"sha256\": \"${OBJ_SHA256}\"
    }";[[ -n "${NODEPS_SHA256}" ]] && echo -n ","
fi
if [[ -n "${NODEPS_SHA256}" ]]; then
    echo -n "
    \"nodeps\": {
      \"sha1\": \"${NODEPS_SHA1}\",
      \"sha256\": \"${NODEPS_SHA256}\"
    }"
fi
echo -e "\n  },"
