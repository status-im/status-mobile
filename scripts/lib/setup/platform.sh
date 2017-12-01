#!/usr/bin/env bash

OS=$(uname -s)

function is_macos() {
  [[ "$OS" =~ Darwin ]]
}

function is_linux() {
  [[ "$OS" =~ Linux ]]
}

function is_ubuntu() {
  ! is_linux && return 1
  (lsb_release -a 2>/dev/null | grep -q Ubuntu) && (command -v apt >/dev/null)
}

function exit_unless_os_supported() {
  if ! is_macos && ! is_ubuntu; then
    cecho "@red[[This install script currently supports Mac OS X and Ubuntu \
via apt. To manually install, please visit the wiki for more information:]]

    @blue[[https://wiki.status.im/Building_Status]]"

    echo

    exit 1
  fi
}
