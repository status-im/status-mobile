{ stdenv, lib, config, callPackage, deps,
  bash, file, gnumake, watchmanFactory, gradle,
  androidPkgs, patchMavenSources, nodeJsModules,
  nodejs, openjdk, jsbundle, status-go, unzip, zlib }:

{
  buildEnv ? "prod", # Value for BUILD_ENV checked by Clojure code at compile time
  secretsFile ? "", # Path to the file containing secret environment variables
  watchmanSockPath ? "", # Path to the socket file exposed by an external watchman instance (workaround needed for building Android on macOS)
}:

assert (lib.stringLength watchmanSockPath) > 0 -> stdenv.isDarwin;

let
  inherit (lib) toLower optionalString stringLength getConfig makeLibraryPath;

  buildType = getConfig "build-type" "prod";
  buildNumber = getConfig "build-number" 9999;
  gradleOpts = getConfig "android.gradle-opts" null;
  keystorePath = getConfig "android.keystore-path" null;

  baseName = "release-android";
  name = "status-react-build-${baseName}";

  envFileName = if (buildType == "release" || buildType == "nightly" || buildType == "e2e")
    then ".env.${buildType}"
    else if buildType != "" then ".env.jenkins"
    else ".env";

  # There are only two types of Gradle builds: pr and release
  gradleBuildType = if (buildType == "pr" || buildType == "e2e")
    then "Pr"
    else "Release"; # PR builds shouldn't replace normal releases

  apksPath = "./android/app/build/outputs/apk/${toLower gradleBuildType}";
  patchedWatchman = watchmanFactory watchmanSockPath;

in stdenv.mkDerivation rec {
  inherit name;
  src = let path = ./../../..;
  # We use builtins.path so that we can name the resulting derivation
  in builtins.path {
    inherit path;
    name = "status-react-source-${baseName}";
    # Keep this filter as restrictive as possible in order to avoid unnecessary rebuilds and limit closure size
    filter = lib.mkFilter {
      root = path;
      include = [
        "mobile/js_files.*" "resources/.*" "translations/.*"
        "modules/react-native-status/android.*" "android/.*"
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

  # custom env variables derived from config
  STATUS_GO_SRC_OVERRIDE = getConfig "nimbus.src-override" null;
  ANDROID_ABI_SPLIT = getConfig "android.abi-split" "false";
  ANDROID_ABI_INCLUDE = getConfig "android.abi-include" "armeabi-v7a;arm64-v8a;x86";

  # Android SDK/NDK for use by Gradle
  ANDROID_SDK_ROOT = "${androidPkgs}";
  ANDROID_NDK_ROOT = "${androidPkgs}/ndk-bundle";

  # Used by the Android Gradle build script in android/build.gradle
  STATUS_GO_ANDROID_LIBDIR = "${status-go}";

  phases = [
    "unpackPhase" "secretPhase" "buildPhase" "checkPhase" "installPhase"
  ];

  unpackPhase = ''
    cp -ar $src/. ./
    chmod u+w -R ./
    runHook postUnpack
  '';
  postUnpack = assert lib.assertMsg (keystorePath != null) "keystore-file has to be set!"; ''
    # Keep the same keystore path for determinism
    export KEYSTORE_PATH="$PWD/status-im.keystore"
    cp -a --no-preserve=ownership "${keystorePath}" "$KEYSTORE_PATH"

    # Ensure we have the right .env file
    cp -f ./${envFileName} ./.env

    # create mobile node/yarn symlinks
    ln -sf ./mobile/js_files/* ./

    # Copy index.js and app/ input files
    cp -ra --no-preserve=ownership ${jsbundle}/* ./

    # Copy android/ directory
    mkdir -p ./android/build
    chmod -R +w ./android

    # Copy node_modules/ directory. The -L is CRUCIAL!
    # Otherwise Metro failes to find modules due to symlinks.
    cp -aL ${nodeJsModules}/node_modules/ ./
    chmod +w -R ./node_modules

    # Patch build.gradle to use local repo
    ${patchMavenSources} ./android/build.gradle ${deps.gradle}

    # Patch dependencies which are not yet ported to AndroidX
    ${nodejs}/bin/node ./node_modules/jetifier/bin/jetify
  '';
  secretPhase = optionalString (secretsFile != "") ''
    source "${secretsFile}"
  '';
  buildPhase = let
    adhocEnvVars = optionalString stdenv.isLinux
      "LD_LIBRARY_PATH=$LD_LIBRARY_PATH:${makeLibraryPath [ zlib ]}";
  in 
    assert ANDROID_ABI_SPLIT != null && ANDROID_ABI_SPLIT != "";
    assert stringLength ANDROID_ABI_INCLUDE > 0;
  ''
    pushd ./android
    ${adhocEnvVars} gradle ${toString gradleOpts} \
      --offline --stacktrace \
      -Dmaven.repo.local='${deps.gradle}' \
      -PversionCode=${toString buildNumber} \
      assemble${gradleBuildType} \
      || exit 1
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
