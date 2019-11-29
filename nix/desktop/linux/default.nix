{ stdenv, mkShell, callPackage,
  appimagekit, patchelf, qt5, status-go, baseImageFactory }:

with stdenv;

assert isLinux;

let
  inherit (lib) concatStrings catAttrs;
  baseImage = baseImageFactory "linux";
  appimagekit = callPackage ./appimagekit { };
  linuxdeployqt = callPackage ./linuxdeployqt { inherit appimagekit; };

in rec {
  buildInputs = [
    appimagekit
    linuxdeployqt
    patchelf
    qt5.full
  ] ++ status-go.buildInputs;

  shell = mkShell {
    inherit buildInputs;
    shellHook = concatStrings (catAttrs "shellHook" [ baseImage status-go ]) + ''
      export QT_PATH="${qt5.full}"
      export QT_BASEBIN_PATH="${qt5.qtbase.bin}"
      export PATH="${qt5.full}/bin:$PATH"
    '';
  };
}
