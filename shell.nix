{ nixpkgs-bootstrap ? import ./nix/nixpkgs-bootstrap.nix { },
  pkgs ? nixpkgs-bootstrap.pkgs,
  target-os ? "all" }:
with pkgs;

let
  projectDeps = import ./default.nix { inherit target-os; inherit (nixpkgs-bootstrap) config; };
  platform = callPackage ./nix/platform.nix { inherit target-os; };
  useFastlanePkg = (platform.targetAndroid && !stdenv'.isDarwin);
  # TODO: Try to use stdenv for iOS. The problem is with building iOS as the build is trying to pass parameters to Apple's ld that are meant for GNU's ld (e.g. -dynamiclib)
  stdenv' = stdenvNoCC;
  mkShell' = mkShell.override { stdenv = stdenv'; };
  fastlane' = callPackage ./fastlane {
    bundlerEnv = _: pkgs.bundlerEnv { 
      name = "fastlane-gems";
      gemdir = ./fastlane;
    };
  };

in mkShell' {
  buildInputs = [
    # utilities
    bash
    curl
    file
    git
    gnumake
    jq
    ncurses
    lsof # used in scripts/start-react-native.sh
    ps # used in scripts/start-react-native.sh
    unzip
    wget
  ] ++
  (if useFastlanePkg then [ fastlane' ] else lib.optionals platform.targetMobile [ bundler ruby ]); # bundler/ruby used for fastlane on macOS
  inputsFrom = [ projectDeps ];
  TARGET_OS = target-os;
  shellHook = ''
    set -e

    export STATUS_REACT_HOME=$(git rev-parse --show-toplevel)
    export PATH=$STATUS_REACT_HOME/node_modules/.bin:$PATH

    ${projectDeps.shellHook}
    ${lib.optionalString useFastlanePkg fastlane'.shellHook}

    if [ "$IN_NIX_SHELL" != 'pure' ] && [ ! -f $STATUS_REACT_HOME/.ran-setup ]; then
      $STATUS_REACT_HOME/scripts/setup
      touch $STATUS_REACT_HOME/.ran-setup
    fi
    set +e
  '';
}
