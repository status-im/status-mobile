#!/usr/bin/env bash

set -e

GIT_ROOT=$(cd "${BASH_SOURCE%/*}" && git rev-parse --show-toplevel)

# cleanup for artifacts created during builds
function cleanup() {
  # clear trapped signals
  trap - EXIT ERR INT QUIT
  # do the actual cleanup, ignore failure 
  if ./nix/clean.sh "${nixResultPath}"; then
    echo "Successful cleanup!"
  elif [[ -n "${JENKINS_URL}" ]]; then
    # in CI removing some paths can fail due to parallel builds
    echo "Ignoring cleanup failure in CI."
  else
    echo "Failed cleanup!"
    exit 1
  fi
}

trap cleanup EXIT ERR INT QUIT

# build output will end up under /nix, we have to extract it
function extractResults() {
  local nixResultPath="$1"
  echo "Saving build result: ${nixResultPath}"
  mkdir -p ${GIT_ROOT}/result
  cp -vfr ${nixResultPath}/* ${GIT_ROOT}/result/
  chmod u+w -R ${GIT_ROOT}/result/
}

# Load Nix profile
. ~/.nix-profile/etc/profile.d/nix.sh

targetAttr="${1}"
shift

if [[ -z "${targetAttr}" ]]; then
  echo "First argument is mandatory and has to specify the Nix attribute!"
  exit 1
fi

# Some defaults flags, --pure could be optional in the future
nixOpts=(
  "--pure"
  "--fallback"
  "--no-out-link"
  "--show-trace"
  "--argstr target-os ${TARGET_OS}"
  "--attr ${targetAttr}"
  "${@}"
  "default.nix"
)

# Run the actual build
echo "Running: nix-build ${nixOpts[@]}"
nixResultPath=$(nix-build ${nixOpts[@]})

extractResults "${nixResultPath}"

echo "SUCCESS"
