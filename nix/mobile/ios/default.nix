{ callPackage, lib, mkShell, pkgs
, status-go, fastlane }:

let
  inherit (lib) catAttrs unique;

  nodejs-sh = callPackage ./shells/nodejs.nix { };
  cocoapods-sh = callPackage ./shells/pod.nix { };
  status-go-sh = callPackage ./shells/status-go.nix { inherit status-go; };

in {
  inherit nodejs-sh cocoapods-sh status-go-sh;

  shell = mkShell {
    buildInputs = with pkgs; [
      xcodeWrapper watchman bundler procps
      flock # used in nix/scripts/node_modules.sh
    ];

    # WARNING: Executes shellHook in reverse order.
    inputsFrom = [
      fastlane.shell
      cocoapods-sh
      nodejs-sh # before 'pod install'
      status-go-sh # before 'pod install'
    ];
  };
}
