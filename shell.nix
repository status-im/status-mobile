{ pkgs ? import <nixpkgs> {},
  target-os ? "all" }:
with pkgs;

let
  projectDeps = import ./default.nix { inherit target-os; };
  targetMobile = {
    "android" = true;
    "ios" = true;
    "all" = true;
  }.${target-os} or false;
  # TODO: Try to use stdenv for iOS. The problem is with building iOS as the build is trying to pass parameters to Apple's ld that are meant for GNU's ld (e.g. -dynamiclib)
  _stdenv = stdenvNoCC;
  _mkShell = mkShell.override { stdenv = _stdenv; };

in _mkShell {
  buildInputs = [
    # utilities
    bash
    curl
    git
    jq
    ncurses
    lsof # used in scripts/start-react-native.sh
    ps # used in scripts/start-react-native.sh
    unzip
    wget
  ] ++ lib.optionals targetMobile [ bundler ruby ]; # bundler/ruby used for fastlane
  inputsFrom = [ projectDeps ];
  shellHook =
    ''
      set -e
    '' +
    projectDeps.shellHook +
    ''
      if [ -n "$ANDROID_SDK_ROOT" ] && [ ! -d "$ANDROID_SDK_ROOT" ]; then
        ./scripts/setup # we assume that if the Android SDK dir does not exist, setup script needs to be run
      fi
      set +e
    '';
}