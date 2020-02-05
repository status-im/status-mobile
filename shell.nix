{
  config ? { },      # for passing build options, see nix/README.md
  target ? "default" # see nix/shells.nix for all valid values
}:

let
  project = import ./default.nix { inherit config; };
in
  # this is where the $TARGET env variable affects things
  project.pkgs.mergeSh project.shells.default [ project.shells.${target} ]
  # combining with default shell to include all the standard utilities
