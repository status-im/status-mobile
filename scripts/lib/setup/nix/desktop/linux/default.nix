{ stdenv, pkgs }:

with pkgs;
with stdenv;

let
  baseImage = callPackage ./base-image { };
  appimagekit = callPackage ./appimagekit { };
  linuxdeployqt = callPackage ./linuxdeployqt { inherit appimagekit; };

in
{
  buildInputs = [ appimagekit linuxdeployqt patchelf baseImage ];

  # Fixes Cgo related build failures (see https://github.com/NixOS/nixpkgs/issues/25959 )
  hardeningDisable = [ "fortify" ];
}
