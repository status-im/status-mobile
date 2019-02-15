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
