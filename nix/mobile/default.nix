{ config, lib, stdenvNoCC, callPackage, mkShell, status-go }:

let
  inherit (lib) catAttrs concatStrings optional unique;

  fastlane = callPackage ./fastlane { };

  android = callPackage ./android {
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
    inputsFrom = (catAttrs "shell" selectedSources);
    shellHooks = ''
      # create mobile node/yarn symlinks
      ln -sf $STATUS_REACT_HOME/mobile/js_files/* $STATUS_REACT_HOME/
    '';
  };

  # TARGETS
  inherit android ios fastlane;
}
