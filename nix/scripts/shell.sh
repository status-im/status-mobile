#!/usr/bin/env bash

#
# This script is used by the Makefile to have an implicit nix-shell.
# The following environment variables modify the script behavior:
# - TARGET: This attribute is passed via --attr to Nix, defining the scope.
# - _NIX_PURE: This variable allows for making the shell pure with the use of --pure.
#     Take note that this makes Nix tools like `nix-build` unavailable in the shell.
# - _NIX_KEEP: This variable allows specifying which env vars to keep for Nix pure shell.

GIT_ROOT=$(cd "${BASH_SOURCE%/*}" && git rev-parse --show-toplevel)
source "${GIT_ROOT}/scripts/colors.sh"
source "${GIT_ROOT}/nix/scripts/source.sh"

export TERM=xterm # fix for colors
shift # we remove the first -c from arguments

nixArgs=(
  "--show-trace"
)

if [[ -z "${TARGET}" ]]; then
    TARGET="default"
    echo -e "${YLW}Missing TARGET, assuming default target.${RST} See nix/README.md for more details." 1>&2
fi
entryPoint="default.nix"
nixArgs+=("--attr shells.${TARGET}")


if [[ "$TARGET" =~ (linux|windows|darwin|macos) ]]; then
  # This is a dirty workaround because 'yarn install' is an impure operation,
  # so we need to call it from an impure shell.
  # Hopefully we'll be able to fix this later on with something like yarn2nix
  nix-shell ${nixArgs[@]} --run "scripts/prepare-for-desktop-platform.sh" || exit
fi

config=''
if [ -n "${STATUS_GO_SRC_OVERRIDE}" ]; then
  config+="status-im.status-go.src-override=\"${STATUS_GO_SRC_OVERRIDE}\";"
fi
if [ -n "${NIMBUS_SRC_OVERRIDE}" ]; then
  config+="status-im.nimbus.src-override=\"${NIMBUS_SRC_OVERRIDE}\";"
fi
config+="status-im.build-type=\"${BUILD_TYPE}\";"

if [ -n "$config" ]; then
  nixArgs+=("--arg config {$config}")
fi

# This variable allows specifying which env vars to keep for Nix pure shell
# The separator is a colon
if [[ -n "${_NIX_KEEP}" ]]; then
  nixArgs+=("--keep ${_NIX_KEEP//,/ --keep }")
fi

# Not all builds are ready to be run in a pure environment
if [[ -n "${_NIX_PURE}" ]]; then
  nixArgs+=("--pure")
  pureDesc='pure '
fi

echo -e "${GRN}Configuring ${pureDesc}Nix shell for target '${TARGET}'...${RST}" 1>&2

# ENTER_NIX_SHELL is the fake command used when `make shell` is run.
# It is just a special string, not a variable, and a marker to not use `--run`.
if [[ "${@}" == "ENTER_NIX_SHELL" ]]; then
  exec nix-shell ${nixArgs[@]} ${entryPoint}
else
  exec nix-shell ${nixArgs[@]} --run "$@" ${entryPoint}
fi
