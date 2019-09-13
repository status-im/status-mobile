#!/usr/bin/env bash

set -e

gitRoot=$(cd "${BASH_SOURCE%/*}" && git rev-parse --show-toplevel)
resultPath="${gitRoot}/result/"

# cleanup for artifacts created during builds
function cleanup() {
  # clear trapped signals
  trap - EXIT ERR INT QUIT
  # do the actual cleanup, ignore failure 
  if ./nix/clean.sh "${nixResultPath}"; then
    echo "Successful cleanup!"
  elif [[ -n "${IN_CI_ENVIRONMENT}" ]]; then
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
  mkdir -p "${resultPath}"
  cp -vfr ${nixResultPath}/* "${resultPath}"
  chmod -R u+w "${resultPath}"
}

# Load Nix profile
. ~/.nix-profile/etc/profile.d/nix.sh

targetAttr="${1}"
shift

if [[ -z "${targetAttr}" ]]; then
  echo "First argument is madatory and has to specify the Nix attribute!"
  exit 1
fi

# Some defaults flags, --pure could be optional in the future.
# NOTE: The --keep-failed flag can be used for debugging issues.
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

echo "Extracting result: ${nixResultPath}"
extractResults "${nixResultPath}"

echo "SUCCESS"
