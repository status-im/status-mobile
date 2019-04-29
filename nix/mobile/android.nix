{ config, stdenv, pkgs }:

with pkgs;
with stdenv;

let
  gradle = gradle_4_10;
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

in
  {
    inherit androidComposition;

    buildInputs = [ openjdk gradle ];
    shellHook = ''
      export JAVA_HOME="${openjdk}"
      export ANDROID_HOME=~/.status/Android/Sdk
      export ANDROID_SDK_ROOT="$ANDROID_HOME"
      export ANDROID_NDK_ROOT="${androidComposition.androidsdk}/libexec/android-sdk/ndk-bundle"
      export ANDROID_NDK_HOME="$ANDROID_NDK_ROOT"
      export ANDROID_NDK="$ANDROID_NDK_ROOT"
      export PATH="$ANDROID_HOME/bin:$ANDROID_HOME/tools:$ANDROID_HOME/tools/bin:$ANDROID_HOME/platform-tools:$ANDROID_HOME/build-tools:$PATH"
    '' +
    ## We need to make a writeable copy of the Android SDK so that we can accept the license (which causes files to be written to the SDK folders)
    ## since the nix store is immutable by nature, we can't license the SDK from there.
    ''
      if ! [ -d $ANDROID_HOME ]; then
        echo "=> pulling the Android SDK out of the nix store and into a writeable directory"

        mkdir -p $ANDROID_HOME
        cp -rL ${androidComposition.androidsdk}/bin $ANDROID_HOME
        cp -rL ${androidComposition.androidsdk}/libexec/android-sdk/* $ANDROID_HOME/
        chmod -R 755 $ANDROID_HOME/
    '' + lib.optionalString config.android_sdk.accept_license ''
        echo "=> accepting Android SDK licenses"
        pushd $ANDROID_HOME
          yes | $PWD/bin/sdkmanager --licenses || if [ $? -ne '141' ]; then exit $?; fi;  #Captures SIGPIPE 141 error but still allow repeating "y" to accept all licenses
        popd
    '' +
    ''
        echo "=> generating keystore"
        $PWD/scripts/generate-keystore.sh
      fi
    '';
  }
