#!/usr/bin/env bash

#
# This script takes a deps.list file and builds a Nix expression
# that can be used by maven-repo-builder.nix to produce a path to
# a local Maven repository.
#

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
    grep '<shadedClassifierName>nodeps</shadedClassifierName>' "${1}" \
        >/dev/null 2>&1 
}

function fetch_and_template_file() {
    local FILENAME="${1}"
    local OBJ_URL OBJ_NIX_FETCH_OUT OBJ_NAME OBJ_PATH

    OBJ_URL="${REPO_URL}/${PKG_PATH}/${FILENAME}"
    if ! OBJ_NIX_FETCH_OUT=$(nix_fetch "${OBJ_URL}"); then
        echo " ! Failed to fetch: ${OBJ_URL}" >&2
        return 1
    fi

    OBJ_NAME="${FILENAME}"
    OBJ_PATH=$(get_nix_path "${OBJ_NIX_FETCH_OUT}")
    echo -n ",
      \"${OBJ_NAME}\": {
        \"sha1\": \"$(get_sha1 "${OBJ_PATH}")\",
        \"sha256\": \"$(get_nix_sha "${OBJ_NIX_FETCH_OUT}")\"
      }"
}

function fetch_and_template_file_no_fail() {
    fetch_and_template_file "${1}" 2>/dev/null || true
}

if [[ -z "${1}" ]]; then
    echo "Required POM URL argument not given!" >&2
    exit 1
fi
POM_URL=${1}
# Drop the POM extension.
PKG_URL_NO_EXT="${POM_URL%.pom}"
# Name of package without extension.
PKG_NAME="$(basename "${PKG_URL_NO_EXT}")"

echo -en "${CLR} - Nix entry for: ${1##*/}\r" >&2

REPO_URL=$(match_repo_url "${PKG_URL_NO_EXT}")

if [[ -z "${REPO_URL}" ]]; then
    echo " ! Repo URL not found: %s" "${REPO_URL}" >&2
    exit 1
fi
# Get the relative path without full URL
PKG_PATH="${PKG_URL_NO_EXT#"${REPO_URL}/"}"
PKG_PATH="$(dirname "${PKG_PATH}")"

# Both JARs and AARs have a POM
POM_NIX_FETCH_OUT=$(nix_fetch "${PKG_URL_NO_EXT}.pom")
POM_PATH=$(get_nix_path "${POM_NIX_FETCH_OUT}")
POM_NAME=$(basename "${PKG_URL_NO_EXT}.pom")
if [[ -z "${POM_PATH}" ]]; then
    echo " ! Failed to fetch: ${PKG_URL_NO_EXT}.pom" >&2
    exit 1
fi
POM_SHA256=$(get_nix_sha "${POM_NIX_FETCH_OUT}")
POM_SHA1=$(get_sha1 "${POM_PATH}")

# Identify packaging type, JAR, AAR, bundle, or just POM.
OBJ_TYPE=$(grep -oP '<packaging>\K[^<]+' "${POM_PATH}")

# Some deps are Eclipse plugins, and we don't need those.
if [[ "${OBJ_TYPE}" == "eclipse-plugin" ]]; then
    exit 0
fi

# Format into a Nix attrset entry
echo -ne "
  {
    \"path\": \"${PKG_PATH}\",
    \"repo\": \"${REPO_URL}\",
    \"files\": {
      \"${POM_NAME}\": {
        \"sha1\": \"${POM_SHA1}\",
        \"sha256\": \"${POM_SHA256}\"
      }"

# Some deps are just POMs, in which case there is no JAR to fetch.
[[ "${OBJ_TYPE}" == "" ]]        && fetch_and_template_file_no_fail "${PKG_NAME}.jar"
[[ "${OBJ_TYPE}" == "jar" ]]     && fetch_and_template_file "${PKG_NAME}.jar"
[[ "${OBJ_TYPE}" == "bundle" ]]  && fetch_and_template_file "${PKG_NAME}.jar"
[[ "${OBJ_TYPE}" =~ aar* ]]      && fetch_and_template_file "${PKG_NAME}.aar"
[[ "${OBJ_TYPE}" == "aar.asc" ]] && fetch_and_template_file "${PKG_NAME}.${OBJ_TYPE}"
pom_has_nodeps_jar "${POM_PATH}" && fetch_and_template_file "${PKG_NAME}-nodeps.jar"

echo -e '\n    }\n  },'
