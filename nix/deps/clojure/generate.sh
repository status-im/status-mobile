#!/usr/bin/env bash

set -e

GIT_ROOT=$(cd "${BASH_SOURCE%/*}" && git rev-parse --show-toplevel)
source "${GIT_ROOT}/scripts/colors.sh"

if [[ -z "${IN_NIX_SHELL}" ]]; then
    echo "Remember to call 'make shell'!"
    exit 1
fi

CLASSPATH_FILE="${GIT_ROOT}/nix/deps/clojure/deps.list"
JSON_DEPS_FILE="${GIT_ROOT}/nix/deps/clojure/deps.json"
MAVEN_CACHE_PATH="${HOME}/.m2/repository"

declare -A REPOS=(
  [central]="https://repo1.maven.org/maven2"
  [clojars]="https://repo.clojars.org"
)

function gen_deps_list() {
    # split into separate lines
    CLASSPATH_LINES=$(yarn shadow-cljs classpath | tr ':' '\n')
    # remove unnecessary lines
    CLASSPATH_LINES=$(echo "${CLASSPATH_LINES}" | grep -vE '^(\$|yarn|Done|shadow-cljs|src|test)')
    # remove local home path
    CLASSPATH_LINES=$(echo "${CLASSPATH_LINES}" | sed "s#${MAVEN_CACHE_PATH}/##")
    # print cleaned up entries from classpath
    echo "${CLASSPATH_LINES}" | sort | uniq
}

function get_repo_for_dir() {
    # This file has different name depending on Maven version
    if [[ -f "${1}/_remote.repositories" ]]; then
        REPO_FILE="${1}/_remote.repositories"
    elif [[ -f "${1}/_maven.repositories" ]]; then
        REPO_FILE="${1}/_maven.repositories"
    else
        echo -e "${RED}Cannot find Maven repo file for:${RST} ${1}" >&2
        exit 1
    fi
    grep -oP '.*>\K\w+' "${REPO_FILE}" | uniq
}

function get_nix_sha() {
    nix hash-file --base32 --type sha256 "$1" 2> /dev/null
}

function nix_entry_from_jar() {
    JAR_REL_PATH="${1}"
    JAR_REL_NAME="${JAR_REL_PATH%.jar}"
    JAR_PATH="${MAVEN_CACHE_PATH}/${JAR_REL_PATH}"
    JAR_NAME=$(basename "${JAR_PATH}")
    JAR_DIR=$(dirname "${JAR_PATH}")
    # POM might have a slightly different name
    POM_PATH=$(echo ${JAR_DIR}/*.pom)

    REPO_NAME=$(get_repo_for_dir "${JAR_DIR}")
    REPO_URL=${REPOS[${REPO_NAME}]}

    JAR_SHA1=$(cat "${JAR_PATH}.sha1")
    JAR_SHA256=$(get_nix_sha "${JAR_PATH}")

    POM_SHA1=$(cat "${POM_PATH}.sha1")
    POM_SHA256=$(get_nix_sha "${POM_PATH}")
    
    # Format into a Nix attrset entry
    echo -n "
  {
    \"path\": \"${JAR_REL_NAME}\",
    \"host\": \"${REPO_URL}\",
    \"pom\": {
      \"sha1\": \"${POM_SHA1}\",
      \"sha256\": \"${POM_SHA256}\"
    },
    \"jar\": {
      \"sha1\": \"${JAR_SHA1}\",
      \"sha256\": \"${JAR_SHA256}\"
    }
  }"
}

# generate the deps.list file with relative paths
gen_deps_list > "${CLASSPATH_FILE}"
echo "Saved Clojure deps list to: ${CLASSPATH_FILE}" >&2

# add the header which defines repo URLs
echo -en "[" > "${JSON_DEPS_FILE}"

# for each dependency generate an attrset with metadata
DEPS=$(cat "${CLASSPATH_FILE}")
LAST=$(tail -n1 "${CLASSPATH_FILE}")
for DEP in ${DEPS}; do
   nix_entry_from_jar "${DEP}" >> "${JSON_DEPS_FILE}"
   # JSON is stupid and requires no comma on last element
   [[ "${DEP}" != "${LAST}" ]] && echo "," >> "${JSON_DEPS_FILE}"
done

# close the attrset
echo -e "\n]" >> "${JSON_DEPS_FILE}"
echo "Generated Clojure deps for Nix: ${JSON_DEPS_FILE}" >&2
