{
  config ? { status-im = { build-type = ""; }; }, # for passing build options, see nix/README.md
}:

let
  main = import ./nix/default.nix { inherit config; };
in {
  inherit (main) pkgs targets shells;
}
