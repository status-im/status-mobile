{
  config ? {},
  pkgs ? import ./pkgs.nix { inherit config; }
}:

let
  inherit (pkgs) stdenv callPackage;

  nim-status = callPackage ./nim-status {};
  mobile = callPackage ./mobile { inherit nim-status;};
in {
  inherit mobile nim-status;
}
