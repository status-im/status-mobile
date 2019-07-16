#!/usr/bin/env bash

#
# This script is used by the Makefile to have an implicit nix-shell.
# The following environment variables modify the script behavior:
# - TARGET_OS: This attribute is passed as `target-os` to Nix, limiting the scope
#     of the Nix expressions.
# - _NIX_ATTR: This attribute can be used to specify an attribute set
#     from inside the expression in `default.nix`, allowing drilling down into a specific attribute.
# - _NIX_KEEP: This variable allows specifying which env vars to keep for Nix pure shell. 
#

GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

export TERM=xterm # fix for colors
shift # we remove the first -c from arguments

if ! command -v "nix" >/dev/null 2>&1; then
  if [ -f ~/.nix-profile/etc/profile.d/nix.sh ]; then
    . ~/.nix-profile/etc/profile.d/nix.sh
  elif [ "$IN_NIX_SHELL" != 'pure' ]; then
    echo -e "${GREEN}Setting up environment...${NC}"
    ./scripts/setup

    . ~/.nix-profile/etc/profile.d/nix.sh
  fi
fi

if !command -v "nix" >/dev/null 2>&1; then
  echo "Nix not available, sourcing profile failed!"
  exit 1
fi

shellArgs=(
  "--show-trace"
)

if [[ -n "${TARGET_OS}" ]]; then
    shellArgs+=("--argstr target-os ${TARGET_OS}")
else
    echo -e "${YELLOW}Env is missing TARGET_OS, assuming no target platform.${NC}"
    echo -e "See nix/README.md for more details."
fi

if [[ "$TARGET_OS" =~ (linux|windows) ]]; then
  # This is a dirty workaround because 'yarn install' is an impure operation,
  # so we need to call it from an impure shell.
  # Hopefull we'll be able to fix this later on with something like yarn2nix
  nix-shell ${shellArgs[@]} --run "scripts/prepare-for-desktop-platform.sh" || exit
fi

# if _NIX_ATTR is specified we shouldn't use shell.nix, the path will be different
entryPoint="shell.nix"
if [ -n "${_NIX_ATTR}" ]; then
  shellArgs+=("--attr ${_NIX_ATTR}")
  entryPoint="default.nix"
fi

# this happens if `make shell` is run already in a Nix shell
if [[ $@ == "ENTER_NIX_SHELL" ]]; then
  echo -e "${GREEN}Configuring ${_NIX_ATTR:-default} Nix shell for target '${TARGET_OS}'...${NC}"
  exec nix-shell ${shellArgs[@]} ${entryPoint}
else
  # Not all builds are ready to be run in a pure environment
  if [[ "${TARGET_OS}" =~ (android|macos|linux) ]]; then
    shellArgs+=("--pure")
    pureDesc='pure '
  fi
  # This variable allows specifying which env vars to keep for Nix pure shell
  # The separator is a semicolon
  if [[ -n "${_NIX_KEEP}" ]]; then
    nixShelArgs+=("--keep ${_NIX_KEEP//;/ --keep }")
  fi
  echo -e "${GREEN}Configuring ${pureDesc}${_NIX_ATTR:-default} Nix shell for target '${TARGET_OS}'...${NC}"
  exec nix-shell ${shellArgs[@]} --run "$@" ${entryPoint}
fi
