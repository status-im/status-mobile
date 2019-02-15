{ stdenv, pkgs }:

with pkgs;
with stdenv; 

let
  windowsPlatform = callPackage ./windows { };
  appimagekit = callPackage ./appimagekit { };
  linuxdeployqt = callPackage ./linuxdeployqt { inherit appimagekit; };

in
  {
    buildInputs = [
      cmake
      extra-cmake-modules
      file
      gnupg # Used by appimagetool
      go
      qt5.full
    ] ++ lib.optional isLinux [ appimagekit linuxdeployqt patchelf ]
      ++ lib.optional isLinux windowsPlatform.buildInputs;
    shellHook = ''
      export QT_PATH="${qt5.full}"
      export PATH="${stdenv.lib.makeBinPath [ qt5.full ]}:$PATH"
    '';
  }
