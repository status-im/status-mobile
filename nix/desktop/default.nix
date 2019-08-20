{ stdenv, pkgs, callPackage, target-os,
  cmake, extra-cmake-modules, file, status-go, go,
  darwin }:

let
  inherit (stdenv.lib) catAttrs concatStrings optional unique;

  platform = callPackage ../platform.nix { inherit target-os; };
  linuxPlatform = callPackage ./linux { inherit status-go; };
  darwinPlatform = callPackage ./macos { inherit status-go darwin; };
  windowsPlatform = callPackage ./windows { inherit go; };
  snoreNotifySources = callPackage ./cmake/snorenotify { };
  qtkeychainSources = callPackage ./cmake/qtkeychain { };
  selectedSources =
    optional platform.targetLinux linuxPlatform ++
    optional platform.targetDarwin darwinPlatform ++
    optional platform.targetWindows windowsPlatform;

in {
  buildInputs = unique ([
    cmake
    extra-cmake-modules
    file
    snoreNotifySources
    qtkeychainSources
  ] ++ catAttrs "buildInputs" selectedSources);

  shellHook = concatStrings (catAttrs "shellHook" (selectedSources ++ [ snoreNotifySources qtkeychainSources ]));
}
