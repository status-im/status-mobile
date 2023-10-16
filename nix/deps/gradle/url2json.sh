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

# These files are not necessary for the build process.
FILENAMES_BLACKLIST='-(javadoc|runtime|gwt|headers|sources|src|tests|adapters|modular|site|bin)\.'
FILETYPES_BLACKLIST='(pom|json|zip|module|xml|md5|sha1|sha256|sha512)$'
SIGNATURE_BLACKLIST='(pom|jar|json|zip|module|xml|md5|asc).asc$'

function nix_prefetch() {
    nix store prefetch-file --json "${1}" 2>/dev/null
}

function get_nix_path() { echo "${1}" | jq -r .storePath; }
function get_nix_sha() { echo "${1}" | jq -r .hash; }
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

function guess_pkg_files() {
    # Some deps are just POMs, in which case there is no JAR to fetch.
    [[ "${OBJ_TYPE}" == "" ]]        && echo "${PKG_NAME}.jar"
    [[ "${OBJ_TYPE}" == "jar" ]]     && echo "${PKG_NAME}.jar"
    [[ "${OBJ_TYPE}" == "bundle" ]]  && echo "${PKG_NAME}.jar"
    [[ "${OBJ_TYPE}" =~ aar* ]]      && echo "${PKG_NAME}.aar"
    [[ "${OBJ_TYPE}" == "aar.asc" ]] && echo "${PKG_NAME}.${OBJ_TYPE}"
    pom_has_nodeps_jar "${POM_PATH}" && echo "${PKG_NAME}-nodeps.jar"
}

function get_pkg_files() {
    REPO_URL="${1}"
    PKG_PATH="${2}"
    PKG_NAME="${3}"
    # Google Maven repo doesn't have normal HTML directory listing.
    if [[ "${REPO_URL}" == "https://dl.google.com/dl/android/maven2" ]]; then
        FOUND=$(curl --fail -s "${REPO_URL}/${PKG_PATH}/artifact-metadata.json")
        # Some older packages do not have artifacts-metadata.json.
        if [[ "$?" -eq 0 ]]; then
            FOUND=$(echo "${FOUND}" | jq -r '.artifacts[].name')
        else
            FOUND=''
        fi
    else
        FOUND=$(
            curl -s "${REPO_URL}/${PKG_PATH}/" \
                | htmlq a -a href \
                | grep -e "^${PKG_NAME}"
        )
    fi
    if [[ "${FOUND}" == '' ]]; then
        guess_pkg_files
    else
        # Filter out files we don't actually need for builds.
        echo "${FOUND}" \
            | grep -v -E \
                -e "${FILENAMES_BLACKLIST}" \
                -e "${FILETYPES_BLACKLIST}" \
                -e "${SIGNATURE_BLACKLIST}"
    fi
}

function fetch_and_template_file() {
    local FILENAME="${1}"
    local OBJ_URL OBJ_NIX_FETCH_OUT OBJ_NAME OBJ_PATH

    OBJ_URL="${REPO_URL}/${PKG_PATH}/${FILENAME}"
    if ! OBJ_NIX_FETCH_OUT=$(nix_prefetch "${OBJ_URL}"); then
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
    echo " ! Repo URL not found for: ${POM_URL}" >&2
    exit 1
fi
# Get the relative path without full URL
PKG_PATH="${PKG_URL_NO_EXT#"${REPO_URL}/"}"
PKG_PATH="$(dirname "${PKG_PATH}")"

# Both JARs and AARs have a POM
POM_NIX_FETCH_OUT=$(nix_prefetch "${PKG_URL_NO_EXT}.pom")
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

for FILE in $(get_pkg_files "${REPO_URL}" "${PKG_PATH}" "${PKG_NAME}"); do
    fetch_and_template_file "${FILE}"
done

echo -e '\n    }\n  },'
