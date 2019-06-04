{ config, stdenv, pkgs, callPackage, mkShell, fetchurl, fetchFromGitHub, target-os,
  mkFilter, localMavenRepoBuilder, maven, status-go, composeXcodeWrapper, nodejs, yarn, prod-build-fn }:

let
  inherit (stdenv.lib) catAttrs concatStrings optional unique;

  platform = callPackage ../platform.nix { inherit target-os; };
  xcodewrapperArgs = {
    version = "10.2.1";
  };
  xcodeWrapper = composeXcodeWrapper xcodewrapperArgs;
  androidPlatform = callPackage ./android { inherit config target-os mkShell mkFilter nodejs maven localMavenRepoBuilder projectNodePackage prod-build-fn; status-go = status-go.android; };
  iosPlatform = callPackage ./ios { inherit config mkFilter mkShell xcodeWrapper projectNodePackage; status-go = status-go.ios; };
  fastlane = callPackage ./fastlane { inherit stdenv target-os mkShell; };
  selectedSources = [
      fastlane
      status-go.android
      status-go.ios
    ] ++
    optional platform.targetAndroid androidPlatform ++
    optional platform.targetIOS iosPlatform;

  projectNodePackage = callPackage ./node-package.nix { inherit pkgs nodejs yarn; };

  # TARGETS
  prod-build = prod-build-fn { inherit projectNodePackage; };

in {
  buildInputs = unique (catAttrs "buildInputs" selectedSources);
  shellHook = concatStrings (catAttrs "shellHook" selectedSources);

  # CHILD DERIVATIONS
  android = androidPlatform;
  ios = iosPlatform;

  # TARGETS
  inherit prod-build fastlane;
}
