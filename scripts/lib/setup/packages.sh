#!/usr/bin/env bash

########
# Install checks
########

function program_exists() {
  local program=$1
  command -v "$program" >/dev/null 2>&1
}



########
# Homebrew
########

function brew_install() {
  local package=$1

  if ! is_macos; then
    return 1
  fi

  if brew list "$package" > /dev/null 2>&1; then
    already_installed "$package"
  else
    brew install $@
  fi
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

    if program_exists "apt"; then
        apt_is_installed "$@"
    elif program_exists "pacman"; then
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
    sudo apt install -y "$package"
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
        sudo pacman -S --noconfirm "$package"
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
  [ -f ~/.nvm/nvm.sh ] && source ~/.nvm/nvm.sh
}

function nvm_installed() {
  program_exists "nvm"
}
