{ stdenv, pkgs, target-os }:

with pkgs;
with stdenv;

let
  targetLinux = {
    "linux" = true;
    "" = isLinux;
  }.${target-os} or false;
  targetDarwin = {
    "macos" = true;
    "" = isDarwin;
  }.${target-os} or false;
  targetWindows = {
    "windows" = true;
    "" = isLinux;
  }.${target-os} or false;
  linuxPlatform = callPackage ./linux { };
  darwinPlatform = callPackage ./macos { };
  windowsPlatform = callPackage ./windows { };

in
  {
    buildInputs = [
      cmake
      extra-cmake-modules
      file
    ] ++ lib.optional targetLinux linuxPlatform.buildInputs
      ++ lib.optional targetDarwin darwinPlatform.buildInputs
      ++ lib.optional (! targetWindows) qt5.full
      ++ lib.optional targetWindows windowsPlatform.buildInputs;
    shellHook = (if target-os == "windows" then ''
      unset QT_PATH
    '' else ''
      export QT_PATH="${qt5.full}"
      export PATH="${stdenv.lib.makeBinPath [ qt5.full ]}:$PATH"
    '');
  }
