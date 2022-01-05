#!/usr/bin/env bash

# Checking group ownership to identify installation type.
file_group() {
    UNAME=$(uname -s)
    if [[ "${UNAME}" == "Linux" ]]; then
        stat -Lc "%G" "${1}" 2>/dev/null
    elif [[ "${UNAME}" == "Darwin" ]]; then
        stat -Lf "%Sg" "${1}" 2>/dev/null
    fi
}

os_name() {
    source /etc/os-release 2>/dev/null
    echo "${NAME}"
}

is_arch_linux() {
    [[ -f /etc/arch-release ]]
}

nix_install_type() {
    NIX_STORE_DIR_GROUP=$(file_group /nix/store)
    if [[ "$(os_name)" =~ NixOS ]]; then
        echo "nixos"
    elif [[ "${NIX_STORE_DIR_GROUP}" == "nixbld" ]]; then
        echo "multi"
    elif [[ "${NIX_STORE_DIR_GROUP}" == "${USER}" ]]; then
        echo "single"
    elif [[ "${NIX_STORE_DIR_GROUP}" == "" ]]; then
        echo "No Nix installtion detected!" >&2
        echo "none"
    else
        echo "Unknown Nix installtion type!" >&2
        exit 1
    fi
}
