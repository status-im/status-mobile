{ stdenv, pkgs, target-os }:

with pkgs;
with stdenv;

let
  targetLinux = {
    "linux" = true;
    "all" = isLinux;
  }.${target-os} or false;
  targetDarwin = {
    "macos" = true;
    "all" = isDarwin;
  }.${target-os} or false;
  targetWindows = {
    "windows" = true;
    "all" = isLinux;
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
    ] ++ lib.optionals targetLinux linuxPlatform.buildInputs
      ++ lib.optionals targetDarwin darwinPlatform.buildInputs
      ++ lib.optionals targetWindows windowsPlatform.buildInputs
      ++ lib.optional (! targetWindows) qt5.full;
    shellHook = (if target-os == "windows" then ''
      unset QT_PATH
    '' else ''
      export QT_PATH="${qt5.full}"
      export QT_BASEBIN_PATH="${qt5.qtbase.bin}"
      export PATH="${stdenv.lib.makeBinPath [ qt5.full ]}:$PATH"
    '') +
    lib.optionalString targetLinux linuxPlatform.shellHook +
    lib.optionalString targetDarwin darwinPlatform.shellHook +
    lib.optionalString targetWindows windowsPlatform.shellHook;
  }
