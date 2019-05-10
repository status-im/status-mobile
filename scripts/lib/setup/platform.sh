#!/usr/bin/env bash

OS=$(uname -s)

function is_macos() {
  [[ "$OS" =~ Darwin ]]
}

function is_linux() {
  [[ "$OS" =~ Linux ]]
}

function is_nixos() {
  is_linux && [[ "$(uname -v)" == *NixOS* ]]
}

function exit_unless_os_supported() {
  if [ "$IN_NIX_SHELL" == 'pure' ]; then
    cecho "@red[[This install script is not supported in a pure Nix shell]]"

    echo

    exit 1
  fi

  if ! is_macos && ! is_linux; then
    cecho "@red[[This install script currently supports Mac OS X and Linux \
via apt. To manually install, please visit the docs for more information:]]

    @blue[[https://status.im/build_status]]"

    echo

    exit 1
  fi
}
