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
    statusMobile = callPackage ./scripts/lib/setup/nix/mobile { stdenv = _stdenv; };
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
      bash
      clojure
      curl
      git
      jq
      leiningen
      lsof # used in scripts/start-react-native.sh
      maven
      ncurses
      ps # used in scripts/start-react-native.sh
      statusDesktop.buildInputs
      statusMobile.buildInputs
      watchman
      unzip
      wget
    ] ++ nodePkgs
      ++ lib.optional isDarwin cocoapods
      ++ lib.optional isLinux gcc7;
    shellHook = ''
        ${statusDesktop.shellHook}
        ${statusMobile.shellHook}

        [ -d "$ANDROID_SDK_ROOT" ] || ./scripts/setup # we assume that if the Android SDK dir does not exist, make setup needs to be run
    '';
  }
