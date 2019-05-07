{ stdenv, pkgs, target-os }:

with pkgs;
with stdenv;

let
  platform = callPackage ../platform.nix { inherit target-os; };
  linuxPlatform = callPackage ./linux { };
  darwinPlatform = callPackage ./macos { };
  windowsPlatform = callPackage ./windows { };
  snoreNotifySources = callPackage ./cmake/snorenotify { };
  qtkeychainSources = callPackage ./cmake/qtkeychain { };

in
  {
    buildInputs = [
      cmake
      extra-cmake-modules
      file
      snoreNotifySources
      qtkeychainSources
    ] ++ lib.optionals platform.targetLinux linuxPlatform.buildInputs
      ++ lib.optionals platform.targetDarwin darwinPlatform.buildInputs
      ++ lib.optionals platform.targetWindows windowsPlatform.buildInputs
      ++ lib.optional (! platform.targetWindows) qt5.full;
    shellHook = 
      snoreNotifySources.shellHook +
      qtkeychainSources.shellHook +
      lib.optionalString (target-os != "windows") ''
        export QT_PATH="${qt5.full}"
        export QT_BASEBIN_PATH="${qt5.qtbase.bin}"
        export PATH="${qt5.full}/bin:$PATH"
      '' +
      lib.optionalString platform.targetLinux linuxPlatform.shellHook +
      lib.optionalString platform.targetDarwin darwinPlatform.shellHook +
      lib.optionalString platform.targetWindows windowsPlatform.shellHook;
  }
