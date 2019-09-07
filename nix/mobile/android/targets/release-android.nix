{ stdenv, lib, config, callPackage, bash, file, gnumake, watchmanFactory, gradle
, androidPkgs, mavenAndNpmDeps, nodejs, openjdk, jsbundle, status-go, unzip, zlib }:

{
  buildEnv ? "prod", # Value for BUILD_ENV checked by Clojure code at compile time
  secretsFile ? "", # Path to the file containing secret environment variables
  watchmanSockPath ? "", # Path to the socket file exposed by an external watchman instance (workaround needed for building Android on macOS)
}:

assert (lib.stringLength watchmanSockPath) > 0 -> stdenv.isDarwin;

let
  inherit (lib)
    toLower splitString optionalString
    attrByPath hasAttrByPath optionalAttrs;

  # helper for getting config values
  safeGetConfig = name: default:
    let path = [ "status-im" ] ++ (splitString "." name);
    in attrByPath path default config;

  # custom env variables derived from config
  env = {
    ANDROID_ABI_SPLIT = safeGetConfig "android.abi-split" false;
    ANDROID_ABI_INCLUDE = safeGetConfig "android.abi-include" "armeabi-v7a;arm64-v8a;x86";
    STATUS_GO_SRC_OVERRIDE = safeGetConfig "nimbus.src-override" null;
  };

  buildType = safeGetConfig "build-type" "prod";
  buildNumber = safeGetConfig "build-number" "9999";
  gradleOpts = safeGetConfig "android.gradle-opts" "";
  keystorePath = safeGetConfig "android.keystore-path" "";
  # Keep the same keystore path for determinism
  keystoreLocal = "${gradleHome}/status-im.keystore";

  baseName = "release-android";
  name = "status-react-build-${baseName}";

  gradleHome = "$NIX_BUILD_TOP/.gradle";
  localMavenRepo = "${mavenAndNpmDeps.drv}/.m2/repository";
  sourceProjectDir = "${mavenAndNpmDeps.drv}/project";
  envFileName = if (buildType == "release" || buildType == "nightly" || buildType == "e2e")
    then ".env.${buildType}"
    else if buildType != "" then ".env.jenkins"
    else ".env";

  # There are only two types of Gradle builds: pr and release
  gradleBuildType = if (buildType == "pr" || buildType == "e2e")
    then "Pr"
    else "Release"; # PR builds shouldn't replace normal releases

  apksPath = "$sourceRoot/android/app/build/outputs/apk/${toLower gradleBuildType}";
  patchedWatchman = watchmanFactory watchmanSockPath;

in stdenv.mkDerivation rec {
  inherit name;
  src = let path = ./../../../..;
  # We use builtins.path so that we can name the resulting derivation
  in builtins.path {
    inherit path;
    name = "status-react-source-${baseName}";
    # Keep this filter as restrictive as possible in order to avoid unnecessary rebuilds and limit closure size
    filter = lib.mkFilter {
      root = path;
      include = [
        "mobile/js_files.*" "resources/.*"
        "modules/react-native-status/android.*"
        envFileName "VERSION" ".watchmanconfig"
        "status-go-version.json" "react-native.config.js"
      ];
    };
  };

  buildInputs = [ nodejs openjdk ];
  nativeBuildInputs = [ bash gradle unzip ]
    ++ lib.optionals stdenv.isDarwin [ file gnumake patchedWatchman ];

  # Used by Clojure at compile time to include JS modules
  BUILD_ENV = buildEnv;

  phases = [ "unpackPhase" "patchPhase" "buildPhase" "checkPhase" "installPhase" ];

  unpackPhase = ''
    runHook preUnpack

    cp -r $src ./project
    chmod u+w -R ./project

    export sourceRoot=$PWD/project

    runHook postUnpack
  '';
  postUnpack = assert lib.assertMsg (keystorePath != "") "keystore-file has to be set!"; ''
    mkdir -p ${gradleHome}

    # WARNING: Renaming the keystore will cause 'Keystore was tampered with' error
    cp -a --no-preserve=ownership "${keystorePath}" "${keystoreLocal}"

    # Ensure we have the right .env file
    cp -f $sourceRoot/${envFileName} $sourceRoot/.env

    # Copy index.js and app/ input files
    cp -ra --no-preserve=ownership ${jsbundle}/* $sourceRoot/

    # Copy android/ directory
    cp -a --no-preserve=ownership ${sourceProjectDir}/android/ $sourceRoot/
    chmod u+w $sourceRoot/android
    chmod u+w $sourceRoot/android/app
    mkdir -p $sourceRoot/android/build
    chmod -R u+w $sourceRoot/android/build

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
        "exec gradle -Dmaven.repo.local='${localMavenRepo}' --offline ${gradleOpts}"

    set $prevSet
  '';
  buildPhase = let
    inherit (lib)
      stringLength optionalString substring
      concatStrings concatStringsSep
      catAttrs mapAttrsToList makeLibraryPath;

    # Take the env attribute set and build a couple of scripts
    #  (one to export the environment variables, and another to unset them)
    exportEnvVars = concatStringsSep ";"
      (mapAttrsToList (name: value: "export ${name}='${toString value}'") env);
    adhocEnvVars = optionalString stdenv.isLinux
      "LD_LIBRARY_PATH=$LD_LIBRARY_PATH:${makeLibraryPath [ zlib ]}";
  in 
    assert stringLength env.ANDROID_ABI_SPLIT > 0;
    assert stringLength env.ANDROID_ABI_INCLUDE > 0;
  ''
    export ANDROID_SDK_ROOT="${androidPkgs}"
    export ANDROID_NDK_ROOT="${androidPkgs}/ndk-bundle"

    export KEYSTORE_PATH="${keystoreLocal}"

    export STATUS_REACT_HOME=$PWD
    export HOME=$sourceRoot

    ${exportEnvVars}
    ${optionalString (secretsFile != "") "source ${secretsFile}"}

    ${concatStrings (catAttrs "shellHook" [ mavenAndNpmDeps.shell status-go.shell ])}

    # fix permissions so gradle can create directories
    chmod -R +w $sourceRoot/android

    pushd $sourceRoot/android
    ${adhocEnvVars} ./gradlew -PversionCode=${buildNumber} assemble${gradleBuildType} || exit
    popd > /dev/null
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
