#!/usr/bin/env bash

# This script makes sure we have Nix tools available

GIT_ROOT=$(cd "${BASH_SOURCE%/*}" && git rev-parse --show-toplevel)

function source_nix() {
  # Just stop if Nix is already available
  if [[ -x $(command -v nix) ]]; then
    return
  elif [[ -f "${HOME}/.nix-profile/etc/profile.d/nix.sh" ]]; then
    # Load Nix profile if it exists
    source "${HOME}/.nix-profile/etc/profile.d/nix.sh"
  else
    # Setup Nix if not available
    ${GIT_ROOT}/nix/scripts/setup.sh
  fi

  # Verify Nix is available
  if [[ ! -x $(command -v nix) ]]; then
    echo "Nix not available, sourcing profile failed!" > /dev/stderr
    exit 1
  fi
}

source_nix
