{ config, stdenv, stdenvNoCC, target-os, callPackage,
  pkgs, mkFilter, androidenv, fetchurl, openjdk, nodejs, bash, maven, zlib,
  status-go, localMavenRepoBuilder, projectNodePackage, prod-build-fn }:

with stdenv;

let
  platform = callPackage ../../platform.nix { inherit target-os; };

  inherit (callPackage ./android-env.nix { }) androidComposition licensedAndroidEnv;

  mavenAndNpmDeps = callPackage ./maven-and-npm-deps { inherit stdenv stdenvNoCC gradle bash zlib androidEnvShellHook localMavenRepoBuilder mkFilter projectNodePackage status-go; };
  gradle = pkgs.gradleGen.gradleGen rec {
    name = "gradle-5.1.1";
    nativeVersion = "0.14";

    src = pkgs.fetchurl {
      url = "http://services.gradle.org/distributions/${name}-bin.zip";
      sha256 = "16671jp5wdr3q6p91h6szkgcxg3mw9wpgp6hjygbimy50lv34ls9";
    };
  };

  prod-build = (prod-build-fn { inherit projectNodePackage; });

  androidEnvShellHook = assert platform.targetAndroid; ''
    export JAVA_HOME="${openjdk}"
    export ANDROID_HOME="${licensedAndroidEnv}"
    export ANDROID_SDK_ROOT="$ANDROID_HOME"
    export ANDROID_NDK_ROOT="${androidComposition.androidsdk}/libexec/android-sdk/ndk-bundle"
    export ANDROID_NDK_HOME="$ANDROID_NDK_ROOT"
    export ANDROID_NDK="$ANDROID_NDK_ROOT"
    export PATH="$ANDROID_HOME/bin:$ANDROID_HOME/tools:$ANDROID_HOME/tools/bin:$ANDROID_HOME/platform-tools:$ANDROID_HOME/build-tools:$PATH"
  '';

  # TARGETS
  release = callPackage ./targets/release-android.nix { inherit target-os gradle androidEnvShellHook mavenAndNpmDeps mkFilter nodejs prod-build status-go zlib; };
  generate-maven-and-npm-deps-shell = callPackage ./maven-and-npm-deps/maven/shell.nix { inherit gradle maven androidEnvShellHook projectNodePackage status-go; };
  adb-shell = pkgs.mkShell {
    buildInputs = [ licensedAndroidEnv ];
    shellHook = androidEnvShellHook ;
  };

in {
  inherit androidComposition;

  buildInputs = assert platform.targetAndroid; [ mavenAndNpmDeps.buildInputs openjdk gradle ];
  shellHook =
    androidEnvShellHook + 
    mavenAndNpmDeps.shellHook + ''
    $STATUS_REACT_HOME/scripts/generate-keystore.sh

    $STATUS_REACT_HOME/nix/mobile/reset-node_modules.sh "${mavenAndNpmDeps.buildInputs}/project" && \
    $STATUS_REACT_HOME/nix/mobile/android/fix-node_modules-permissions.sh || exit
  '';

  # TARGETS
  inherit release generate-maven-and-npm-deps-shell;
  adb = {
    shell = adb-shell;
  };
}
