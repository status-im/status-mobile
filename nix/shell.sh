#!/usr/bin/env bash

GREEN='\033[0;32m'
NC='\033[0m'

export TERM=xterm # fix for colors
shift # we remove the first -c from arguments
if ! command -v "nix" >/dev/null 2>&1; then
  echo -e "${GREEN}Setting up environment...${NC}"
  ./scripts/setup
fi
if command -v "nix" >/dev/null 2>&1 || [ -f ~/.nix-profile/etc/profile.d/nix.sh ]; then
  echo -e "${GREEN}Configuring Nix shell...${NC}";
  if [[ $@ == "ENTER_NIX_SHELL" ]]; then
    . ~/.nix-profile/etc/profile.d/nix.sh && exec nix-shell
  else
    . ~/.nix-profile/etc/profile.d/nix.sh && exec nix-shell --run "$@"
  fi
fi
