{ stdenv, pkgs }:

with pkgs;
with stdenv;

assert isLinux;

let
  baseImage = callPackage ./base-image { };
  appimagekit = callPackage ./appimagekit { };
  linuxdeployqt = callPackage ./linuxdeployqt { inherit appimagekit; };

in {
  buildInputs = [ appimagekit linuxdeployqt patchelf baseImage ];

  inherit (baseImage) shellHook;
}
