#!/usr/bin/env bash

GIT_ROOT=$(cd "${BASH_SOURCE%/*}" && git rev-parse --show-toplevel)
source "${GIT_ROOT}/scripts/colors.sh"

NIX_VERSION="2.3.12"
NIX_INSTALL_URL="https://nixos.org/releases/nix/nix-${NIX_VERSION}/install"
NIX_INSTALL_SHA256="468a49a1cef293d59508bb3b62625dfcd99ec00334a14309f125cf8de513d5f1"
NIX_INSTALL_PATH="/tmp/nix-install-${NIX_VERSION}"

function install_nix() {
  # Don't break people's profiles
  export NIX_INSTALLER_NO_MODIFY_PROFILE=1
  # Fix for installing on MacOS Catalina
  export NIX_IGNORE_SYMLINK_STORE=1
  # Download installer and verify SHA256
  curl -s "${NIX_INSTALL_URL}" -o "${NIX_INSTALL_PATH}"
  echo "${NIX_INSTALL_SHA256}  ${NIX_INSTALL_PATH}" | sha256sum -c
  chmod +x "${NIX_INSTALL_PATH}"
  # Run the installer
  "${NIX_INSTALL_PATH}" --no-daemon
  if [ $? -eq 0 ]; then
    echo -e "${GRN}The Nix package manager was successfully installed.${RST}"
  else
    echo -e "${RED}Failed to install Nix package manager!${RST}" >&2
    echo "Please see: https://nixos.org/nix/manual/#chap-installation" >&2
    exit 1
  fi
}

if [[ ! -x "$(command -v git)" ]]; then
  echo -e "${RED}The 'curl' utility is required for Nix installation.${RST}" >&2
  exit 1
fi

if [[ "$(source /etc/os-release && echo "${NAME}")" == *NixOS* ]]; then
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
install_nix
echo -e "${YLW}See STARTING_GUIDE.md if you're new here.${RST}"
