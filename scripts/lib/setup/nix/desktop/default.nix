{ stdenv, pkgs, target-os }:

with pkgs;
with stdenv;

let
  targetLinux = {
    "linux" = true;
    "" = isLinux;
  }.${target-os} or false;
  targetWindows = {
    "windows" = true;
    "" = isLinux;
  }.${target-os} or false;
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
      hostname # Used by run-app.sh
    ] ++ lib.optional targetLinux [ appimagekit linuxdeployqt patchelf ]
      ++ lib.optional (! targetWindows) qt5.full
      ++ lib.optional targetWindows windowsPlatform.buildInputs;
    shellHook = (if target-os == "windows" then "unset QT_PATH" else ''
      export QT_PATH="${qt5.full}"
      export PATH="${stdenv.lib.makeBinPath [ qt5.full ]}:$PATH"
    '') + (lib.optionalString isDarwin ''
      export MACOSX_DEPLOYMENT_TARGET=10.9
    '');
  }
