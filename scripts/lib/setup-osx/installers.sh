#!/usr/bin/env bash

function install_node() {
  if nvm_installed; then
    install_node_via_nvm
  else
    install_node_via_brew
  fi
}

function install_react_native_cli() {
  cd "$(repo_path)"

  if npm list -g | grep -q react-native-cli; then
    already_installed "react-native-cli"
  else
    npm install -g react-native-cli
  fi
}

function install_node_via_nvm() {
  local nvmrc="$(repo_path)/.nvmrc"

  cd "$(repo_path)"

  if [ ! -e "$nvmrc" ]; then
    cecho "@b@blue[[+ Installing latest stable Node version]]"

    nvm install stable
    echo stable > "$nvmrc"

    nvm use
  else
    nvm use >/dev/null

    local version=$(node -v)
    cecho "+ Node already installed ($version via NVM)... skipping."
  fi
}

function install_node_via_brew() {
  if ! program_exists "node"; then
    brew install node
  else
    cecho "+ Node already installed ($(node -v) via Homebrew)... skipping."
  fi
}

function required_pod_version() {
  cat "$(repo_path)/ios/Podfile.lock" | grep "COCOAPODS: " | awk '{ print $2 }'
}

function correct_pod_version_is_installed() {
  ! program_exists "pod" && return 1

  [[ "$(required_pod_version)" == "$(pod --version)" ]]
}

function initialize_rvm() {
  cd "$(repo_path)"

  if [ ! -e "$(repo_path)/.ruby-version" ]; then
    rvm use --default > /dev/null
    echo "$(rvm current)" > .ruby-version
  fi

  rvm use . >/dev/null
}

function install_cocoapods() {
  local gem_command="sudo gem"
  local destination="system Ruby"

  if program_exists "rvm"; then
    initialize_rvm

    gem_command="gem"
    destination="RVM ($(rvm current))"
  fi

  if ! program_exists "pod"; then
    $gem_command install cocoapods
  elif ! correct_pod_version_is_installed; then
    local version=$(required_pod_version)

    cecho "@b@blue[[+ Updating to cocoapods $version]]"

    $gem_command uninstall cocoapods --ignore-dependencies --silent
    $gem_command install cocoapods -v "$version"
  else
    cecho "+ cocoapods already installed to $destination... skipping."
  fi
}

function dependency_setup() {
  cecho "@b@blue[[\$ $@]]"
  echo

  cd "$(repo_path)"
  eval "$@"

  echo
  echo "  + done"
  echo
}
