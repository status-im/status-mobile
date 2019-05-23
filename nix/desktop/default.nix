{ stdenv, pkgs, callPackage, target-os,
  cmake, extra-cmake-modules, file, status-go,
  darwin, nodejs }:

with stdenv;

let
  platform = callPackage ../platform.nix { inherit target-os; };
  linuxPlatform = callPackage ./linux { inherit status-go nodejs; };
  darwinPlatform = callPackage ./macos { inherit status-go darwin; };
  windowsPlatform = callPackage ./windows { };
  snoreNotifySources = callPackage ./cmake/snorenotify { };
  qtkeychainSources = callPackage ./cmake/qtkeychain { };
  selectedSources =
    lib.optional platform.targetLinux linuxPlatform ++
    lib.optional platform.targetDarwin darwinPlatform ++
    lib.optional platform.targetWindows windowsPlatform;
  nodeInputs = import ./realm-node {
    # The remaining dependencies come from Nixpkgs
    inherit pkgs nodejs;
  };

in
  {
    buildInputs = [
      cmake
      extra-cmake-modules
      file
      snoreNotifySources
      qtkeychainSources
    ] ++ lib.catAttrs "buildInputs" selectedSources
      ++ (builtins.attrValues nodeInputs);

    shellHook = lib.concatStrings (lib.catAttrs "shellHook" (selectedSources ++ [ snoreNotifySources qtkeychainSources ]));
  }
