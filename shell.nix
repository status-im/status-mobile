{ pkgs ? import <nixpkgs> {},
  target-os ? "all" }:
with pkgs;

let
  projectDeps = import ./default.nix { inherit target-os; };
  targetAndroid = {
    "android" = true;
    "all" = true;
  }.${target-os} or false;
  targetMobile = {
    "android" = true;
    "ios" = true;
    "all" = true;
  }.${target-os} or false;
  useFastlanePkg = (targetAndroid && !_stdenv.isDarwin);
  # TODO: Try to use stdenv for iOS. The problem is with building iOS as the build is trying to pass parameters to Apple's ld that are meant for GNU's ld (e.g. -dynamiclib)
  _stdenv = stdenvNoCC;
  _mkShell = mkShell.override { stdenv = _stdenv; };
  _fastlane = callPackage ./fastlane {
    bundlerEnv = _: pkgs.bundlerEnv { 
      name = "fastlane-gems";
      gemdir = ./fastlane;
    };
  };

in _mkShell {
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
  (if useFastlanePkg then [ _fastlane.package ] else [ bundler ruby ]); # bundler/ruby used for fastlane on macOS
  inputsFrom = [ projectDeps ];
  TARGET_OS=target-os;
  shellHook =
    ''
      set -e

      STATUS_REACT_HOME=$(git rev-parse --show-toplevel)

      ${projectDeps.shellHook}
      ${lib.optionalString useFastlanePkg _fastlane.shellHook}

      if [ ! -f $STATUS_REACT_HOME/.ran-setup ]; then
        $STATUS_REACT_HOME/scripts/setup
        touch $STATUS_REACT_HOME/.ran-setup
      fi
      set +e
    '';
}