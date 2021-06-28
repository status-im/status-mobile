#
# This Nix expression centralizes the configuration
# for the Android development environment.
#

{ stdenv, config, callPackage, androidenv, openjdk, mkShell }:

androidenv.composeAndroidPackages {
  toolsVersion = "26.1.1";
  platformToolsVersion = "31.0.2";
  buildToolsVersions = [ "30.0.3" ];
  includeEmulator = false;
  includeSources = false;
  platformVersions = [ "29" ];
  includeSystemImages = false;
  systemImageTypes = [ "default" ];
  cmakeVersions = [ "3.18.1" ];
  includeNDK = true;
  ndkVersion = "22.1.7171670";
  useGoogleAPIs = false;
  useGoogleTVAddOns = false;
  includeExtras = [
    "extras;android;m2repository"
    "extras;google;m2repository"
  ];
  # The "android-sdk-license" license is accepted
  # by setting android_sdk.accept_license = true.
  extraLicenses = [];
}
