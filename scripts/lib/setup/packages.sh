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

###############
# Linux
###############

# FIXME This command assumes that package names in different package managers (apt, pacman) are same.
# At this moment, it works as expected because we only call it for installing maven and nodejs.
# If this list grows, please consider adding some sort of mapping mechanism.
function linux_install() {
  ! is_linux && return 0

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
