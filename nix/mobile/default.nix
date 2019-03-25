{ stdenv, pkgs, target-os ? "", status-go, androidPkgs }:

with pkgs;
with stdenv;

let
  targetAndroid = {
    "android" = true;
    "" = true;
  }.${target-os} or false;
  targetIOS = {
    "ios" = true;
    "" = true;
  }.${target-os} or false;

in
  {
    buildInputs =
      [ bundler ruby ] ++ ## bundler/ruby used for fastlane
      lib.optional targetAndroid [
        openjdk
      ];
    shellHook =
      lib.optionalString targetIOS ''
        export RCTSTATUS_FILEPATH=${status-go}/lib/ios/Statusgo.framework
      '' +
      lib.optionalString targetAndroid ''
        export JAVA_HOME="${openjdk}"
        export ANDROID_HOME=~/.status/Android/Sdk
        export ANDROID_SDK_ROOT="$ANDROID_HOME"
        export ANDROID_NDK_ROOT="${androidPkgs.ndk-bundle}/libexec/android-sdk/ndk-bundle"
        export ANDROID_NDK_HOME="$ANDROID_NDK_ROOT"
        export ANDROID_NDK="$ANDROID_NDK_ROOT"
        export PATH="$ANDROID_HOME/bin:$ANDROID_HOME/tools:$ANDROID_HOME/tools/bin:$ANDROID_HOME/platform-tools:$ANDROID_HOME/build-tools:$PATH"
      '';
  }
