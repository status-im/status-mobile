#
# This Nix expression centralizes the configuration
# for the Android development environment.
#

{ callPackage }:

let
  compose = callPackage ./compose.nix { };
  pkgs = callPackage ./pkgs.nix { inherit compose; };
  shell = callPackage ./shell.nix { androidPkgs = pkgs; };
in {
  inherit compose pkgs shell;
}
