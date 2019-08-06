{ stdenv, stdenvNoCC, lib, target-os, callPackage,
  mkFilter, bash, file, gnumake, watchman, gradle,
  androidEnvShellHook, mavenAndNpmDeps,
  nodejs, openjdk, jsbundle, status-go, zlib }:

{ build-number,
  build-type, # Build type (e.g. nightly, release, e2e). Default is to use .env.nightly file
  gradle-opts ? "",
  keystore-file ? "", # Path to the .keystore file used to sign the package
  secrets-file ? "", # Path to the file containing secret environment variables
  env ? {} # Attribute set contaning environment variables to expose to the build script
}:

let
  baseName = "release-${target-os}";
  name = "status-react-build-${baseName}";
  gradleHome = "$NIX_BUILD_TOP/.gradle";
  localMavenRepo = "${mavenAndNpmDeps.deriv}/.m2/repository";
  sourceProjectDir = "${mavenAndNpmDeps.deriv}/project";
  envFileName =
    if (build-type == "release" || build-type == "nightly" || build-type == "e2e") then ".env.${build-type}" else
    if build-type != "" then ".env.jenkins" else ".env";
  buildType' = if (build-type == "pr" || build-type == "e2e") then "pr" else "release"; /* PR builds shouldn't replace normal releases */
  generatedApkPath = "$sourceRoot/android/app/build/outputs/apk/${buildType'}/app-${buildType'}.apk";
  outApkName = "app.apk";

in stdenv.mkDerivation {
  inherit name;
  src =
    let path = ./../../../..;
    in builtins.path { # We use builtins.path so that we can name the resulting derivation, otherwise the name would be taken from the checkout directory, which is outside of our control
      inherit path;
      name = "status-react-source-${baseName}";
      filter =
        # Keep this filter as restrictive as possible in order to avoid unnecessary rebuilds and limit closure size
        mkFilter {
          dirRootsToInclude = [
            "mobile_files"
            "modules/react-native-status"
            "resources"
          ];
          dirsToExclude = [ ".git" ".svn" "CVS" ".hg" ".gradle" "build" "intermediates" "libs" "obj" ];
          filesToInclude = [ envFileName "status-go-version.json" "VERSION" "react-native.config.js" ];
          root = path;
        };
    };
  nativeBuildInputs = [ bash gradle ] ++ lib.optionals stdenv.isDarwin [ file gnumake watchman ];
  buildInputs = [ nodejs openjdk ];
  phases = [ "unpackPhase" "patchPhase" "buildPhase" "installPhase" ];
  unpackPhase = ''
    runHook preUnpack

    cp -r $src ./project
    chmod u+w -R ./project

    export sourceRoot=$PWD/project

    runHook postUnpack
  '';
  postUnpack = ''
    mkdir -p ${gradleHome}

    ${if keystore-file != "" then "cp -a --no-preserve=ownership ${keystore-file} ${gradleHome}/; export STATUS_RELEASE_STORE_FILE=${gradleHome}/$(basename ${keystore-file})" else ""}

    # Ensure we have the right .env file
    cp -f $sourceRoot/${envFileName} $sourceRoot/.env

    # Copy index.*.js input file
    cp -a --no-preserve=ownership ${jsbundle}/index*.js $sourceRoot/

    # Copy android/ directory
    cp -a --no-preserve=ownership ${sourceProjectDir}/android/ $sourceRoot/
    chmod u+w $sourceRoot/android
    chmod u+w $sourceRoot/android/app
    mkdir $sourceRoot/android/build && chmod -R u+w $sourceRoot/android/build

    # Copy node_modules/ directory
    cp -a --no-preserve=ownership ${sourceProjectDir}/node_modules/ $sourceRoot/
    # Make android/build directories writable under node_modules
    for d in `find $sourceRoot/node_modules -type f -name build.gradle | xargs dirname`; do
      chmod -R u+w $d
    done
  '';
  patchPhase = ''
    prevSet=$-
    set -e

    substituteInPlace $sourceRoot/android/gradlew \
      --replace \
        'exec gradle' \
        "exec gradle -Dmaven.repo.local='${localMavenRepo}' --offline ${gradle-opts}"

    set $prevSet
  '';
  buildPhase =
    let
      inherit (lib) catAttrs concatStrings concatStringsSep mapAttrsToList makeLibraryPath optionalString substring toUpper;
      # Take the env attribute set and build a couple of scripts
      #  (one to export the environment variables, and another to unset them)
      exportEnvVars = concatStringsSep ";" (mapAttrsToList (name: value: "export ${name}='${value}'") env);
      unsetEnvVars = concatStringsSep ";" (mapAttrsToList (name: value: "unset ${name}") env);
      adhocEnvVars = optionalString stdenv.isLinux "LD_LIBRARY_PATH=$LD_LIBRARY_PATH:${makeLibraryPath [ zlib ]}";
      capitalizedBuildType = toUpper (substring 0 1 buildType') + substring 1 (-1) buildType';
    in ''
    export STATUS_REACT_HOME=$PWD
    export HOME=$sourceRoot

    ${exportEnvVars}
    ${if secrets-file != "" then "source ${secrets-file}" else ""}

    ${androidEnvShellHook}
    ${concatStrings (catAttrs "shellHook" [ mavenAndNpmDeps status-go ])}

    pushd $sourceRoot/android
    ${adhocEnvVars} ./gradlew -PversionCode=${build-number} assemble${capitalizedBuildType} || exit
    popd > /dev/null

    ${unsetEnvVars}
  '';
  installPhase = ''
    mkdir -p $out
    cp ${generatedApkPath} $out/${outApkName}
  '';
}
