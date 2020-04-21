#!/usr/bin/env bash

set -e

GIT_ROOT=$(cd "${BASH_SOURCE%/*}" && git rev-parse --show-toplevel)
source "${GIT_ROOT}/scripts/colors.sh"

# Purging /nix on NixOS would be disasterous
if [[ -f "/etc/os-release" ]]; then
    OS_NAME=$(awk -F= '/^NAME/{print $2}' /etc/os-release)
    if [[ "$OS_NAME" == "NixOS" ]]; then
        echo -e "${RED}You should not purge Nix files on NixOS!${RST}" >&2
        exit
    fi
fi

NIX_ROOT="/nix"
if [[ $(uname -s) == "Darwin" ]]; then
    # Special case due to read-only root on MacOS Catalina
    NIX_ROOT="/opt/nix"
fi

sudo rm -rf ${NIX_ROOT}/* ~/.nix-profile ~/.nix-defexpr ~/.nix-channels ~/.cache/nix ~/.status .nix-gcroots
echo -e "${GRN}Purged all Nix files from your system.${RST}" >&2
