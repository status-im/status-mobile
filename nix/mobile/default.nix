{ config, stdenv, pkgs, target-os, status-go }:

with pkgs;
with stdenv;

let
  gradle = gradle_4_10;
  platform = pkgs.callPackage ../platform.nix { inherit target-os; };
  xcodewrapperArgs = {
    version = "10.1";
  };
  xcodeWrapper = xcodeenv.composeXcodeWrapper xcodewrapperArgs;
  androidPlatform = callPackage ./android.nix { inherit config; };
  selectedSources =
    [ status-go ] ++
    lib.optional platform.targetAndroid androidPlatform;

in
  {
    inherit (androidPlatform) androidComposition;
    inherit xcodewrapperArgs;

    buildInputs =
      status-go.buildInputs ++
      lib.catAttrs "buildInputs" selectedSources ++
      lib.optional (platform.targetIOS && isDarwin) xcodeWrapper;
    shellHook = lib.concatStrings (lib.catAttrs "shellHook" selectedSources);
  }
