{ stdenv, stdenvNoCC, lib, target-os, callPackage,
  mkFilter, bash, gradle, androidEnvShellHook, mavenAndNpmDeps, nodejs, openjdk, prod-build, status-go, zlib }:

{ build-number ? "9999",
  build-type ? "nightly", # Build type (e.g. nightly, release, e2e). Default is to use .env.nightly file
  gradle-opts ? "",
  keystore-file ? "", # Path to the .keystore file used to sign the package
  secrets-file ? "", # Path to the file containing secret environment variables
  env ? {} # Attribute set contaning environment variables to expose to the build script
}:

let
  name = "release-${target-os}";
  gradleHome = "$NIX_BUILD_TOP/.gradle";
  localMavenRepo = "${mavenAndNpmDeps.buildInputs}/.m2/repository";
  sourceProjectDir = "${mavenAndNpmDeps.buildInputs}/project";
  envFileName =
    if (build-type == "release" || build-type == "nightly" || build-type == "e2e") then ".env.${build-type}" else
    if build-type != "" then ".env.jenkins" else ".env";
  buildType' = if (build-type == "release" || build-type == "nightly") then "release" else "pr";
  generatedApkPath = "android/app/build/outputs/apk/${buildType'}/app-${buildType'}.apk";
  outApkName = "app.apk";

in stdenv.mkDerivation {
  inherit name;
  src =
    let path = ./../../../..;
    in builtins.path { # We use builtins.path so that we can name the resulting derivation, otherwise the name would be taken from the checkout directory, which is outside of our control
      inherit path;
      name = "status-react-${name}-source";
      filter =
        # Keep this filter as restrictive as possible in order to avoid unnecessary rebuilds and limit closure size
        mkFilter {
          dirRootsToInclude = [ 
            "mobile_files"
            "modules/react-native-status"
            "packager"
            "resources"
          ];
          dirsToExclude = [ ".git" ".svn" "CVS" ".hg" ".gradle" "build" "intermediates" "libs" "obj" ];
          filesToInclude = [ envFileName "STATUS_GO_VERSION" "VERSION" ];
          root = path;
        };
    };
  buildInputs = [ bash gradle nodejs openjdk ];
  phases = [ "unpackPhase" "patchPhase" "buildPhase" "installPhase" ];
  postUnpack = ''
    mkdir -p ${gradleHome}

    ${if keystore-file != "" then "cp -a --no-preserve=ownership ${keystore-file} ${gradleHome}/; export STATUS_RELEASE_STORE_FILE=${gradleHome}/$(basename ${keystore-file})" else ""}

    # Ensure we have the right .env file
    cp -f $sourceRoot/${envFileName} $sourceRoot/.env

    # Copy index.*.js input file
    cp -a --no-preserve=ownership ${prod-build}/index*.js $sourceRoot/

    # Copy android/ directory
    cp -a --no-preserve=ownership ${sourceProjectDir}/android/ $sourceRoot/
    chmod u+w $sourceRoot/android
    chmod u+w $sourceRoot/android/app
    chmod -R u+w $sourceRoot/android/.gradle
    mkdir $sourceRoot/android/build && chmod -R u+w $sourceRoot/android/build

    # Copy node_modules/ directory
    #ln -sf ${sourceProjectDir}/node_modules/

    # Make android/build directories writable under node_modules
    cp -a --no-preserve=ownership ${sourceProjectDir}/node_modules/ $sourceRoot/
    chmod -R u+w $sourceRoot/node_modules/react-native/
    for d in `ls $sourceRoot/node_modules/react-native-*/android/build -d1`; do
      chmod -R u+w $d
    done
    for d in `ls $sourceRoot/node_modules/react-native-*/android -d1`; do
      chmod u+w $d
    done
    chmod u+w $sourceRoot/node_modules/realm/android
  '';
  patchPhase = ''
    prevSet=$-
    set -e

    substituteInPlace android/gradlew \
      --replace 'exec gradle' 'exec gradle -S -Dmaven.repo.local='${localMavenRepo}' --offline ${gradle-opts}'
    substituteInPlace android/app/build.gradle \
      --replace 'versionCode 9999' 'versionCode ${build-number}'

    # OPTIONAL: There's no need to forward debug ports for a release build, just disable it
    substituteInPlace node_modules/realm/android/build.gradle \
      --replace 'compileTask.dependsOn forwardDebugPort' 'compileTask'

    set $prevSet
  '';
  buildPhase =
    let
      exportEnvVars = lib.concatStrings (
        builtins.attrValues (
          lib.mapAttrs (name: value:
            "export ${name}='${value}';"
            ) env));
      unsetEnvVars = lib.concatStrings (
        builtins.attrValues (
          lib.mapAttrs (name: value:
            "unset ${name};"
            ) env));
      adhocEnvVars = if stdenv.isLinux then "LD_LIBRARY_PATH=$LD_LIBRARY_PATH:${lib.makeLibraryPath [ zlib ]}" else "";
      capitalizedBuildType = lib.toUpper (lib.substring 0 1 buildType') + lib.substring 1 (-1) buildType';
    in ''
    export STATUS_REACT_HOME=$PWD
    export HOME=$NIX_BUILD_TOP

    ${exportEnvVars}
    ${if secrets-file != "" then "source ${secrets-file}" else ""}
  '' +
    mavenAndNpmDeps.shellHook +
    androidEnvShellHook +
    status-go.shellHook + ''
    pushd android
    ${adhocEnvVars} gradle assemble${capitalizedBuildType} -S -Dmaven.repo.local='${localMavenRepo}' --offline ${gradle-opts} || exit
    popd > /dev/null

    ${unsetEnvVars}
  '';
  installPhase = ''
    mkdir -p $out
    cp ${generatedApkPath} $out/${outApkName}
  '';
}
