{ stdenv, callPackage,
  appimagekit, patchelf, qt5, status-go }:

with stdenv;

assert isLinux;

let
  inherit (lib) concatStrings catAttrs;
  baseImage = callPackage ./base-image { };
  appimagekit = callPackage ./appimagekit { };
  linuxdeployqt = callPackage ./linuxdeployqt { inherit appimagekit; };

in {
  buildInputs = [
    appimagekit
    linuxdeployqt
    patchelf
    baseImage
    qt5.full
  ] ++ status-go.buildInputs;

  shellHook =
    concatStrings (catAttrs "shellHook" [ baseImage status-go ] ) + ''
      export QT_PATH="${qt5.full}"
      export QT_BASEBIN_PATH="${qt5.qtbase.bin}"
      export PATH="${qt5.full}/bin:$PATH"
    '';
}
