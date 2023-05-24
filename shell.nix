# for passing build options, see nix/README.md
{ config ? { } }:

let
  main = import ./nix { inherit config; };
in
  # use the default shell when calling nix-shell without arguments
  main.shells.default
