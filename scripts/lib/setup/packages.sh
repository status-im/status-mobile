#!/usr/bin/env bash

########
# Install checks
########

GIT_ROOT=$(git rev-parse --show-toplevel)

function program_exists() {
  local program=$1
  command -v "$program" >/dev/null 2>&1
}

function program_version_exists() {
  local program=$1
  if ! program_exists "$program"; then
    $(exit 1)
    return
  fi

  local required_version=$2
  if echo "$($program --version)" | grep -q -wo "$required_version\|$required_version[^\.]"; then
    $(exit 0)
    return
  fi
  $(exit 1)
}

function toolversion() {
  ${GIT_ROOT}/scripts/toolversion "${1}"
}

########
# Homebrew
########

function brew_install() {
  local package=$1
  local required_version=$2
  local url="https://raw.githubusercontent.com/Homebrew/homebrew-core/${3:-master}/Formula/$package.rb"

  if ! is_macos; then
    return 1
  fi

  local pkginfo=$(brew info "$package")
  if [ $? -eq 0 ]; then
    # Package already exists locally
    local prefix_expr="HOMEBREW_PREFIX: "
    local brew_base=$(brew config | grep "$prefix_expr" | sed "s|$prefix_expr||")
    local pkg_base="$brew_base/Cellar/$package/"
    local installed_versions=$(echo "$pkginfo" | grep "$pkg_base" | sed -E "s:$pkg_base([^ ]+).*$:\1:")
    local active_version=$(echo "$pkginfo" | grep -E "$pkg_base.*\*$" | sed -E "s:$pkg_base([^ ]+).*\*$:\1:")

    for installed_version in $installed_versions;
    do
      if [ "$installed_version" = "$required_version" ]; then
        if [ "$active_version" = "$required_version" ]; then
          already_installed "$package $required_version"
        else
          brew unlink "$package"
          brew switch "$package" "$required_version"
        fi
        return
      fi
    done
  fi

  brew install $url
}

function brew_cask_install() {
  local package=$1

  if ! is_macos; then
    return 1
  fi

  if brew cask list | grep -q "$package"; then
    already_installed "$package"
  else
    brew cask install $@
  fi
}

function brew_tap() {
  local cask=$1

  if ! is_macos; then
    return 1
  fi

  if ! brew tap | grep -q "$cask"; then
    brew tap "$cask"
  fi
}

###############
# Linux
###############

function linux_update() {
  ! is_linux && return 1

  if program_exists "apt"; then
    apt_update
  elif program_exists "pacman"; then
    pacman_update
  else
    echo "Unsupported Linux distro."
    exit 1;
  fi
}

function linux_is_installed() {
  ! is_linux && return 1

  if program_exists 'apt'; then
    apt_is_installed "$@"
  elif program_exists 'pacman'; then
    pacman_is_installed "$@"
  else
    echo "Unsupported Linux distro."
    exit 1;
  fi
}

# FIXME This command assumes that package names in different package managers (apt, pacman) are same.
# At this moment, it works as expected because we only call it for installing maven and nodejs.
# If this list grows, please consider adding some sort of mapping mechanism.
function linux_install() {
  ! is_linux && return 1

  if program_exists "apt"; then
    apt_install "$@"
  elif program_exists "pacman"; then
    pacman_install "$@"
  else
    echo "Unsupported Linux distro."
    exit 1;
  fi
}


###############
# Aptitude
###############

function apt_update() {
  sudo apt update
}

function apt_is_installed() {
  local package=$1

  dpkg -s "$package" >/dev/null 2>&1
}

function apt_install() {
  local package=$1

  if apt_is_installed "$package"; then
    cecho "+ $package already installed... skipping."
  else
    sudo apt install -y "$package" || exit 1
  fi
}

###############
# Pacman
###############

function pacman_update() {
  sudo pacman -Syu
}

function pacman_is_installed() {
  local package=$1
  pacman -Qs $package >/dev/null 2>&1
}

function pacman_install() {
  local package=$1

  if pacman_is_installed "$package"; then
    cecho "+ $package already installed... skipping."
  else
    sudo pacman -S --noconfirm "$package" || exit 1
  fi
}

###############
# RVM
###############

function load_rvm_if_available() {
  [ -f ~/.rvm/scripts/rvm ] && source ~/.rvm/scripts/rvm
}

###############
# NVM
###############

function load_nvm_if_available() {
  local nvm_path=${NVM_DIR:-~/.nvm}
  [ -f ${nvm_path}/nvm.sh ] && chmod +x ${nvm_path}/nvm.sh && source ${nvm_path}/nvm.sh
}

function nvm_installed() {
  declare -F nvm &>/dev/null
}
