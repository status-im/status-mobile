# for passing build options, see nix/README.md
{ config ? { } }:

let
  project = import ./default.nix { inherit config; };
in
  # we use the shell combining most shells as default
  project.shells.default
