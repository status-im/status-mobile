#
# This Nix expression centralizes the configuration
# for the Android development environment.
#

{ androidenv }:

# The "android-sdk-license" license is accepted
# by setting android_sdk.accept_license = true.
androidenv.composeAndroidPackages {
  toolsVersion = "26.1.1";
  platformToolsVersion = "33.0.1";
  buildToolsVersions = [ "31.0.0" ];
  platformVersions = [ "30" ];
  cmakeVersions = [ "3.18.1" ];
  ndkVersion = "22.1.7171670";
  includeNDK = true;
  includeExtras = [
    "extras;android;m2repository"
    "extras;google;m2repository"
  ];
}
