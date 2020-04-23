{ config, lib, stdenvNoCC, callPackage, mkShell,
  status-go, xcodeWrapper }:

let
  inherit (lib) catAttrs concatStrings optional unique;

  projectNodePackage = callPackage ./node-package.nix { };

  localMavenRepoBuilder = callPackage ../tools/maven/maven-repo-builder.nix { };

  fastlane = callPackage ./fastlane { };

  android = callPackage ./android {
    inherit localMavenRepoBuilder projectNodePackage;
    status-go = status-go.mobile.android;
  };

  ios = callPackage ./ios {
    inherit xcodeWrapper projectNodePackage fastlane;
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
  buildInputs = unique (catAttrs "buildInputs" selectedSources);

  shell = lib.mergeSh (mkShell {}) (catAttrs "shell" selectedSources);

  # TARGETS
  inherit android ios fastlane;
}
