{ stdenv, callPackage,
  appimagekit, patchelf, qt5, status-go, nodejs }:

with stdenv;

assert isLinux;

let
  baseImage = callPackage ./base-image { inherit nodejs; };
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
    baseImage.shellHook +
    status-go.shellHook + ''
      export QT_PATH="${qt5.full}"
      export QT_BASEBIN_PATH="${qt5.qtbase.bin}"
      export PATH="${qt5.full}/bin:$PATH"
    '';
}
