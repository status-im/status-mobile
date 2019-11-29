{
  config ? { }, # for passing status_go.src_override
}:

let
  main = import ./nix/default.nix { inherit config; };
in {
  inherit (main) pkgs targets shells;
}
