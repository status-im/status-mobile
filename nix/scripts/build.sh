#!/usr/bin/env bash

set -e

GIT_ROOT=$(cd "${BASH_SOURCE%/*}" && git rev-parse --show-toplevel)
resultPath="${GIT_ROOT}/result/"
source "${GIT_ROOT}/scripts/colors.sh"
source "${GIT_ROOT}/nix/scripts/source.sh"

# cleanup for artifacts created during builds
function cleanup() {
  # clear trapped signals
  trap - EXIT ERR INT QUIT
  # do the actual cleanup, ignore failure
  if ${GIT_ROOT}/nix/scripts/clean.sh "${nixResultPath}"; then
    echo -e "${GRN}Successful cleanup!${RST}"
  elif [[ -n "${JENKINS_URL}" ]]; then
    # in CI removing some paths can fail due to parallel builds
    echo -e "${YLW}Ignoring cleanup failure in CI.${RST}"
  else
    echo -e "${RED}Failed cleanup!${RST}"
    exit 1
  fi
}

trap cleanup EXIT ERR INT QUIT

# build output will end up under /nix, we have to extract it
function extractResults() {
  local nixResultPath="$1"
  mkdir -p "${resultPath}"
  cp -vfr ${nixResultPath}/* "${resultPath}"
  chmod -R u+w "${resultPath}"
  ls -l "${resultPath}"
}

targetAttr="${1}"
shift

if [[ -z "${targetAttr}" ]]; then
  echo -e "${RED}First argument is mandatory and has to specify the Nix attribute!${RST}"
  exit 1
fi

# Some defaults flags, --pure could be optional in the future.
# NOTE: The --keep-failed flag can be used for debugging issues.
nixOpts=(
  "--pure"
  "--fallback"
  "--no-out-link"
  "--show-trace"
  "--attr ${targetAttr}"
  "${@}"
  "default.nix"
)

# This variable allows specifying which env vars to keep for Nix pure shell
# The separator is a semicolon
if [[ -n "${_NIX_KEEP}" ]]; then
  nixOpts+=("--keep ${_NIX_KEEP//;/ --keep }")
fi

# Run the actual build
echo "Running: nix-build ${nixOpts[@]}"
nixResultPath=$(nix-build ${nixOpts[@]})

echo "Extracting result: ${nixResultPath}"
extractResults "${nixResultPath}"

echo -e "${GRN}SUCCESS${RST}"
