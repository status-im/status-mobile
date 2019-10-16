{ stdenv, pkgs, callPackage, target-os,
  cmake, extra-cmake-modules, file, moreutils, status-go, go,
  darwin, nodejs }:

let
  inherit (stdenv.lib) catAttrs concatStrings optional unique;

  platform = callPackage ../platform.nix { inherit target-os; };
  baseImageFactory = callPackage ./base-image { inherit stdenv; };
  linuxPlatform = callPackage ./linux { inherit stdenv status-go baseImageFactory; };
  darwinPlatform = callPackage ./macos { inherit stdenv status-go darwin baseImageFactory pkgs; };
  windowsPlatform = callPackage ./windows { inherit stdenv go baseImageFactory; };
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
    moreutils
    snoreNotifySources
    qtkeychainSources
  ] ++ catAttrs "buildInputs" selectedSources);

  shellHook = concatStrings (catAttrs "shellHook" (selectedSources ++ [ snoreNotifySources qtkeychainSources ]));
}
