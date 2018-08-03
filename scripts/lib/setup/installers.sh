#!/usr/bin/env bash

_localPropertiesPath=./android/local.properties

function install_node() {
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

    #increase watches
    echo 999999 | sudo tee -a /proc/sys/fs/inotify/max_user_watches
    watchman watch-del-all
    watchman shutdown-server

    echo "fs.inotify.max_user_watches = 524288" | sudo tee -a /etc/sysctl.conf
    sudo sysctl -p --system

    buildtools=(
      autoconf
      automake
      build-essential
      cmake
      curl
      g++
      libssl-dev
      libtool
      make
      python-dev
      wget
      unzip
      rlwrap
    )

    for package in "${buildtools[@]}"; do
      linux_install "$package"
    done
  fi
}

function install_wget() {
  if is_macos; then
    brew_install wget
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
       apt install oracle-java8-set-default
EOF
    else
      already_installed "java8"
    fi
  fi
}

function install_leiningen() {
  if is_macos; then
    brew_install leiningen
  elif is_linux; then
    install_leiningen_linux
  fi
}

function install_leiningen_linux() {
  local destination=/usr/bin/lein

  if ! program_exists "lein"; then
    cecho "@b@blue[[+ Installing lein...]]"

    sudo su << EOF
      curl --silent \
        https://raw.githubusercontent.com/technomancy/leiningen/stable/bin/lein \
        -o $destination

      chmod 755 $destination

      cd $HOME && lein upgrade
EOF
  else
    already_installed "lein"
  fi
}

function install_clojure_cli() {
  if is_macos; then
    brew_install clojure
  elif is_linux; then
    install_clojure_cli_linux
  fi
}

function install_clojure_cli_linux() {
  if ! program_exists "lein"; then
    cecho "@b@blue[[+ Installing Clojure CLI...]]"

    local current_dir=$(pwd)
    sudo su << EOF
      curl --silent \
        https://download.clojure.org/install/linux-install-1.9.0.381.sh \
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
  if is_macos; then
    brew_install watchman
  elif is_linux; then
    if ! program_exists "watchman"; then
      local current_dir=$(pwd)
      local clone_path="/tmp/watchman"

      git clone https://github.com/facebook/watchman.git $clone_path
      cd $clone_path
      git checkout v4.9.0
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

function install_android_sdk() {
  if is_macos; then
    brew_cask_install android-sdk
  elif is_linux; then
    install_android_sdk_linux
  fi

  use_android_sdk
}

function install_android_sdk_linux() {
  if [ -e ~/Android ]; then
    cecho "@green[[+ Skipping Android SDK setup- the ~/Android already exists ]]"
  else
    mkdir -p ~/Android/Sdk
    cd ~/Android/Sdk
    wget https://dl.google.com/android/repository/sdk-tools-linux-3859397.zip
    unzip sdk-tools-linux-3859397.zip
    export ANDROID_HOME=$(pwd)
    echo "export ANDROID_HOME=$(pwd)" >> ~/.profile
    $ANDROID_HOME/tools/bin/sdkmanager --no_https "system-images;android-26;google_apis;x86" "platforms;android-26" "platform-tools" "emulator" "ndk-bundle"
    $ANDROID_HOME/tools/bin/avdmanager --verbose create avd --name Status --device pixel_xl --package system-images\;android-26\;google_apis\;x86 --tag google_apis
    export PATH=$ANDROID_HOME/platform-tools:$PATH
    echo 'export PATH=$ANDROID_HOME/platform-tools:$PATH' >> ~/.profile
    export PATH=$ANDROID_HOME/tools:$PATH
    echo 'export PATH=$ANDROID_HOME/tools:$PATH' >> ~/.profile
    export ANDROID_SDK_ROOT=$ANDROID_HOME
    echo 'export ANDROID_SDK_ROOT=$ANDROID_HOME' >> ~/.profile
    $ANDROID_HOME/tools/emulator -avd Status -skin "1440x2560"
  fi
}

function install_maven() {
  brew_install maven
  linux_install maven
}

function install_react_native_cli() {
  cd "$(repo_path)"

  local npm_command="npm"

  if is_linux && ! nvm_installed; then
    # aptitude version of node requires sudo for global install
    npm_command="sudo npm"
  fi

  if npm list -g | grep -q react-native-cli; then
    already_installed "react-native-cli"
  else
    $npm_command install -g react-native-cli
  fi
}

function install_node_via_nvm() {
  local nvmrc="$(repo_path)/.nvmrc"

  cd "$(repo_path)"

  if [ ! -e "$nvmrc" ]; then
    cecho "@b@blue[[+ Installing Node 9 (Node 10 is not supported by Realm)]]"

    nvm install 9
    nvm alias status-im 9
    echo status-im > "$nvmrc"

    nvm use status-im
  else
    local version_alias=$(cat "$nvmrc")
    nvm use $version_alias

    local version=$(node -v)
    cecho "+ Node already installed ($version_alias $version via NVM)... skipping."
  fi
}

function install_node_via_package_manager() {
  if ! program_exists "node"; then
    if is_macos; then
      brew_install node
    elif is_linux; then
      curl -sL https://deb.nodesource.com/setup_9.x | sudo -E bash -
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
  eval "$@" || (cecho "@b@red[[Error running dependency install]]" && exit 1)

  echo
  echo "  + done"
  echo
}

function use_android_sdk() {
  if [ -n "$ANDROID_SDK_ROOT" ]; then
    if ! grep -Fq "sdk.dir" $_localPropertiesPath; then
      echo "sdk.dir=$ANDROID_SDK_ROOT" | tee -a $_localPropertiesPath
    fi
  else
    local _docUrl="https://docs.status.im/docs/build_status.html"
    cecho "@yellow[[ANDROID_SDK_ROOT environment variable not defined, please install the Android SDK.]]"
    cecho "@yellow[[(see $_docUrl).]]"

    echo

    exit 1
  fi
}

function install_android_ndk() {
  if is_macos; then
    install_android_ndk_mac
  elif is_linux; then
    install_android_ndk_linux
  fi
}

function install_android_ndk_linux() {
  if [ -e ~/Android/android-ndk-r10e/ ]; then
    cecho "@green[[+ Skipping Android NDK setup- the ~/Android/android-ndk-r10e/ already exists ]]"
  else
    cd ~/Android/
    wget http://dl.google.com/android/repository/android-ndk-r10e-linux-x86_64.zip
    unzip android-ndk-r10e-linux-x86_64.zip
    cd android-ndk-r10e/
    export ANDROID_NDK=$(pwd)
    echo "export ANDROID_NDK=$(pwd)" >> ~/.profile
    export ANDROID_NDK_HOME=$(pwd)
    echo "export ANDROID_NDK_HOME=$(pwd)" >> ~/.profile
    echo "ndk.dir=$(pwd)" >> ~/android/local.properties
   fi
}

function install_android_ndk_mac() {
  if grep -Fq "ndk.dir" $_localPropertiesPath; then
    cecho "@green[[Android NDK already declared.]]"
  else
    local _ndkParentDir=~/Android/Sdk
    mkdir -p $_ndkParentDir
    cecho "@cyan[[Downloading Android NDK.]]"
    wget --output-document=android-ndk.zip https://dl.google.com/android/repository/android-ndk-r10e-linux-x86_64.zip && \
      cecho "@cyan[[Extracting Android NDK to $_ndkParentDir.]]" && \
      unzip -q -o android-ndk.zip -d "$_ndkParentDir" && \
      rm -f android-ndk.zip && \
      _ndkTargetDir="$_ndkParentDir/$(ls $_ndkParentDir | head -n 1)" && \
      echo "ndk.dir=$_ndkTargetDir" | tee -a $_localPropertiesPath && \
      cecho "@blue[[Android NDK installation completed in $_ndkTargetDir.]]"
  fi
}
