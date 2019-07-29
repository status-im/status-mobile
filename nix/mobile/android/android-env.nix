#
# This Nix expression centralizes the configuration for the Android development environment
#

{ stdenv, config, target-os, callPackage,
  androidenv, openjdk }:

let
  platform = callPackage ../../platform.nix { inherit target-os; };

  androidComposition = androidenv.composeAndroidPackages {
    toolsVersion = "26.1.1";
    platformToolsVersion = "28.0.2";
    buildToolsVersions = [ "28.0.3" ];
    includeEmulator = false;
    platformVersions = [ "28" ];
    includeSources = false;
    includeDocs = false;
    includeSystemImages = false;
    systemImageTypes = [ "default" ];
    abiVersions = [ "armeabi-v7a" ];
    lldbVersions = [ "2.0.2558144" ];
    cmakeVersions = [ "3.6.4111459" ];
    includeNDK = true;
    ndkVersion = "19.2.5345600";
    useGoogleAPIs = false;
    useGoogleTVAddOns = false;
    includeExtras = [ "extras;android;m2repository" "extras;google;m2repository" ];
  };
  licensedAndroidEnv = stdenv.mkDerivation rec {
    name = "licensed-android-sdk";
    version = "licensed";
    phases = [ "installPhase" ];
    installPhase = ''
      mkdir -p $out/libexec/android-sdk
      ln -s "${androidComposition.androidsdk}/bin" $out/bin
      for d in ${androidComposition.androidsdk}/libexec/android-sdk/*; do
        ln -s $d $out/$(basename $d)
      done
    '' + stdenv.lib.optionalString config.android_sdk.accept_license ''
      mkdir -p $out/licenses
      echo -e "\n601085b94cd77f0b54ff86406957099ebe79c4d6" > "$out/licenses/android-googletv-license"
      echo -e "\n24333f8a63b6825ea9c5514f83c2829b004d1fee" > "$out/licenses/android-sdk-license"
      echo -e "\n84831b9409646a918e30573bab4c9c91346d8abd" > "$out/licenses/android-sdk-preview-license"
      echo -e "\nd975f751698a77b662f1254ddbeed3901e976f5a" > "$out/licenses/intel-android-extra-license"
      echo -e "\n33b6a2b64607f11b759f320ef9dff4ae5c47d97a" > "$out/licenses/google-gdk-license"
    '';
  };
  shellHook = assert platform.targetAndroid;
    ''
      export JAVA_HOME="${openjdk}"
      export ANDROID_HOME="${licensedAndroidEnv}"
      export ANDROID_SDK_ROOT="$ANDROID_HOME"
      export ANDROID_NDK_ROOT="${androidComposition.androidsdk}/libexec/android-sdk/ndk-bundle"
      export ANDROID_NDK_HOME="$ANDROID_NDK_ROOT"
      export ANDROID_NDK="$ANDROID_NDK_ROOT"
      export PATH="$ANDROID_HOME/bin:$ANDROID_HOME/tools:$ANDROID_HOME/tools/bin:$ANDROID_HOME/platform-tools:$ANDROID_HOME/build-tools:$PATH"
    '';

in {
  inherit androidComposition licensedAndroidEnv shellHook;
}