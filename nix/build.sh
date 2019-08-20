#!/usr/bin/env bash

set -e

# cleanup for artifacts created during builds
function cleanup() {
  # clear trapped signals
  trap - EXIT ERR INT QUIT
  # do the actual cleanup
  ./nix/clean.sh "${nixResultPath}"
}

trap cleanup EXIT ERR INT QUIT

# build output will end up under /nix, we have to extract it
function extractResults() {
  local nixResultPath="$1"
  echo "Saving build result: ${nixResultPath}"
  mkdir -p result
  cp -vfr ${nixResultPath}/* result/
  chmod u+w -R result/
}

# Load Nix profile
. ~/.nix-profile/etc/profile.d/nix.sh

targetAttr="${1}"
shift

if [[ -z "${targetAttr}" ]]; then
  echo "First argument is madatory and has to specify the Nix attribute!"
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
