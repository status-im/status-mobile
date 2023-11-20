{ callPackage, lib, mkShell, pkgs, stdenv
, status-go, fastlane }:

assert lib.assertMsg stdenv.isDarwin
  "iOS development shell supported only on OSX.";

let
  inherit (lib) catAttrs unique;

  # Sub-shells preparing various dependencies.
  nodejs-sh = callPackage ./shells/nodejs.nix { };
  bundler-sh = callPackage ./shells/bundler.nix { };
  cocoapods-sh = callPackage ./shells/cocoapods.nix { };
  status-go-sh = callPackage ./shells/status-go.nix { inherit status-go; };

in {
  inherit nodejs-sh cocoapods-sh status-go-sh;

  shell = mkShell {
    buildInputs = with pkgs; [
      xcodeWrapper watchman procps
      flock # used in nix/scripts/node_modules.sh
      ios-deploy # used in 'make run-ios-device'
    ];

    # WARNING: Executes shellHook in reverse order.
    # WARNING: Only some variables are merged.
    inputsFrom = [
      fastlane.shell
      cocoapods-sh
      nodejs-sh    # before 'pod install'
      bundler-sh   # before 'pod install'
      status-go-sh # before 'pod install'
    ];
  };
}
