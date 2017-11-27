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

function install_homebrew_if_needed() {
  ! is_macos && return 0

  if test ! $(which brew); then
    cecho "@b@blue[[+ Installing homebrew]]"

    ruby -e "$(curl -fsSL \
      https://raw.githubusercontent.com/Homebrew/install/master/install)"

    brew update
  else
    already_installed "Homebrew"
  fi
}

function brew_install() {
  local package=$1

  if ! is_macos; then
    return 0
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
    return 0
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
    return 0
  fi

  if ! brew tap | grep -q "$cask"; then
    brew tap "$cask"
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
