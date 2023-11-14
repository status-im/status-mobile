#!/usr/bin/env bash
# This script upgrades Nix to specific version.
# https://nixos.org/manual/nix/stable/installation/upgrading.html
set -eo pipefail

GIT_ROOT=$(cd "${BASH_SOURCE%/*}" && git rev-parse --show-toplevel)
source "${GIT_ROOT}/scripts/colors.sh"
source "${GIT_ROOT}/nix/scripts/lib.sh"
source "${GIT_ROOT}/nix/scripts/source.sh"
source "${GIT_ROOT}/nix/scripts/version.sh"

nix_upgrade() {
    echo -e "Upgrading Nix interpreter to: ${GRN}${NIX_VERSION}${RST}" >&2
    nix-channel --update
    nix-env --install --attr "nixpkgs.${NIX_PACKAGE}" "nixpkgs.cacert"
    nix_daemon_restart
}

# Allow for sourcing the script
if [[ "${BASH_SOURCE[0]}" != "$0" ]]; then
    return
fi

if [[ "$(nix_current_version)" == "${NIX_VERSION}" ]]; then
    echo -e "Nix interpreter already on version: ${GRN}${NIX_VERSION}${RST}"
    exit 0
fi

NIX_INSTALL_TYPE=$(nix_install_type)
if [[ "${NIX_INSTALL_TYPE}" == "nixos" ]]; then
    echo -e "${YLW}WARNING:${RST} Upgrade Nix in your NixOS configuration!" >&2
    exit 0
elif [[ "${NIX_INSTALL_TYPE}" == "single" ]]; then
    nix_upgrade
elif [[ "${NIX_INSTALL_TYPE}" == "multi" ]]; then
    sudo -i bash -c "source ${PWD}/${0}; nix_upgrade"
fi
