#!/usr/bin/env bash
# This script installs a specific version of Nix.
set -eo pipefail

GIT_ROOT=$(cd "${BASH_SOURCE%/*}" && git rev-parse --show-toplevel)
source "${GIT_ROOT}/scripts/colors.sh"
source "${GIT_ROOT}/nix/scripts/lib.sh"
source "${GIT_ROOT}/nix/scripts/version.sh"

nix_install() {
    # Download installer and verify SHA256>
    curl -sSf "${NIX_INSTALL_URL}" -o "${NIX_INSTALL_PATH}"
    echo "${NIX_INSTALL_SHA256}  ${NIX_INSTALL_PATH}" | sha256sum -c
    chmod +x "${NIX_INSTALL_PATH}"

    # Identify installation type.
    if [[ -z "${NIX_INSTALL_OPTS}" ]]; then
        if [[ "$(uname -r)" =~ microsoft ]]; then
            # Systemd is not started by default on WSL.
            NIX_INSTALL_OPTS="--no-daemon"
        elif [[ "$(uname -s)" == "Darwin" ]]; then
            # Single-user not supported on Darwin.
            NIX_INSTALL_OPTS="--daemon"
        elif [[ "$(uname -s)" == "Linux" ]]; then
            # Open file limit issues on Linux.
            # https://github.com/NixOS/nix/issues/6007
            # Alson known issues with nix-daemon.socket on Arch.
            NIX_INSTALL_OPTS="--no-daemon"
        fi
    fi

    # Run the installer
    "${NIX_INSTALL_PATH}" "${NIX_INSTALL_OPTS}"
    if [[ $? -eq 0 ]]; then
        echo -e "${GRN}The Nix package manager was successfully installed.${RST}"
    else
        echo -e "${RED}Failed to install Nix package manager!${RST}" >&2
        echo "Please see: https://nixos.org/nix/manual/#chap-installation" >&2
        exit 1
    fi

    # Additional fixes
    nix_make_user_trusted
    nix_daemon_restart
}

# Fix for warnings about ignoring binary cache.
nix_make_user_trusted() {
    # Single-user installations do not have this issue.
    [[ ! -f /etc/nix/nix.conf ]] && return
    echo -e "Adding ${USER} to trusted-users..." >&2
    if grep trusted-users /etc/nix/nix.conf 2>/dev/null; then
        sed -i "s/trusted-users = \(.*\)$/trusted-users = \1 $USER/" /etc/nix/nix.conf
    else
        echo "trusted-users = $USER" | sudo tee -a /etc/nix/nix.conf >/dev/null
    fi
}

if [[ ! -x "$(command -v sha256sum)" ]]; then
    echo -e "${RED}The 'sha256sum' utility is required for Nix installation.${RST}" >&2
    echo -e "${YLW}Install 'coreutils' package on your system.${RST}" >&2
    exit 1
fi

if [[ ! -x "$(command -v curl)" ]]; then
    echo -e "${RED}The 'curl' utility is required for Nix installation.${RST}" >&2
    exit 1
fi

if [[ "$(source /etc/os-release 2>/dev/null && echo "${NAME}")" == *NixOS* ]]; then
    echo -e "${GRN}Already running NixOS.${RST}"
    exit
fi

if [[ -x "$(command -v nix)" ]]; then
    echo -e "${GRN}Nix package manager already installed.${RST}"
    exit
fi

if [[ "${IN_NIX_SHELL}" == 'pure' ]]; then
    echo -e "${GRN}Already in a pure Nix shell.${RST}"
    exit
fi

# If none of the checks before succeeded we need to install Nix
echo -e "${GRN}Setting up Nix package manager...${RST}"
nix_install
echo -e "${YLW}See STARTING_GUIDE.md if you're new here.${RST}"
