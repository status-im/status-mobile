# target-os = [ 'windows' 'linux' 'macos' 'android' 'ios' ]
{ pkgs ? import ((import <nixpkgs> { }).fetchFromGitHub {
    owner = "status-im";
    repo = "nixpkgs";
    rev = "15623aac6e8cbfa24d4268195bc8eda7303ea2ff";
    sha256 = "0crjmspk65rbpkl3kqcj7433355i9fy530lhc48g2cz75xjk4sxh";
  }) { config = { }; },
  target-os ? "" }:

with pkgs;
  let
    targetDesktop = {
      "linux" = true;
      "windows" = true;
      "macos" = true;
      "" = true;
    }.${target-os} or false;
    targetMobile = {
      "android" = true;
      "ios" = true;
      "" = true;
    }.${target-os} or false;
    _stdenv = stdenvNoCC; # TODO: Try to use stdenv for Darwin
    statusDesktop = callPackage ./scripts/lib/setup/nix/desktop { inherit target-os; stdenv = _stdenv; };
    statusMobile = callPackage ./scripts/lib/setup/nix/mobile { inherit target-os; stdenv = _stdenv; };
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
      watchman
      unzip
      wget
    ] ++ nodePkgs
      ++ lib.optional isDarwin cocoapods
      ++ lib.optional isLinux gcc7
      ++ lib.optional targetDesktop statusDesktop.buildInputs
      ++ lib.optional targetMobile statusMobile.buildInputs;
    shellHook =
      (if targetDesktop then statusDesktop.shellHook else "") +
      (if targetMobile then statusMobile.shellHook else "") +
      ''
        if [ -n "$ANDROID_SDK_ROOT" ] && [ ! -d "$ANDROID_SDK_ROOT" ]; then
          ./scripts/setup # we assume that if the Android SDK dir does not exist, make setup needs to be run
        fi
      '';
  }
