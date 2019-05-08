{ stdenv, pkgs, status-go }:

with pkgs;
with stdenv;

assert isLinux;

let
  baseImage = callPackage ./base-image { };
  appimagekit = callPackage ./appimagekit { };
  linuxdeployqt = callPackage ./linuxdeployqt { inherit appimagekit; };

in {
  buildInputs = [
    appimagekit
    linuxdeployqt
    patchelf
    baseImage
  ] ++ status-go.packages;

  shellHook =
    baseImage.shellHook +
    status-go.shellHook;
}
