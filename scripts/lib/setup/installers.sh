#!/usr/bin/env bash

_localPropertiesPath=./android/local.properties

function downloadUrl() {
  if program_exists "aria2c"; then
    aria2c --max-connection-per-server=16 --split=16 --dir="$1" -o "$2" "$3"
  else
    wget --show-progress --output-document="$1/$2" "$3"
  fi
}

function install_nsis() {
  # NSIS (Nullsoft Scriptable Install System) is a professional open source system to create Windows installers. It is designed to be as small and flexible as possible and is therefore very suitable for internet distribution.
  linux_install nsis
}

function install_node() {
  if ! nvm_installed && ! program_exists 'node'; then
    install_nvm
  fi

  if nvm_installed; then
    install_node_via_nvm
  else
    install_node_via_package_manager
  fi
}

function install_and_setup_package_manager() {
  if is_macos; then
    install_homebrew_if_needed
    brew_tap "caskroom/cask"
  elif is_linux; then
    # Linux
    buildtools=(
      autoconf
      automake
      build-essential
      cmake
      curl
      g++
      lib32ncurses5    # required for Android SDK
      lib32stdc++6     # required for Android SDK
      libssl-dev
      libtool
      make
      pkg-config       # required to e.g. build watchman
      python-dev
      rlwrap           # required to use clj
      wget
      unzip
    )

    for package in "${buildtools[@]}"; do
      linux_install "$package"
    done
  fi
}

function install_wget() {
  if is_macos; then
    brew_install wget 1.19.4 0505e48743f82ac2e9f5d0c9d6d811949982262e
  fi
  # it's installed on ubuntu/debian by default
}

function needs_java8_linux() {
  ! program_exists "java" || !(java -version 2>&1 | grep -q "1.8.0")
}

function install_java8() {
  if is_macos; then
    brew_cask_install "caskroom/versions/java8"
  elif is_linux; then
    if needs_java8_linux; then
      sudo su << EOF
       add-apt-repository ppa:webupd8team/java -y
       apt update
       echo oracle-java8-installer shared/accepted-oracle-license-v1-1 select true | /usr/bin/debconf-set-selections
       apt install -y oracle-java8-installer
EOF
    else
      already_installed "java8"
    fi
  fi
}

function install_leiningen() {
  if is_macos; then
    brew_install leiningen $(toolversion leiningen) f7e10afc6d04a13d28e825db71326d16c12e9724
  elif is_linux; then
    install_leiningen_linux
  fi
}

function install_leiningen_linux() {
  local destination=/usr/bin/lein
  local required_version=$(toolversion leiningen)
  if ! program_version_exists "lein" "$required_version"; then
    cecho "@b@blue[[+ Installing lein...]]"

    sudo su << EOF
      curl --silent \
        https://raw.githubusercontent.com/technomancy/leiningen/stable/bin/lein \
        -o $destination

      chmod 755 $destination

      cd $HOME && lein downgrade $required_version
EOF
  else
    already_installed "lein"
  fi
}

function install_clojure_cli() {
  if is_macos; then
    local required_version=$(toolversion clojure_cli)
    brew_install clojure $required_version 90ea0cb4b194282b5906108dcec522c5a1ed7ce0
  elif is_linux; then
    install_clojure_cli_linux
  fi
}

function install_clojure_cli_linux() {
  local required_version=$(toolversion clojure_cli)

  if ! program_exists "clojure" || ! echo "$(clj -r <<< '(System/exit 0)')" | grep -q -o ${required_version%.*}; then
    cecho "@b@blue[[+ Installing Clojure CLI...]]"

    local current_dir=$(pwd)
    sudo su << EOF
      curl --silent \
        https://download.clojure.org/install/linux-install-${required_version}.sh \
        -o /tmp/clojure

      chmod +x /tmp/clojure
      cd /tmp
      ./clojure
EOF
    cd "$current_dir"
  else
    already_installed "Clojure CLI"
  fi
}

function install_watchman() {
  local required_version=$(toolversion watchman)
  if is_macos; then
    brew_install watchman $required_version 1a41406af8db6bbc0c94811cf60043a7436be3c4
  elif is_linux; then
    if ! program_version_exists "watchman" "$required_version"; then
      local current_dir=$(pwd)
      local clone_path="/tmp/watchman"

      rm -rf $clone_path
      git clone https://github.com/facebook/watchman.git $clone_path
      cd $clone_path
      git checkout v$required_version
      ./autogen.sh && \
        ./configure && \
        make && \
        sudo make install

      cd "$current_dir"
    else
      already_installed "watchman"
    fi
  fi
}

function install_homebrew_if_needed() {
  ! is_macos && return 1

  if test ! $(which brew); then
    cecho "@b@blue[[+ Installing homebrew]]"

    ruby -e "$(curl -fsSL \
      https://raw.githubusercontent.com/Homebrew/install/master/install)"

    brew update
  else
    already_installed "Homebrew"
  fi
}

function export_android_sdk_vars() {
  local profile
  local target_path
  if is_macos; then
    profile=$HOME/.bash_profile
  elif is_linux; then
    profile=$HOME/.bashrc
  fi

  [ -f $profile ] || touch $profile
  if ! grep -Fq "export ANDROID_SDK_ROOT=" $profile; then
    echo "export ANDROID_HOME=\"$1\"" >> $profile && \
    echo "export ANDROID_SDK_ROOT=\"$1\"" >> $profile && \
    echo "export PATH=\"$1/tools:$1/tools/bin:\$PATH\"" >> $profile
  fi
  export ANDROID_HOME="$1" && \
  export ANDROID_SDK_ROOT="$1" && \
  export PATH="$1/tools:$1/tools/bin:$PATH"
}

function install_android_sdk() {
  if is_macos; then
    brew_cask_install android-sdk

    [ -z "$ANDROID_SDK_ROOT" ] && export_android_sdk_vars /usr/local/share/android-sdk
  elif is_linux; then
    install_android_sdk_linux
  fi

  use_android_sdk
}

function install_android_sdk_linux() {
  if [ -z "$ANDROID_SDK_ROOT" ]; then
    if grep -Fq "sdk.dir" $_localPropertiesPath; then
      local _sdkParentDir="$(awk -F'=' "/^sdk.dir=/{print \$2}" "$_localPropertiesPath")"
      export_android_sdk_vars $_sdkParentDir
      cecho "@green[[Android SDK already declared.]]"
    else
      local required_version=$(toolversion android-sdk)
      local _sdkParentDir=$HOME/Android/Sdk
      mkdir -p $_sdkParentDir
      cecho "@cyan[[Downloading Android SDK.]]"

      downloadUrl . sdk-tools-linux.zip https://dl.google.com/android/repository/sdk-tools-linux-${required_version}.zip && \
        cecho "@cyan[[Extracting Android SDK to $_sdkParentDir.]]" && \
        unzip -q -o ./sdk-tools-linux.zip -d "$_sdkParentDir" && \
        rm -f ./sdk-tools-linux.zip && \
        _sdkTargetDir="$_sdkParentDir" && \
        echo "sdk.dir=$_sdkTargetDir" | tee -a $_localPropertiesPath && \
        export_android_sdk_vars $_sdkParentDir && \
        cecho "@blue[[Android SDK installation completed in $_sdkTargetDir.]]" || \
        return 0
    fi
  else
    if ! grep -Fq "sdk.dir" $_localPropertiesPath; then
      echo "sdk.dir=$ANDROID_SDK_ROOT" | tee -a $_localPropertiesPath
    fi
    cecho "@green[[Android SDK already declared.]]"
  fi

  return 1
}

function install_maven() {
  local required_version=$(toolversion maven)
  brew_install maven $required_version 4c23c22dc71eadaeb7b25d6e6c10fd53bfc26976
  linux_install maven
}

function install_react_native_cli() {
  cd "$(repo_path)"

  local npm_command='npm'
  local required_version=$(toolversion react_native_cli)

  if is_linux && ! nvm_installed; then
    # aptitude version of node requires sudo for global install
    npm_command="sudo $npm_command"
  fi

  if npm list -g "react-native-cli@{required_version}" &>/dev/null; then
    already_installed "react-native-cli@{required_version}"
  else
    $npm_command install -g react-native-cli@${required_version}
  fi
}

function install_yarn() {
  local required_version=$(toolversion yarn)
  if ! program_exists "yarn"; then
    npm install -g yarn@$required_version # Install the required yarn version
  else
    cecho "+ yarn already installed... skipping."
  fi

  if program_exists "yarn"; then
    local yarn_version=$(yarn -v)
    if [[ $yarn_version != "$required_version" ]]; then
      cecho "@b@yellow[[+ yarn version $yarn_version is installed. Downloading yarn version $required_version in the local repo.]]"
      yarn policies set-version $required_version
    fi
  fi
}

function install_nvm() {
  local required_version=$(toolversion nvm)

  if ! program_version_exists 'nvm' "$required_version"; then
    cecho "@b@blue[[+ Installing nvm $required_version]]"

    # Ensure a profile file exists, otherwise NVM will not add its configuration anywhere
    # and will therefore be inaccessible
    [ -f "~/.bash_profile" ] || touch ~/.bash_profile

    sudo apt install -y build-essential libssl-dev
    source scripts/3rd-party/nvm/${required_version}/install.sh
    load_nvm_if_available
  else
    cecho "+ nvm already installed... skipping."
  fi
}

function install_node_via_nvm() {
  local required_version=$(toolversion node)

  cd "$(repo_path)"

  if [ "$(nvm version v""$required_version"")" = "N/A" ]; then
    cecho "@b@blue[[+ Installing Node $required_version]]"

    nvm install $required_version
    nvm alias status-im $required_version
    nvm use status-im

  else
    if [ "$(nvm version status-im)" != "v$required_version" ]; then
      nvm alias status-im $required_version
    fi
    nvm use status-im

    local version=$(node -v)
    cecho "+ Node already installed (status-im $version via NVM)... skipping."
  fi
}

function install_node_via_package_manager() {
  local required_version=$(toolversion node)
  if ! program_version_exists "node" "v$required_version"; then
    if is_macos; then
      brew_install node "$required_version" b801cc6b71e7c09448b4f823e493710665de68eb
    elif is_linux; then
      curl -sL https://deb.nodesource.com/setup_${required_version%%\.*}.x | sudo -E bash -
      linux_update

      linux_install nodejs
    fi
  else
    cecho \
      "+ Node already installed ($(node -v) via package manager)... skipping."
  fi
}

function required_pod_version() {
  cat "$(repo_path)/ios/Podfile.lock" | grep "COCOAPODS: " | awk '{ print $2 }'
}

function correct_pod_version_is_installed() {
  ! program_exists "pod" && return 1

  [[ "$(required_pod_version)" == "$(pod --version)" ]]
}

function using_rvm() {
  program_exists "rvm"
}

function initialize_rvm() {
  cd "$(repo_path)"

  if [ ! -e "$(repo_path)/.ruby-version" ]; then
    rvm use --default > /dev/null
    echo "$(rvm current)" > .ruby-version
  fi

  rvm use . >/dev/null
}

function using_cocoapods() {
  is_macos
}

function install_cocoapods() {
  ! using_cocoapods && return 1

  local gem_command="sudo gem"
  local destination="system Ruby"
  local version=$(required_pod_version)

  if using_rvm; then
    initialize_rvm

    gem_command="gem"
    destination="RVM ($(rvm current))"
  fi

  if ! program_exists "pod"; then
    $gem_command install cocoapods -v "$version"
  elif ! correct_pod_version_is_installed; then
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
  eval "$@" || (cecho "@b@red[[Error running dependency install '$@']]" && exit 1)

  echo
  echo "  + done"
  echo
}

function use_android_sdk() {
  if [ -n "$ANDROID_SDK_ROOT" ]; then
    if ! grep -Fq "sdk.dir" $_localPropertiesPath; then
      echo "sdk.dir=$ANDROID_SDK_ROOT" | tee -a $_localPropertiesPath
    fi

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

function install_android_ndk() {
  if grep -Fq "ndk.dir" $_localPropertiesPath; then
    cecho "@green[[Android NDK already declared.]]"
  else
    local ANDROID_NDK_VERSION=$(toolversion android-ndk)
    local _ndkParentDir=~/Android/Sdk
    mkdir -p $_ndkParentDir
    cecho "@cyan[[Downloading Android NDK.]]"

    local PLATFORM="linux"
    if is_macos; then
        PLATFORM="darwin"
    fi

    downloadUrl . android-ndk.zip https://dl.google.com/android/repository/android-ndk-$ANDROID_NDK_VERSION-$PLATFORM-x86_64.zip && \
      cecho "@cyan[[Extracting Android NDK to $_ndkParentDir.]]" && \
      unzip -q -o ./android-ndk.zip -d "$_ndkParentDir" && \
      rm -f ./android-ndk.zip && \
      _ndkTargetDir="$_ndkParentDir/$(ls $_ndkParentDir | grep ndk)" && \
      echo "ndk.dir=$_ndkTargetDir" | tee -a $_localPropertiesPath && \
      cecho "@blue[[Android NDK installation completed in $_ndkTargetDir.]]"
  fi
}
