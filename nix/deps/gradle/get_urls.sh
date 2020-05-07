#!/usr/bin/env bash

GIT_ROOT=$(cd "${BASH_SOURCE%/*}" && git rev-parse --show-toplevel)
CUR_DIR=$(cd "${BASH_SOURCE%/*}" && pwd)
# Gradle needs to be run in 'android' subfolder
cd ${GIT_ROOT}/android

function join_by {
    local IFS="$1"; shift; echo "$*";
}

# sources REPOS associative array
source ${CUR_DIR}/repos.sh
mavenSourcesSedFilter=$(join_by '|' ${REPOS[@]})

# Converts a URL to a Maven package ID (e.g. https://dl.google.com/dl/android/maven2/android/arch/core/common/1.0.0/common-1.0.0 -> android.arch.core:common:1.0.0)
function getPackageIdFromURL() {
  local url="${1}"
  local path=$(echo ${url} | sed -E "s;(${mavenSourcesSedFilter})/(.+);\2;")

  IFS='/' read -ra tokens <<< "${path}"
  local groupLength=$(( ${#tokens[@]} - 3 ))
  local groupId=''
  for ((i=0;i<${groupLength};i++)); do
    if [ $i -eq 0 ]; then
      groupId=${tokens[i]}
    else
      groupId="${groupId}.${tokens[i]}"
    fi
  done
  artifactId=${tokens[-3]}
  version="${tokens[-2]}"
  echo "${groupId}:${artifactId}:${version}"
}

# Formats the components of a Maven package ID as a URL path component (e.g. android/arch/core/common/1.0.0/common-1.0.0)
function getPath() {
  local tokens=("${@}")
  local groupId=${tokens[0]}
  local artifactId=${tokens[1]}
  local version=${tokens[2]}
  if [[ "${version}" == "jar" ]] || [[ "${version}" == "aar" ]]; then
    local version=${tokens[3]}
  fi

  groupId=$(echo ${groupId} | tr '.' '/')
  echo "${groupId}/${artifactId}/${version}/${artifactId}-${version}"
}

# Tries to download a POM, fails on 404
function tryGetPOMFromURL() {
    # Using nix-prefetch-url so it's already downloaded for next step
    FETCH_OUT=$(nix-prefetch-url --print-path "${1}.pom" 2>/dev/null)
    RVAL=${?}
    POM_PATH=$(echo "${FETCH_OUT}" | tail -n1)
    # We symlink the POM it can be used with retrieveAdditionalDependencies
    if [[ ${RVAL} -eq 0 ]] && [[ ! -L "${TMP_POM_SYMLINK}" ]]; then
        ln -s "${POM_PATH}" "${TMP_POM_SYMLINK}"
    fi
    return ${RVAL}
}

# Given the components of a package ID, will loop through known repositories to figure out a source for the package
function determineArtifactUrl() {
  # Parse dependency ID into components (group ID, artifact ID, version)
  IFS=':' read -ra tokens <<< "${1}"
  local groupId=${tokens[0]}
  [ -z "${groupId}" ] && return
  local path=$(getPath "${tokens[@]}")

  # check old file for URL to avoid making requests if possible
  if [[ -s "${CUR_DIR}/deps.urls.old" ]]; then
      local url=$(grep ${path} ${CUR_DIR}/deps.urls.old | sort -V | head -n1)
      if [[ -n "${url}" ]]; then
          # Make sure we symlink the POM
          tryGetPOMFromURL "${url}"
          echo "${url}"
          return
      fi
  fi

  # otherwise try to find it via fetching
  for mavenSourceUrl in ${REPOS[@]}; do
    if tryGetPOMFromURL "${mavenSourceUrl}/${path}"; then
      echo "${mavenSourceUrl}/${path}"
      return
    fi
  done
  echo "<NOTFOUND>"
}

function retrieveAdditionalDependencies() {
  # It is not enough to output the dependencies in deps, we must also ask maven to report
  # the dependencies for each individual POM file. Instead of parsing the dependency tree itself though,
  # we look at what packages maven downloads from the internet into the local repo,
  # which avoids us having to do a deep search, and does not report duplicates
  echo -n > ${TMP_MVN_DEP_TREE}

  mvn dependency:list --batch-mode --file "${1}" \
      --define includeScope=compile \
      --define excludeScope=test \
      --define maven.repo.local=${MVN_REPO_CACHE} \
      > ${TMP_MVN_DEP_TREE} 2>&1 || echo -n

  local additional_deps=( $(cat ${TMP_MVN_DEP_TREE} \
    | grep -E 'Downloaded from [^:]+: [^ ]+\.(pom|jar|aar)' \
    | sed -E "s;^\[INFO\] Downloaded from [^:]+: ([^ ]+)\.(pom|jar|aar) .*$;\1;") )

  local missing_additional_deps=( $(cat ${TMP_MVN_DEP_TREE} \
    | grep -E "The POM for .+:.+:(pom|jar):.+ is missing" \
    | sed -E "s;^.*The POM for (.+:.+:(pom|jar):.+) is missing.*$;\1;") )

  for additional_dep_url in ${additional_deps[@]}; do
    local additional_dep_id=$(getPackageIdFromURL ${additional_dep_url})

    # See if we already have this dependency in $deps
    local alreadyExists=0
    for _dep in ${deps[@]}; do
      if [[ "${additional_dep_id}" = "${_dep}" ]]; then
        alreadyExists=1
        break
      fi
    done
    [[ ${alreadyExists} -eq 0 ]] && echo "${additional_dep_url}" || continue
  done

  for additional_dep_id in ${missing_additional_deps[@]}; do
    # these are un-fetchable
    [[ "${additional_dep_id}" = *":unspecified" ]] && continue

    # See if we already have this dependency in $deps
    local alreadyExists=0
    for _dep in ${deps[@]}; do
      if [ "${additional_dep_id}" = "${_dep}" ]; then
        alreadyExists=1
        break
      fi
    done

    if [[ ${alreadyExists} -eq 0 ]]; then
      artifactUrl=$(determineArtifactUrl ${additional_dep_id})

      [[ -z "${artifactUrl}" ]] && continue

      if [[ "${artifactUrl}" = "<NOTFOUND>" ]]; then
        # Some dependencies don't contain a normal format, so we ignore them (e.g. `com.squareup.okhttp:okhttp:{strictly`)
        echo -e "\033[2K ! Failed to find URL for: ${additional_dep_id}" >&2
        continue
      fi

      echo "${artifactUrl}"
    fi
  done
}

# The only argument is the file with the deps list
DEP="${1}"
if [[ -z "${DEP}" ]]; then
    echo "No argument given!" >&2
    exit 1
fi

# This will be a symlink to downloaded POM file
TMP_POM_SYMLINK=$(mktemp --tmpdir -u fetch-maven-deps-XXXXX.pom)
TMP_MVN_DEP_TREE=$(mktemp --tmpdir mvn-dep-tree-XXXXX.txt)

# To make maven always re-download dependencies
# WARNING: If this is not cleared after full run this won't work
TMP_MVN_REPO=$(mktemp -d) # this is for when this script is run by hand
MVN_REPO_CACHE=${MVN_REPO_CACHE:-${TMP_MVN_REPO}}

trap "rm -rf ${TMP_POM_SYMLINK} ${TMP_MVN_DEP_TREE}" ERR EXIT HUP INT

echo -en "\033[2K - Finding URL: ${DEP}\r" >&2

FOUND_URL=$(determineArtifactUrl ${DEP})

if [ -z "${FOUND_URL}" ] || [ "${FOUND_URL}" = "<NOTFOUND>" ]; then
    # Some dependencies don't contain a normal format
    echo -e "\033[2K ! Failed to find URL for: ${DEP}" >&2
    exit 1
fi

echo "${FOUND_URL}"

retrieveAdditionalDependencies "${TMP_POM_SYMLINK}"
