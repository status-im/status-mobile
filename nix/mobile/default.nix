{ lib, callPackage, mkShell, status-go }:

let
  fastlane = callPackage ./fastlane { };

  jsbundle = callPackage ./jsbundle { };

  android = callPackage ./android {
    inherit jsbundle;
    status-go = status-go.mobile.android;
  };

  ios = callPackage ./ios {
    inherit fastlane;
    status-go = status-go.mobile.ios;
  };

  selectedSources = [
    status-go.mobile.android
    status-go.mobile.ios
    fastlane
    android
    ios
  ];

in {
  shell = mkShell {
    inputsFrom = lib.catAttrs "shell" selectedSources;
  };

  # TARGETS
  inherit android ios fastlane jsbundle;
}
