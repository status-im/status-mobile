#
# This Nix expression centralizes the configuration
# for the Android development environment.
#

{ stdenv, config, callPackage, androidenv, openjdk, mkShell }:

androidenv.composeAndroidPackages {
  toolsVersion = "26.1.1";
  platformToolsVersion = "29.0.6";
  buildToolsVersions = [ "29.0.2" ];
  includeEmulator = false;
  platformVersions = [ "29" ];
  includeSources = false;
  includeDocs = false;
  includeSystemImages = false;
  systemImageTypes = [ "default" ];
  lldbVersions = [ "3.1.4508709" ];
  cmakeVersions = [ "3.10.2" ];
  includeNDK = true;
  ndkVersion = "21.0.6113669";
  useGoogleAPIs = false;
  useGoogleTVAddOns = false;
  includeExtras = [
    "extras;android;m2repository"
    "extras;google;m2repository"
  ];
}
