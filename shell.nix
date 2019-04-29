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
    file
    git
    gnumake
    jq
    ncurses
    lsof # used in scripts/start-react-native.sh
    ps # used in scripts/start-react-native.sh
    unzip
    wget
  ] ++ lib.optionals targetMobile [ bundler ruby ]; # bundler/ruby used for fastlane
  inputsFrom = [ projectDeps ];
  TARGET_OS=target-os;
  shellHook =
    ''
      set -e
    
      ${projectDeps.shellHook}

      STATUS_REACT_HOME=$(git rev-parse --show-toplevel)
      if [ ! -f $STATUS_REACT_HOME/.ran-setup ]; then
        $STATUS_REACT_HOME/scripts/setup
        touch $STATUS_REACT_HOME/.ran-setup
      fi
      set +e
    '';
}