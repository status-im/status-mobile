#
# This Nix expression centralizes the configuration
# for the Android development environment.
#

{ androidenv, lib, stdenv }:

assert lib.assertMsg (stdenv.system != "aarch64-darwin")
  "aarch64-darwin not supported for Android SDK. Use: NIXPKGS_SYSTEM_OVERRIDE=x86_64-darwin";

# The "android-sdk-license" license is accepted
# by setting android_sdk.accept_license = true.
androidenv.composeAndroidPackages {
  cmdLineToolsVersion = "9.0";
  toolsVersion = "26.1.1";
  platformToolsVersion = "33.0.3";
  buildToolsVersions = [ "33.0.0" ];
  platformVersions = [ "33" ];
  cmakeVersions = [ "3.22.1" ];
  ndkVersion = "23.1.7779620";
  includeNDK = true;
  includeExtras = [
    "extras;android;m2repository"
    "extras;google;m2repository"
  ];
}
