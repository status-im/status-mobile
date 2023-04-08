#
# This Nix expression centralizes the configuration
# for the Android development environment.
#

{ androidenv }:

# The "android-sdk-license" license is accepted
# by setting android_sdk.accept_license = true.
androidenv.composeAndroidPackages {
  toolsVersion = "26.1.1";
  platformToolsVersion = "33.0.2";
  buildToolsVersions = [ "30.0.0" ];
  platformVersions = [ "31" ];
  cmakeVersions = [ "3.18.1" ];
  ndkVersion = "24.0.8215888";
  includeNDK = true;
  includeExtras = [
    "extras;android;m2repository"
    "extras;google;m2repository"
  ];
}
