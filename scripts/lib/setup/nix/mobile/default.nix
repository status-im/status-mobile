{ stdenv, pkgs }:

with pkgs;
with stdenv; 

let
  android-ndk = callPackage ./android-ndk { };

in
  {
    buildInputs = [
      android-ndk
      openjdk
    ];
    shellHook = ''
      export JAVA_HOME="${openjdk}"
      export ANDROID_HOME=~/.status/Android/Sdk
      export ANDROID_SDK_ROOT="$ANDROID_HOME"
      export ANDROID_NDK_ROOT="${android-ndk}"
      export ANDROID_NDK_HOME="${android-ndk}"
      export ANDROID_NDK="${android-ndk}"
      export PATH="$ANDROID_HOME/bin:$ANDROID_HOME/tools:$ANDROID_HOME/tools/bin:$ANDROID_HOME/platform-tools:$ANDROID_HOME/build-tools:$PATH"
    '';
  }
