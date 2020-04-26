# for passing build options, see nix/README.md
{ config ? { status-im = { build-type = ""; }; } }:

let
  main = import ./nix/default.nix { inherit config; };
in {
  # this is where the --attr argument selects the shell or target
  inherit (main) pkgs targets shells;
}
