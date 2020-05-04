#!/usr/bin/env bash

# This script makes sure we have Nix tools available

GIT_ROOT=$(cd "${BASH_SOURCE%/*}" && git rev-parse --show-toplevel)

# Location of profile script for Nix that adjusts PATH
NIX_PROFILE_SH="${HOME}/.nix-profile/etc/profile.d/nix.sh"

function source_nix() {
  # Just stop if Nix is already available
  if [[ -x $(command -v nix) ]]; then
    return
  elif [[ -f "${NIX_PROFILE_SH}" ]]; then
    # Load Nix profile if it exists
    source "${NIX_PROFILE_SH}"
    return
  else
    # Setup Nix if not available
    ${GIT_ROOT}/nix/scripts/setup.sh
  fi

  # Load Nix profile
  source "${NIX_PROFILE_SH}"

  # Verify Nix is available
  if [[ ! -x $(command -v nix) ]]; then
    echo "Nix not available, sourcing profile failed!" > /dev/stderr
    exit 1
  fi
}

source_nix
