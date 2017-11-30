#!/usr/bin/env bash

OS=$(uname -s)

function is_macos() {
  [[ "$OS" =~ Darwin ]]
}

function exit_unless_mac() {
  if ! is_macos; then
    cecho "@red[[This install script currently supports Mac OS X only. To
manually install, please visit the wiki for more information:]]

    @blue[[https://wiki.status.im/Building_Status]]"

    echo
  fi
}
