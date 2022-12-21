{
  config ? {},
  pkgs ? import ./pkgs.nix { inherit config; }
}:

let
  inherit (pkgs) stdenv callPackage;

  status-go = callPackage ./status-go { };
  mobile = callPackage ./mobile { inherit status-go; };
in {
  inherit mobile status-go;
}
