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
  elif [ "$IN_NIX_SHELL" != 'pure' ]; then
    echo -e "${GREEN}Setting up environment...${NC}"
    ./scripts/setup

    . ~/.nix-profile/etc/profile.d/nix.sh
  fi
fi

if command -v "nix" >/dev/null 2>&1; then
  platform=${TARGET_OS:=all}
  if [ "$platform" != 'all' ] && [ "$platform" != 'android' ] && [ "$platform" != 'ios' ]; then
    # This is a dirty workaround to the fact that 'yarn install' is an impure operation, so we need to call it from an impure shell. Hopefull we'll be able to fix this later on with something like yarn2nix
    nix-shell --show-trace --argstr target-os ${TARGET_OS} --run "scripts/prepare-for-desktop-platform.sh" || exit
  fi

  entry_point='shell.nix'
  attrset_option=''
  if [ -n "${_NIX_ATTR}" ]; then
    entry_point='default.nix'
    attrset_option="-A ${_NIX_ATTR}"
  fi

  attrStr="${_NIX_ATTR}"
  [ -n "$attrStr" ] && attrStr="$attrStr "
  if [[ $@ == "ENTER_NIX_SHELL" ]]; then
    echo -e "${GREEN}Configuring ${attrStr}Nix shell for target '${TARGET_OS}'...${NC}"
    exec nix-shell --show-trace --argstr target-os ${TARGET_OS} ${attrset_option} ${entry_point}
  else
    is_pure=''
    if [[ "${TARGET_OS}" != 'all' ]] && [[ "${TARGET_OS}" != 'ios' ]] && [[ "${TARGET_OS}" != 'windows' ]]; then
      is_pure='--pure'
      pure_desc='pure '
    fi
    # This variable allows specifying which env vars to keep for Nix pure shell
    # The separator is a semicolon
    keep_opts=''
    if [[ -n "${NIX_KEEP}" ]]; then
      keep_opts="--keep ${NIX_KEEP//;/ --keep }"
    fi
    echo -e "${GREEN}Configuring ${pure_desc}${attrStr}Nix shell for target '${TARGET_OS}'...${NC}"
    exec nix-shell ${is_pure} ${keep_opts} --show-trace --argstr target-os ${TARGET_OS} ${attrset_option} --run "$@" ${entry_point}
  fi
fi
