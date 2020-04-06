{ stdenv, lib, config, callPackage,
  mkFilter, bash, file, gnumake, watchmanFactory, gradle,
  androidEnvShellHook, mavenAndNpmDeps,
  nodejs, openjdk, jsbundle, status-go, unzip, zlib }:

{ secrets-file ? "", # Path to the file containing secret environment variables
  watchmanSockPath ? "", # Path to the socket file exposed by an external watchman instance (workaround needed for building Android on macOS)
  env ? {} # Attribute set containing environment variables to expose to the build script
}:

assert (builtins.stringLength watchmanSockPath) > 0 -> stdenv.isDarwin;

let
  inherit (lib) attrByPath hasAttrByPath optionalAttrs;
  env' = env // optionalAttrs (hasAttrByPath ["status-im" "status-go" "src-override"] config) {
    STATUS_GO_SRC_OVERRIDE = config.status-im.status-go.src-override;
  } // optionalAttrs (hasAttrByPath ["status-im" "nimbus" "src-override"] config) {
    NIMBUS_SRC_OVERRIDE = config.status-im.nimbus.src-override;
  };
  inherit (config.status-im) build-type;
  inherit (config.status-im.status-react) build-number;
  gradle-opts = (attrByPath ["status-im" "status-react" "gradle-opts"] "" config);
  # Path to the .keystore file used to sign the Android APK
  keystore-file = (attrByPath ["status-im" "status-react" "keystore-file"] "" config);
  baseName = "release-android";
  name = "status-react-build-${baseName}";
  gradleHome = "$NIX_BUILD_TOP/.gradle";
  localMavenRepo = "${mavenAndNpmDeps.drv}/.m2/repository";
  sourceProjectDir = "${mavenAndNpmDeps.drv}/project";
  envFileName =
    if (build-type == "release" || build-type == "nightly" || build-type == "e2e") then ".env.${build-type}" else
    if build-type != "" then ".env.jenkins" else ".env";
  buildType = if (build-type == "pr" || build-type == "e2e") then "pr" else "release"; /* PR builds shouldn't replace normal releases */
  apksPath = "$sourceRoot/android/app/build/outputs/apk/${buildType}";
  patchedWatchman = watchmanFactory watchmanSockPath;

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
          root = path;
          include = [
            "mobile/js_files.*" "resources/.*"
            "modules/react-native-status/android.*"
            envFileName "VERSION" ".watchmanconfig"
            "status-go-version.json" "react-native.config.js"
          ];
        };
    };
  nativeBuildInputs = [ bash gradle unzip ] ++ lib.optionals stdenv.isDarwin [ file gnumake patchedWatchman ];
  buildInputs = [ nodejs openjdk ];
  phases = [ "unpackPhase" "patchPhase" "buildPhase" "checkPhase" "installPhase" ];
  unpackPhase = ''
    runHook preUnpack

    cp -r $src ./project
    chmod u+w -R ./project

    export sourceRoot=$PWD/project

    runHook postUnpack
  '';
  postUnpack = ''
    mkdir -p ${gradleHome}

    ${if keystore-file != "" then "cp -a --no-preserve=ownership ${keystore-file} ${gradleHome}/; export KEYSTORE_PATH=${gradleHome}/$(basename ${keystore-file})" else ""}

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
      exportEnvVars = concatStringsSep ";" (mapAttrsToList (name: value: "export ${name}='${value}'") env');
      unsetEnvVars = concatStringsSep ";" (mapAttrsToList (name: value: "unset ${name}") env');
      adhocEnvVars = optionalString stdenv.isLinux "LD_LIBRARY_PATH=$LD_LIBRARY_PATH:${makeLibraryPath [ zlib ]}";
      capitalizedBuildType = toUpper (substring 0 1 buildType) + substring 1 (-1) buildType;
    in ''
    export STATUS_REACT_HOME=$PWD
    export HOME=$sourceRoot

    ${exportEnvVars}
    ${if secrets-file != "" then "source ${secrets-file}" else ""}

    ${androidEnvShellHook}
    ${concatStrings (catAttrs "shellHook" [ mavenAndNpmDeps.shell status-go.shell ])}

    pushd $sourceRoot/android
    ${adhocEnvVars} ./gradlew -PversionCode=${assert build-number != ""; build-number} assemble${capitalizedBuildType} || exit
    popd > /dev/null

    ${unsetEnvVars}
  '';
  doCheck = true;
  checkPhase = ''
    ls ${apksPath}/*.apk | xargs -n1 unzip -qql | grep 'assets/index.android.bundle'
  '';
  installPhase = ''
    mkdir -p $out
    cp ${apksPath}/*.apk $out/
  '';
}
