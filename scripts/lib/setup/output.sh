#!/usr/bin/env bash

function load_color_support() {
  # Check if we're in a terminal
  if test -t 1; then
    local color_count=$(tput colors)

    # Only load colors if our terminal supports them
    if test -n "$color_count" && test $color_count -ge 8; then
      _color_bold="$(tput bold)"
      _color_underline="$(tput sgr 0 1)"
      _color_red="$(tput setaf 1)"
      _color_green="$(tput setaf 2)"
      _color_yellow="$(tput setaf 3)"
      _color_blue="$(tput setaf 4)"
      _color_magenta="$(tput setaf 5)"
      _color_cyan="$(tput setaf 6)"
      _color_white="$(tput setaf 7)"
      _color_reset="$(tput sgr0)"
    fi
  fi
}

load_color_support

function cecho() {
  local colorized=$(
    echo "$@" | sed -E \
      -e 's/((@(red|green|yellow|blue|magenta|cyan|white|reset|b|u))+)[[]{2}(.*)[]]{2}/\1\4@reset/g' \
      -e "s/@red/${_color_red}/g" \
      -e "s/@green/${_color_green}/g" \
      -e "s/@yellow/${_color_yellow}/g" \
      -e "s/@blue/${_color_blue}/g" \
      -e "s/@magenta/${_color_magenta}/g" \
      -e "s/@cyan/${_color_cyan}/g" \
      -e "s/@white/${_color_white}/g" \
      -e "s/@reset/${_color_reset}/g" \
      -e "s/@b/${_color_bold}/g" \
      -e "s/@u/${_color_underline}/g"
  )

  echo "$colorized"
}

function setup_header() {
  local header=$1

  cecho "@b@green[[$header]]"
  echo
}

function already_installed() {
  local package=$1

  cecho "+ $package already installed... skipping"
}

function setup_complete() {
  cecho "@b@blue[[Setup complete!]]
===============

There are a few @b[[manual steps]] you might want to do:

1. Optionally set up Genymotion if you don't want to use Android Virtual Device:

    @blue[[https://www.genymotion.com]]

2. Setup Android Development Environment + Simulator:

    @blue[[https://facebook.github.io/react-native/docs/getting-started.html]]

3. Add your SSH public key to Github if it isn't already in there.

To build the app, please run one of the @b[[make release-*]] commands.
"

  echo
}
