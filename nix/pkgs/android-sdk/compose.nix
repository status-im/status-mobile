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
  buildToolsVersions = [ "31.0.0" ];
  platformVersions = [ "31" ];
  cmakeVersions = [ "3.18.1" ];
  ndkVersion = "22.1.7171670";
  includeNDK = true;
  includeExtras = [
    "extras;android;m2repository"
    "extras;google;m2repository"
  ];
}
