#!/usr/bin/env bash

# This script takes care of generating/updating the maven-sources.nix file
# representing the offline Maven repo containing the dependencies
# required to build the project

set -Eeo pipefail

if [[ -z "${IN_NIX_SHELL}" ]]; then
    echo "Remember to call 'make shell'!"
    exit 1
fi

GIT_ROOT=$(cd "${BASH_SOURCE%/*}" && git rev-parse --show-toplevel)
source "${GIT_ROOT}/scripts/colors.sh"

CUR_DIR=$(cd "${BASH_SOURCE%/*}" && pwd)
PROJ_LIST="${CUR_DIR}/proj.list"
DEPS_LIST="${CUR_DIR}/deps.list"
DEPS_URLS="${CUR_DIR}/deps.urls"
DEPS_JSON="${CUR_DIR}/deps.json"

# Raise limit of file descriptors
ulimit -n 16384

# Generate list of Gradle sub-projects.
function gen_proj_list() {
    "${CUR_DIR}/get_projects.sh" | sort -u -o "${PROJ_LIST}"
    echo -e "Found ${GRN}$(wc -l < "${PROJ_LIST}")${RST} sub-projects..."
}

# Check each sub-project in parallel, the ":" is for local deps.
function gen_deps_list() {
    PROJECTS=$(cat "${PROJ_LIST}")
    # WARNING: The ${PROJECTS[@]} needs to remain unquoted to expand correctly.
    # shellcheck disable=SC2068
    "${CUR_DIR}/get_deps.sh" ":" ${PROJECTS[@]} | sort -uV -o "${DEPS_LIST}"
    echo -e "${CLR}Found ${GRN}$(wc -l < "${DEPS_LIST}")${RST} direct dependencies..."
}

# Find download URLs for each dependency.
function gen_deps_urls() {
    go-maven-resolver < "${DEPS_LIST}" | sort -uV -o "${DEPS_URLS}"
    echo -e "${CLR}Found ${GRN}$(wc -l < "${DEPS_URLS}")${RST} dependency URLs..."
}

# Generate the JSON that Nix will consume.
function gen_deps_json() {
    # Open the Nix attribute set.
    echo -n "[" > "${DEPS_JSON}"

    # Format URLs into a Nix consumable file.
    URLS=$(cat "${DEPS_URLS}")
    # Avoid rate limiting by using 4 of the available threads.
    parallel --will-cite --keep-order --jobs 4 \
        "${CUR_DIR}/url2json.sh" \
        ::: "${URLS}" \
        >> "${DEPS_JSON}"

    # Drop tailing comma on last object, stupid JSON
    sed -i '$ s/},/}/' "${DEPS_JSON}"

    # Close the Nix attribute set
    echo "]" >> "${DEPS_JSON}"
}

# ------------------------------------------------------------------------------
echo "Regenerating Nix files..."

# Gradle needs to be run in 'android' subfolder
cd "${GIT_ROOT}/android"

# Stop gradle daemons to avoid locking
./gradlew --stop >/dev/null

# A way to run a specific stage of generation
if [[ -n "${1}" ]] && type "${1}" > /dev/null; then
    ${1}; exit 0
elif [[ -n "${1}" ]]; then
    echo "No such function: ${1}"; exit 1
fi

# Run each stage in order
gen_proj_list
gen_deps_list
# FIXME: Temporary fix for missing package.
echo 'org.jetbrains.kotlin.jvm:org.jetbrains.kotlin.jvm.gradle.plugin:1.6.10' >> "${DEPS_LIST}"
echo 'com.android.tools.lint:lint-gradle:30.3.1' >> "${DEPS_LIST}"
gen_deps_urls
gen_deps_json

REL_DEPS_JSON=$(realpath --relative-to="${PWD}" "${DEPS_JSON}")
echo -e "${CLR}Generated Nix deps file: ${REL_DEPS_JSON#../}"
echo -e "${GRN}Done${RST}"
