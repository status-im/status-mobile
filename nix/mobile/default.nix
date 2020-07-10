{ config, lib, stdenvNoCC, callPackage, mkShell, nim-status }:

let
  inherit (lib) catAttrs concatStrings optional unique;

  fastlane = callPackage ./fastlane { };

  android = callPackage ./android {
    nim-status = nim-status.android;
  };

  ios = callPackage ./ios {
    inherit fastlane;
    nim-status = nim-status.ios;
  };

  selectedSources = [
    fastlane
    android
    ios
  ];

in {
  shell = mkShell {
    inputsFrom = (catAttrs "shell" selectedSources);
  };

  # TARGETS
  inherit android ios fastlane;
}
