#!/usr/bin/env bash
# This script makes sure we have Nix tools available
set -e

GIT_ROOT=$(cd "${BASH_SOURCE%/*}" && git rev-parse --show-toplevel)
source "${GIT_ROOT}/nix/scripts/lib.sh"
source "${GIT_ROOT}/scripts/colors.sh"

source_nix_profile() {
    NIX_INSTALL_TYPE=$(nix_install_type)
    if [[ "${NIX_INSTALL_TYPE}" == "multi" ]]; then
        source "/nix/var/nix/profiles/default/etc/profile.d/nix-daemon.sh"
    elif [[ "${NIX_INSTALL_TYPE}" == "single" ]]; then
        source "${HOME}/.nix-profile/etc/profile.d/nix.sh"
    elif [[ "${NIX_INSTALL_TYPE}" == "nixos" ]]; then
        echo "Sourcing profile not necessary on NixOS!" >&2
    fi
}

main() {
    # Just stop if Nix is already available
    if [[ -x $(command -v nix) ]]; then
        return
    fi

    # Setup Nix if not available
    if [[ ! -d /nix ]]; then
        "${GIT_ROOT}/nix/scripts/setup.sh"
    fi

    # Load Nix profile
    source_nix_profile

    # Verify Nix is available
    if [[ ! -x $(command -v nix) ]]; then
        echo -e "${RED}Nix not available, sourcing profile failed!${RST}" >&2
        exit 1
    fi
}

main
