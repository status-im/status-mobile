# for passing build options, see nix/README.md
{ config ? { } }:

let
  main = import ./nix { inherit config; };
in {
  # this is where the --attr argument selects the shell or target
  inherit (main) pkgs targets shells;
  inherit (main.pkgs) config;
}
