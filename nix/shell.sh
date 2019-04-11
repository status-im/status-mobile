#!/usr/bin/env bash

#
# This script is used by the Makefile to have an implicit nix-shell.
#

GREEN='\033[0;32m'
NC='\033[0m'

export TERM=xterm # fix for colors
shift # we remove the first -c from arguments

if ! command -v "nix" >/dev/null 2>&1; then
  if [ -f ~/.nix-profile/etc/profile.d/nix.sh ]; then
    . ~/.nix-profile/etc/profile.d/nix.sh
  else
    echo -e "${GREEN}Setting up environment...${NC}"
    ./scripts/setup

    . ~/.nix-profile/etc/profile.d/nix.sh
  fi
fi

if command -v "nix" >/dev/null 2>&1; then
  echo -e "${GREEN}Configuring Nix shell for target '${TARGET_OS:=all}'...${NC}";
  if [[ $@ == "ENTER_NIX_SHELL" ]]; then
    exec nix-shell --show-trace --argstr target-os ${TARGET_OS}
  else
    exec nix-shell --show-trace --argstr target-os ${TARGET_OS} --run "$@"
  fi
fi
