{ config, lib, stdenvNoCC, callPackage,status-go, 
  localMavenRepoBuilder, composeXcodeWrapper, mkShell, mergeSh }:

let
  inherit (lib) catAttrs concatStrings optional unique;

  xcodewrapperArgs = {
    version = "11.2.1";
  };
  xcodeWrapper = composeXcodeWrapper xcodewrapperArgs;
  fastlane = callPackage ./fastlane { };
  androidPlatform = callPackage ./android {
    inherit localMavenRepoBuilder projectNodePackage;
    status-go = status-go.android;
  };
  iosPlatform = callPackage ./ios {
    inherit xcodeWrapper projectNodePackage fastlane;
    status-go = status-go.ios;
  };
  selectedSources = [
    fastlane
    status-go.android
    status-go.ios
    androidPlatform 
    iosPlatform
  ];

  projectNodePackage = callPackage ./node-package.nix { inherit (lib) importJSON; };

in {
  buildInputs = unique (catAttrs "buildInputs" selectedSources);

  shell = mergeSh (mkShell {}) (catAttrs "shell" selectedSources);

  # CHILD DERIVATIONS
  android = androidPlatform;
  ios = iosPlatform;

  # TARGETS
  inherit fastlane xcodeWrapper;
}
