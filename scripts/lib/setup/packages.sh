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
# Aptitude
###############

function apt_update() {
  ! is_linux && return 1

  sudo apt update
}

function apt_is_installed() {
  ! is_linux && return 1

  local package=$1

  dpkg -s "$package" >/dev/null 2>&1
}

function apt_install() {
  ! is_linux && return 1

  local package=$1

  if apt_is_installed "$package"; then
    cecho "+ $package already installed... skipping."
  else
    sudo apt install -y "$package"
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
