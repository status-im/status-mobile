{ pkgs ? import <nixpkgs> {},
  target-os ? "all" }:
with pkgs;

let
  projectDeps = import ./default.nix { inherit target-os; };
  platform = callPackage ./nix/platform.nix { inherit target-os; };
  useFastlanePkg = (platform.targetIOS || (platform.targetAndroid && !stdenv'.isDarwin));
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
    fastlane'
    bundler
    ruby
  ];
  inputsFrom = [ projectDeps ];
  TARGET_OS = target-os;
  shellHook = ''
    set -e

    STATUS_REACT_HOME=$(git rev-parse --show-toplevel)

    ${projectDeps.shellHook}
    ${lib.optionalString useFastlanePkg fastlane'.shellHook}

    if [ "$IN_NIX_SHELL" != 'pure' ] && [ ! -f $STATUS_REACT_HOME/.ran-setup ]; then
      $STATUS_REACT_HOME/scripts/setup
      touch $STATUS_REACT_HOME/.ran-setup
    fi

    # HACK: see https://github.com/facebook/react-native/pull/25146/
    HACK_FILE='node_modules/react-native/React/Base/RCTModuleMethod.mm'
    set -x
    if [[ -f $HACK_FILE ]] && ! grep 'RCTReadString(input, "__attribute__((__unused__))' $HACK_FILE; then
      sed -i.bkp '/return RCTReadString/a RCTReadString(input, "__attribute__((__unused__))") ||' $HACK_FILE
    fi
    set +x

    set +e
  '';
}
