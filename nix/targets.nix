{
  config ? {},
  pkgs ? import ./pkgs.nix { inherit config; }
}:

let
  inherit (pkgs) stdenv callPackage;

  status-go = callPackage ./status-go {};
  nim-status = callPackage ./nim-status {};
  mobile = callPackage ./mobile { inherit status-go nim-status;};
in {
  inherit mobile status-go nim-status;
}
