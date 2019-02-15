let
  pkgs = import ((import <nixpkgs> { }).fetchFromGitHub {
    owner = "status-im";
    repo = "nixpkgs";
    rev = "15623aac6e8cbfa24d4268195bc8eda7303ea2ff";
    sha256 = "0crjmspk65rbpkl3kqcj7433355i9fy530lhc48g2cz75xjk4sxh";
  }) { config = { }; };

in with pkgs;
  let
    _stdenv = stdenvNoCC; # TODO: Try to use stdenv for Darwin
    statusDesktop = callPackage ./scripts/lib/setup/nix/desktop { stdenv = _stdenv; };
    nodeInputs = import ./scripts/lib/setup/nix/global-node-packages/output {
      # The remaining dependencies come from Nixpkgs
      inherit pkgs;
      inherit nodejs;
    };
    nodePkgs = [
      nodejs
      python27 # for e.g. gyp
      yarn
    ] ++ (map (x: nodeInputs."${x}") (builtins.attrNames nodeInputs));

  in _stdenv.mkDerivation rec {
    name = "env";
    env = buildEnv { name = name; paths = buildInputs; };
    buildInputs = with _stdenv; [
      clojure
      curl
      jq
      leiningen
      lsof # used in scripts/start-react-native.sh
      maven
      ncurses
      ps # used in scripts/start-react-native.sh
      openjdk
      statusDesktop.buildInputs
      watchman
      unzip
      wget
    ] ++ nodePkgs
      ++ lib.optional isDarwin cocoapods
      ++ lib.optional isLinux gcc7;
    shellHook = ''
        ${statusDesktop.shellHook}

        local toolversion="$(git rev-parse --show-toplevel)/scripts/toolversion"

        export JAVA_HOME="${openjdk}"
        export ANDROID_HOME=~/.status/Android/Sdk
        export ANDROID_SDK_ROOT="$ANDROID_HOME"
        export ANDROID_NDK_ROOT="$ANDROID_SDK_ROOT/android-ndk-$($toolversion android-ndk)"
        export ANDROID_NDK_HOME="$ANDROID_NDK_ROOT"
        export ANDROID_NDK="$ANDROID_NDK_ROOT"
        export PATH="$ANDROID_HOME/bin:$ANDROID_HOME/tools:$ANDROID_HOME/tools/bin:$ANDROID_HOME/platform-tools:$ANDROID_HOME/build-tools:$PATH"

        [ -d "$ANDROID_NDK_ROOT" ] || ./scripts/setup # we assume that if the Android NDK dir does not exist, make setup needs to be run
    '';
  }