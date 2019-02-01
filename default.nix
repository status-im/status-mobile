let
  pkgs = import ((import <nixpkgs> { }).fetchFromGitHub {
    owner = "NixOS";
    repo = "nixpkgs";
    rev = "168cbb39691cca2822ce1fdb3e8c0183af5c6d0d";
    sha256 = "0fqasswfqrz2rbag9bz17j8y7615s0p9l23cw4sk2f384gk0zf6c";
  }) { config = { }; };

in pkgs.stdenv.mkDerivation rec {
  name = "env";
  env = pkgs.buildEnv { name = name; paths = buildInputs; };
  statusDesktopBuildInputs = with pkgs; [
    cmake
    extra-cmake-modules
    go_1_10
  ] ++ stdenv.lib.optional stdenv.isLinux python37; # for Conan
  buildInputs = with pkgs; [
    clojure
    jq
    leiningen
    lsof # used in scripts/start-react-native.sh
    maven
    nodejs
    openjdk
    python27 # for e.g. gyp
    watchman
    unzip
    wget
    yarn
  ] ++ statusDesktopBuildInputs
    ++ stdenv.lib.optional stdenv.isDarwin cocoapods;
  shellHook = with pkgs; ''
      local toolversion="$(git rev-parse --show-toplevel)/scripts/toolversion"

      export JAVA_HOME="${openjdk}"
      export ANDROID_HOME=~/.status/Android/Sdk
      export ANDROID_SDK_ROOT="$ANDROID_HOME"
      export ANDROID_NDK_ROOT="$ANDROID_SDK_ROOT/android-ndk-$($toolversion android-ndk)"
      export ANDROID_NDK_HOME="$ANDROID_NDK_ROOT"
      export ANDROID_NDK="$ANDROID_NDK_ROOT"
      export PATH="$ANDROID_HOME/bin:$ANDROID_HOME/tools:$ANDROID_HOME/tools/bin:$ANDROID_HOME/platform-tools:$ANDROID_HOME/build-tools:$PATH"

      [ -d "$ANDROID_NDK_ROOT" ] || ./scripts/setup # we assume that if the NDK dir does not exist, make setup needs to be run
  '';
}
