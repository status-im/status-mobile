#!/usr/bin/env bash
# This script removes all Nix files.

GIT_ROOT=$(cd "${BASH_SOURCE%/*}" && git rev-parse --show-toplevel)
source "${GIT_ROOT}/nix/scripts/lib.sh"
source "${GIT_ROOT}/scripts/colors.sh"

nix_purge_multi_user() {
    sudo systemctl stop nix-daemon.socket
    sudo systemctl stop nix-daemon.service
    sudo systemctl disable nix-daemon.socket
    sudo systemctl disable nix-daemon.service
    sudo systemctl daemon-reload
    sudo rm -fr /etc/nix
    sudo rm -f /etc/profile.d/nix.sh*

    # Remove nix build users and groups
    for NIX_USER in $(awk -F: '/nixbld/{print $1}' /etc/passwd); do
        sudo userdel "${NIX_USER}"
    done
    sudo groupdel nixbld

    # Restore old shell profiles
    NIX_PROFILE_FILES=(
        /etc/bash.bashrc /etc/bashrc /etc/bash/bashrc
        /etc/zsh.zshhrc /etc/zshrc /etc/zsh/zshrc
    )
    for NIX_FILE in "${NIX_PROFILE_FILES[@]}"; do
        if [[ -f "${NIX_FILE}.backup-before-nix" ]]; then
            sudo mv -f "${NIX_FILE}.backup-before-nix" "${NIX_FILE}"
        fi
    done
}

nix_purge_user_profile() {
    sudo rm -rf \
        ~/.nix-* \
        ~/.cache/nix \
        ~/.config/nixpkgs \
        ${GIT_ROOT}/.nix-gcroots
}

nix_purge_root() {
    NIX_ROOT="/nix"
    if [[ $(uname -s) == "Darwin" ]]; then
        # Special case due to read-only root on MacOS Catalina
        NIX_ROOT="/opt/nix"
    fi
    if [[ -z "${NIX_ROOT}" ]]; then
        echo -e "${RED}Unable to identify Nix root!${RST}" >&2
        exit 1
    fi
    sudo rm -fr "${NIX_ROOT}"
}

NIX_INSTALL_TYPE=$(nix_install_type)

if [[ "${1}" == "--force" ]] && [[ "${NIX_INSTALL_TYPE}" != "nixos" ]]; then
    echo -e "${YLW}Purge forced, no checks performed!${RST}" >&2
    nix_purge_multi_user
    nix_purge_user_profile
    nix_purge_root
    exit
fi

# Purging /nix on NixOS would be disasterous
if [[ "${NIX_INSTALL_TYPE}" == "nixos" ]]; then
    echo -e "${RED}You should not purge Nix files on NixOS!${RST}" >&2
    exit
elif [[ "${NIX_INSTALL_TYPE}" == "none" ]]; then
    echo -e "${YLW}Nothing to remove, Nix not installed.${RST}" >&2
    exit
elif [[ "${NIX_INSTALL_TYPE}" == "multi" ]]; then
    echo -e "${YLW}Detected multi-user Nix installation.${RST}" >&2
    nix_purge_multi_user
elif [[ "${NIX_INSTALL_TYPE}" == "single" ]]; then
    echo -e "${YLW}Detected single-user Nix installation.${RST}" >&2
    nix_purge_user_profile
fi
nix_purge_root

echo -e "${GRN}Purged all Nix files from your system.${RST}" >&2
