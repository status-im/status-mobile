{ stdenv, pkgs, target-os ? "all", status-go, androidPkgs }:

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

in
  {
    buildInputs =
      lib.optionals targetAndroid [
        openjdk gradle
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
