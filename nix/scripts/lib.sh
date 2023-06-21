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
    else
        case "${NIX_STORE_DIR_GROUP}" in
            "nixbld")   echo "multi";;
            "30000")    echo "multi";;
            "(30000)")  echo "multi";;
            "wheel")    echo "single";;
            "users")    echo "single";;
            "${USER}")  echo "single";;
            "${UID}")   echo "single";;
            "(${UID})") echo "single";;
            "")         echo "none";
                        echo "No Nix installtion detected!" >&2;;
            *)          echo "Unknown Nix installtion type!" >&2; exit 1;;
        esac
    fi
}

nix_root() {
    NIX_ROOT="/nix"
    if [[ $(uname -s) == "Darwin" ]]; then
        # Special case due to read-only root on MacOS Catalina
        NIX_ROOT="/opt/nix"
    fi
    echo "${NIX_ROOT}"
}

nix_current_version() {
    nix-env --version | awk '{print $3}'
}

nix_daemon_restart() {
    if [[ "$(uname -s)" == "Darwin" ]]; then
        echo "Restarting Nix daemon Launchd service" >&2
        sudo launchctl unload /Library/LaunchDaemons/org.nixos.nix-daemon.plist
        sudo launchctl load   /Library/LaunchDaemons/org.nixos.nix-daemon.plist
    elif [[ "$(uname -s)" == "Linux" ]] && [[ "$(nix_install_type)" == "multi" ]]; then
        echo "Restarting Nix daemon Systemd service" >&2
        sudo systemctl daemon-reload
        sudo systemctl restart nix-daemon
    else
        echo "Unknown platform! Unable to restart daemon!" >&2
        exit 1
    fi
}
