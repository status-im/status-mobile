#!/usr/bin/env bash

#
# This script is used by the Makefile to have an implicit nix-shell.
# The following environment variables modify the script behavior:
# - TARGET: This attribute is passed as `targets` arg to Nix, limiting the scope
#     of the Nix expressions.
# - _NIX_ATTR: Used to specify an attribute set from inside the expression in `default.nix`.
#     This allows for drilling down into a specific attribute in Nix expressions.
# - _NIX_PURE: This variable allows for making the shell pure with the use of --pure.
#     Take note that this makes Nix tools like `nix-build` unavailable in the shell.
# - _NIX_KEEP: This variable allows specifying which env vars to keep for Nix pure shell.

GIT_ROOT=$(cd "${BASH_SOURCE%/*}" && git rev-parse --show-toplevel)
source "${GIT_ROOT}/scripts/colors.sh"
source "${GIT_ROOT}/nix/scripts/source.sh"

export TERM=xterm # fix for colors
shift # we remove the first -c from arguments

shellArgs=(
  "--show-trace"
)

if [[ -n "${TARGET}" ]]; then
    shellArgs+=("--argstr target ${TARGET}")
else
    echo -e "${YLW}Env is missing TARGET, assuming default target.${RST} See nix/README.md for more details." 1>&2
fi

if [[ "$TARGET" =~ (linux|windows|darwin|macos) ]]; then
  # This is a dirty workaround because 'yarn install' is an impure operation,
  # so we need to call it from an impure shell.
  # Hopefully we'll be able to fix this later on with something like yarn2nix
  nix-shell ${shellArgs[@]} --run "scripts/prepare-for-desktop-platform.sh" || exit
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
  shellArgs+=("--arg config {$config}")
fi

# if _NIX_ATTR is specified we shouldn't use shell.nix, the path will be different
entryPoint="shell.nix"
if [ -n "${_NIX_ATTR}" ]; then
  shellArgs+=("--attr ${_NIX_ATTR}")
  entryPoint="default.nix"
fi

# ENTER_NIX_SHELL is the fake command used when `make shell` is run.
# It is just a special string, not a variable, and a marker to not use `--run`.
if [[ $@ == "ENTER_NIX_SHELL" ]]; then
  echo -e "${GRN}Configuring ${_NIX_ATTR:-default} Nix shell for target '${TARGET:-default}'...${RST}" 1>&2
  exec nix-shell ${shellArgs[@]} ${entryPoint}
else
  # Not all builds are ready to be run in a pure environment
  if [[ -n "${_NIX_PURE}" ]]; then
    shellArgs+=("--pure")
    pureDesc='pure '
  fi
  # This variable allows specifying which env vars to keep for Nix pure shell
  # The separator is a semicolon
  if [[ -n "${_NIX_KEEP}" ]]; then
    shellArgs+=("--keep ${_NIX_KEEP//;/ --keep }")
  fi
  echo -e "${GRN}Configuring ${pureDesc}${_NIX_ATTR:-default} Nix shell for target '${TARGET}'...${RST}" 1>&2
  exec nix-shell ${shellArgs[@]} --run "$@" ${entryPoint}
fi
