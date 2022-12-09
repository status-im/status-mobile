{ stdenv, pkgs, deps, lib
, androidPkgs, patchMavenSources, jsbundle, status-go }:

{
  # Value for BUILD_ENV checked by Clojure code at compile time
  buildEnv ? "prod",
  # Path to the file containing secret environment variables
  secretsFile ? "",
}:

let
  inherit (lib) toLower optionalString stringLength getConfig makeLibraryPath elem;

  # Pass secretsFile for INFURA_TOKEN to jsbundle build
  builtJsBundle = jsbundle { inherit secretsFile; };

  buildType = getConfig "build-type" "release";
  buildNumber = getConfig "build-number" 9999;
  commitHash = getConfig "commit-hash" "unknown";
  gradleOpts = getConfig "android.gradle-opts" null;
  # Used to detect end-to-end builds
  androidAbiInclude = getConfig "android.abi-include" "armeabi-v7a;arm64-v8a;x86";

  envFileName =
    if androidAbiInclude == "x86"                  then ".env.e2e"
    else if (elem buildType ["release" "nightly"]) then ".env.${buildType}"
    else if (elem buildType ["pr" "manual"])       then ".env.jenkins"
    else ".env";

  # There are only two types of Gradle build targets: pr and release
  gradleBuildType = if buildType == "pr" then "Pr" else "Release";

  apksPath = "./android/app/build/outputs/apk/${toLower gradleBuildType}";

  baseName = "${buildType}-android";
in stdenv.mkDerivation rec {
  name = "status-mobile-build-${baseName}";

  src = let path = ./../../..;
  # We use builtins.path so that we can name the resulting derivation
  in builtins.path {
    inherit path;
    name = "status-mobile-source-${baseName}";
    # Keep this filter as restrictive as possible in order to avoid unnecessary rebuilds and limit closure size
    filter = lib.mkFilter {
      root = path;
      include = [
        "package.json" "yarn.lock" "metro.config.js" ".babelrc"
        "resources/.*" "translations/.*" "src/js/worklet_factory.js"
        "modules/react-native-status/android.*" "android/.*"
        envFileName "VERSION" "status-go-version.json" "react-native.config.js"
      ];
    };
  };

  buildInputs = with pkgs; [ nodejs openjdk ];
  nativeBuildInputs = with pkgs; [ bash gradle unzip ]
    ++ lib.optionals stdenv.isDarwin [ file gnumake ];

  # Disable metro watching for file changes. (#13783)
  CI = true;

  # Used by Clojure at compile time to include JS modules
  BUILD_ENV = buildEnv;

  # custom env variables derived from config
  STATUS_GO_SRC_OVERRIDE = getConfig "status-go.src-override" null;
  ANDROID_ABI_SPLIT = getConfig "android.abi-split" "false";
  ANDROID_ABI_INCLUDE = androidAbiInclude;

  # Android SDK/NDK for use by Gradle
  ANDROID_SDK_ROOT = "${androidPkgs.sdk}";
  ANDROID_NDK_ROOT = "${androidPkgs.ndk}";

  # Used by the Android Gradle build script in android/build.gradle
  STATUS_GO_ANDROID_LIBDIR = status-go;

  phases = [
    "unpackPhase" "secretsPhase" "buildPhase" "checkPhase" "installPhase"
  ];

  unpackPhase = ''
    cp -ar $src/. ./
    chmod u+w -R ./
    runHook postUnpack
  '';
  postUnpack = ''
    # Ensure we have the right .env file
    cp -bf ./${envFileName} ./.env

    # Export all vars from .env file
    export $(cut -d= -f1 .env)

    # Copy index.js and app/ input files
    cp -ra --no-preserve=ownership ${builtJsBundle}/* ./

    # Copy android/ directory
    mkdir -p ./android/build
    chmod -R +w ./android

    # Copy node_modules/ directory. The -L is CRUCIAL!
    # Otherwise Metro failes to find modules due to symlinks.
    cp -aL ${deps.nodejs-patched}/node_modules/ ./
    chmod +w -R ./node_modules

    # Patch build.gradle to use local repo
    ${patchMavenSources} ./android/build.gradle
  '';

  # Secrets file is passed to sandbox using extra-sandbox-paths.
  secretsPhase = if (secretsFile != "") then ''
    source "${secretsFile}"
  '' else ''
    echo 'WARNING: No secrets provided!' >&2
  '';

  buildPhase = let
    adhocEnvVars = optionalString stdenv.isLinux
      "LD_LIBRARY_PATH=$LD_LIBRARY_PATH:${makeLibraryPath [ pkgs.zlib ]}";
  in
    assert ANDROID_ABI_SPLIT != null && ANDROID_ABI_SPLIT != "";
    assert stringLength ANDROID_ABI_INCLUDE > 0;
  ''
    # Fixes issue with failing to load libnative-platform.so
    export GRADLE_USER_HOME=$(mktemp -d)
    export ANDROID_SDK_HOME=$(mktemp -d)

    pushd ./android
    ${adhocEnvVars} ${pkgs.gradle}/bin/gradle \
      ${toString gradleOpts} \
      --console=plain \
      --offline --stacktrace \
      -Dorg.gradle.daemon=false \
      -Dmaven.repo.local='${deps.gradle}' \
      -PversionCode=${toString buildNumber} \
      -PcommitHash=${commitHash} \
      assemble${gradleBuildType} \
      || exit 1
    popd > /dev/null
  '';
  doCheck = true;
  checkPhase = ''
    ls ${apksPath}/*.apk \
      | xargs -n1 ${pkgs.unzip}/bin/unzip -qql \
      | grep 'assets/index.android.bundle'
  '';
  installPhase = ''
    mkdir -p $out
    cp ${apksPath}/*.apk $out/
  '';
}
