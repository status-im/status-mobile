{ config, stdenv, pkgs, target-os ? "all", status-go }:

with pkgs;
with stdenv;

let
  gradle = gradle_4_10;
  targetAndroid = {
    "android" = true;
    "all" = true;
  }.${target-os} or false;
  targetIOS = {
    "ios" = true;
    "all" = true;
  }.${target-os} or false;
  xcodewrapperArgs = {
    version = "10.1";
  };
  xcodeWrapper = xcodeenv.composeXcodeWrapper xcodewrapperArgs;
  android = callPackage ./android.nix { inherit config; };

in
  {
    inherit (android) androidComposition;
    inherit xcodewrapperArgs;

    buildInputs =
      lib.optional targetAndroid android.buildInputs ++
      lib.optional (targetIOS && isDarwin) xcodeWrapper;
    shellHook =
      lib.optionalString targetIOS ''
        export RCTSTATUS_FILEPATH=${status-go}/lib/ios/Statusgo.framework
      '' +
      lib.optionalString targetAndroid android.shellHook;
  }
