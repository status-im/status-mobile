{ lib, stdenv, mkShell, callPackage
# pkgs
, appimagekit, linuxdeployqt, patchelf, qt5custom
# custom arguments
, status-go, baseImageFactory }:

assert lib.assertMsg stdenv.isLinux "Building Linux app can work only on Linux!";

let
  inherit (lib) concatStrings catAttrs;
  baseImage = baseImageFactory "linux";

in rec {
  buildInputs = [
    appimagekit
    linuxdeployqt
    patchelf
    qt5custom
  ];

  shell = mkShell {
    inherit buildInputs;
    inputsFrom = [ baseImage status-go ];
    shellHook = ''
      export QT_PATH="${qt5custom}"
      export QT_BASEBIN_PATH="${qt5custom}/bin"
      export PATH="$QT_BASEBIN_PATH:$PATH"
    '';
  };
}
