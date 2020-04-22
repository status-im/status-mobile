{
  config ? {},
  pkgs ? import ./pkgs.nix { inherit config; }
}:

let
  inherit (pkgs) stdenv callPackage;

  status-go = callPackage ./status-go { };

  desktop = callPackage ./desktop {
    status-go = status-go.desktop;
  };

  mobile = callPackage ./mobile {
    inherit status-go;
  };
in {
  inherit mobile desktop status-go;
}
