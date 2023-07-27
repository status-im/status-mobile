#!/usr/bin/env bash
# This script is a wrapper around nix-build with some niceties.
set -e

GIT_ROOT=$(cd "${BASH_SOURCE%/*}" && git rev-parse --show-toplevel)
resultPath="${GIT_ROOT}/result/"
source "${GIT_ROOT}/scripts/colors.sh"
source "${GIT_ROOT}/nix/scripts/source.sh"

# cleanup for artifacts created during builds
cleanup() {
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

# If you want to clean after every build set _NIX_CLEAN=true
if [[ -n "${_NIX_CLEAN}" ]]; then
  trap cleanup EXIT ERR INT QUIT
fi

# build output will end up under /nix, we have to extract it
extractResults() {
  local nixResultPath="$1"
  mkdir -p "${resultPath}"
  cp -vfr ${nixResultPath}/* "${resultPath}" | sed 's#'${PWD}'#.#'
  chmod -R u+w "${resultPath}"
}

TARGET="${1}"
shift

if [[ -z "${TARGET}" ]]; then
  echo -e "${RED}First argument is mandatory and has to specify the Nix attribute!${RST}"
  exit 1
fi

# Hack fix for missing Android SDK for aarch64 on Darwin. See systemOverride in `nix/pkgs.nix`.
if [[ "${TARGET}" =~ ^(targets.status-go.mobile.android|targets.mobile.android.release)$ ]]; then
  os=$(uname -s | tr '[:upper:]' '[:lower:]')
  export NIXPKGS_SYSTEM_OVERRIDE="x86_64-${os}"
fi

# Some defaults flags, --pure could be optional in the future.
# NOTE: The --keep-failed flag can be used for debugging issues.
nixOpts=(
  "--pure"
  "--fallback"
  "--no-out-link"
  "--show-trace"
  "--attr" "${TARGET}"
)

# Save derivation from being garbage collected
"${GIT_ROOT}/nix/scripts/gcroots.sh" "${TARGET}" "${@}"

# Run the actual build
echo -e "${GRN}Running:${RST} ${BLD}nix-build "${nixOpts[@]}" ${@}${RST}"
nixResultPath=$(nix-build "${nixOpts[@]}" "${@}" default.nix)

echo -e "\n${YLW}Extracting result${RST}: ${BLD}${nixResultPath}${RST}"

extractResults "${nixResultPath}"

echo -e "\n${GRN}SUCCESS${RST}"
