{ config, stdenv, stdenvNoCC, target-os ? "android", callPackage, mkShell,
  mkFilter, androidenv, fetchurl, openjdk, nodejs, bash, maven, zlib,
  status-go, localMavenRepoBuilder, projectNodePackage, jsbundle }:

let
  platform = callPackage ../../platform.nix { inherit target-os; };

  androidEnv = callPackage ./android-env.nix { inherit target-os openjdk; };
  gradle = callPackage ./gradle.nix { };

  # Import a local patched version of node_modules, together with a local version of the Maven repo
  mavenAndNpmDeps = callPackage ./maven-and-npm-deps { inherit stdenv stdenvNoCC gradle bash nodejs zlib localMavenRepoBuilder mkFilter projectNodePackage status-go; androidEnvShellHook = androidEnv.shellHook; };

  # TARGETS
  release = callPackage ./targets/release-android.nix { inherit target-os gradle mavenAndNpmDeps mkFilter nodejs jsbundle status-go zlib; androidEnvShellHook = androidEnv.shellHook; };
  generate-maven-and-npm-deps-shell = callPackage ./maven-and-npm-deps/maven/shell.nix { inherit gradle maven nodejs projectNodePackage status-go; androidEnvShellHook = androidEnv.shellHook; };
  adb-shell = mkShell {
    buildInputs = [ androidEnv.licensedAndroidEnv ];
    inherit (androidEnv) shellHook;
  };

in {
  inherit (androidEnv) androidComposition;

  buildInputs = assert platform.targetAndroid; [ mavenAndNpmDeps.deriv openjdk gradle ];
  shellHook =
    let
      inherit (stdenv.lib) catAttrs concatStrings;
    in ''
    ${concatStrings (catAttrs "shellHook" [ mavenAndNpmDeps androidEnv ])}
    
    $STATUS_REACT_HOME/scripts/generate-keystore.sh

    $STATUS_REACT_HOME/nix/mobile/reset-node_modules.sh "${mavenAndNpmDeps.deriv}/project" || exit
  '';

  # TARGETS
  inherit release generate-maven-and-npm-deps-shell;
  adb = {
    shell = adb-shell;
  };
}
