#!/usr/bin/env bash

GIT_ROOT=$(git rev-parse --show-toplevel)

function download_url() {
  if program_exists "aria2c"; then
    aria2c --max-connection-per-server=16 --split=16 --dir="$1" -o "$2" "$3"
  else
    wget --show-progress --output-document="$1/$2" "$3"
  fi
}

function install_nix() {
  if ! program_exists nix; then
    touch -a "${HOME}/.bash_profile"

    local required_version=$(toolversion nix)
    bash <(curl https://nixos.org/releases/nix/nix-${required_version}/install) --no-daemon
    if [ $? -eq 0 ]; then
      if is_linux && [ -f ~/.bashrc ] && ! grep -qF ".nix-profile/etc/profile.d/nix.sh" ~/.bashrc; then
        # For some reason, new terminals are not started as login shells, so .profile and .bash_profile are not sourced.
        # To get around it, we add Nix initialization to .bashrc as well, if it exists
        echo "if [ -e ${HOME}/.nix-profile/etc/profile.d/nix.sh ]; then . ${HOME}/.nix-profile/etc/profile.d/nix.sh; fi # added by make setup Status script" >> ~/.bashrc
      fi

      local buildFlags=''
      [ -n "$CI_ENVIRONMENT" ] && buildFlags='-v'
      . ${HOME}/.nix-profile/etc/profile.d/nix.sh && \
      NIX_CONF_DIR=$(cd "${BASH_SOURCE%/*}" && pwd)/nix \
        nix build --no-link ${buildFlags} -f ${GIT_ROOT}/default.nix

      if [ $? -eq 0 ]; then
        echo -e "${YELLOW}**********************************************************************************************************"
        echo "The Nix package manager was successfully installed. Please run \`make shell\` to initialize the Nix environment."
        echo "If this doesn't work, you might have to sign out and back in, in order for the environment to be reloaded."
        echo -e "**********************************************************************************************************${NC}"
      fi
    else
      echo "Please see https://nixos.org/nix/manual/#chap-installation"
    fi
    exit
  fi
}

function install_android_sdk() {
  if [ -z "$ANDROID_SDK_ROOT" ]; then
    return 0
  fi

  if [ -d "$ANDROID_SDK_ROOT" ]; then
    cecho "@green[[Android SDK already installed.]]"
  else
    local required_version=$(toolversion android-sdk)
    mkdir -p $ANDROID_SDK_ROOT
    cecho "@cyan[[Downloading Android SDK.]]"

    local PLATFORM=$(echo "$OS" | tr '[:upper:]' '[:lower:]')
    download_url . sdk-tools-${PLATFORM}.zip https://dl.google.com/android/repository/sdk-tools-${PLATFORM}-${required_version}.zip && \
      cecho "@cyan[[Extracting Android SDK to $ANDROID_SDK_ROOT.]]" && \
      unzip -q -o ./sdk-tools-${PLATFORM}.zip -d "$ANDROID_SDK_ROOT" && \
      rm -f ./sdk-tools-${PLATFORM}.zip && \
      cecho "@blue[[Android SDK installation completed in $ANDROID_SDK_ROOT.]]" || \
      exit $?
  fi

  [ $? -eq 0 ] && use_android_sdk

  return 0
}

function dependency_setup() {
  cecho "@b@blue[[\$ $@]]"
  echo

  cd "$(repo_path)"
  eval "$@" || (cecho "@b@red[[Error running dependency install '$@']]" && exit 1)

  echo
  echo "  + done"
  echo
}

function use_android_sdk() {
  if [ -d "$ANDROID_SDK_ROOT" ]; then
    local ANDROID_BUILD_TOOLS_VERSION=$(toolversion android-sdk-build-tools)
    local ANDROID_PLATFORM_VERSION=$(toolversion android-sdk-platform)
    touch ~/.android/repositories.cfg
    echo y | sdkmanager "platform-tools" "build-tools;$ANDROID_BUILD_TOOLS_VERSION" "platforms;$ANDROID_PLATFORM_VERSION"
    yes | sdkmanager --licenses
  else
    local _docUrl="https://status.im/build_status/"
    cecho "@yellow[[ANDROID_SDK_ROOT environment variable not defined, please install the Android SDK.]]"
    cecho "@yellow[[(see $_docUrl).]]"

    echo

    exit 1
  fi

  scripts/generate-keystore.sh
}
