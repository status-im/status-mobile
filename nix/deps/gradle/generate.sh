#!/usr/bin/env bash

set -e

if [[ -z "${IN_NIX_SHELL}" ]]; then
    echo "Remember to call 'make shell'!"
    exit 1
fi

# This script takes care of generating/updating the maven-sources.nix file
# representing the offline Maven repo containing the dependencies
# required to build the project

GIT_ROOT=$(cd "${BASH_SOURCE%/*}" && git rev-parse --show-toplevel)
THIS_SCRIPT=$(realpath --relative-to="${GIT_ROOT}" ${BASH_SOURCE})
CUR_DIR=$(cd "${BASH_SOURCE%/*}" && pwd)
source "${GIT_ROOT}/scripts/colors.sh"

PROJ_LIST="${CUR_DIR}/proj.list"
DEPS_LIST="${CUR_DIR}/deps.list"
DEPS_URLS="${CUR_DIR}/deps.urls"
DEPS_JSON="${CUR_DIR}/deps.json"

# Raise limit of file descriptors
ulimit -n 16384

echo "Regenerating Nix files..."

# Gradle needs to be run in 'android' subfolder
cd $GIT_ROOT/android

# Stop gradle daemons to avoid locking
./gradlew --stop >/dev/null

# Generate list of Gradle sub-projects ----------------------------------------
${CUR_DIR}/get_projects.sh | sort -u -o ${PROJ_LIST}

echo -e "Found ${GRN}$(wc -l < ${PROJ_LIST})${RST} sub-projects..."

# Check each sub-project in parallel, the ":" is for local deps ---------------
PROJECTS=$(cat ${PROJ_LIST})
${CUR_DIR}/get_deps.sh ":" ${PROJECTS[@]} | sort -uV -o ${DEPS_LIST}

echo -e "${CLR}Found ${GRN}$(wc -l < ${DEPS_LIST})${RST} direct dependencies..."

# Find download URLs for each dependency --------------------------------------
cat ${DEPS_LIST} | go-maven-resolver | sort -uV -o ${DEPS_URLS}

echo -e "${CLR}Found ${GRN}$(wc -l < ${DEPS_URLS})${RST} dependency URLs..."

# Open the Nix attribute set --------------------------------------------------
echo -n "[" > ${DEPS_JSON}

# Format URLs into a Nix consumable file.
URLS=$(cat ${DEPS_URLS})
parallel --will-cite --keep-order \
    "${CUR_DIR}/url2json.sh" \
    ::: ${URLS} \
    >> ${DEPS_JSON}

# Drop tailing comma on last object, stupid JSON
sed -i '$ s/},/}/' ${DEPS_JSON}

# Close the Nix attribute set
echo "]" >> ${DEPS_JSON}

REL_DEPS_JSON=$(realpath --relative-to=${PWD} ${DEPS_JSON})
echo -e "${CLR}Generated Nix deps file: ${REL_DEPS_JSON#../}"
echo -e "${GRN}Done${RST}"
