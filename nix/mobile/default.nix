{ config, lib, stdenvNoCC, callPackage, mkShell, status-go, nim-status }:

let
  inherit (lib) catAttrs concatStrings optional unique;

  fastlane = callPackage ./fastlane { };

  android = callPackage ./android {
    status-go = status-go.android;
    nim-status = nim-status.android;
  };

  ios = callPackage ./ios {
    inherit fastlane;
    status-go = status-go.ios;
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
