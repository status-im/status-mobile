#
# This script prepares a finalized version of node_modules,
#   as well as a local version of the Maven repository required by Gradle scripts
#

{ stdenv, lib, callPackage, pkgs, deps, mkShell }:

# fake build to pre-download deps into fixed-output derivation
let
  # Place build target directories in NIX_BUILD_TOP (normally represents /build)
  projectBuildDir = "$NIX_BUILD_TOP/project";
  mavenRepoDir = "$NIX_BUILD_TOP/.m2/repository";
  reactNativeDepsDir = "$NIX_BUILD_TOP/deps"; # Use local writable deps, otherwise (probably due to some interaction between Nix sandboxing and Java) gradle will fail copying directly from the nix store

in stdenv.mkDerivation {
  name = "status-react-patched-npm-gradle-modules";
  src =
    let path = ./../../../..; # Import the root /android and /mobile/js_files folders clean of any build artifacts
    in builtins.path { # We use builtins.path so that we can name the resulting derivation, otherwise the name would be taken from the checkout directory, which is outside of our control
      inherit path;
      name = "status-react-source-gradle-install";
      filter =
        # Keep this filter as restrictive as possible in order to avoid unnecessary rebuilds and limit closure size
        lib.mkFilter {
          root = path;
          include = [
            "android/.*" "resources/.*"
            "mobile/js_files/.*" ".babelrc"
          ];
          exclude = [
            ".*.keystore" "node_modules"
          ];
        };
    };
  phases = [ "unpackPhase" "patchPhase" "installPhase" ];
  nativeBuildInputs = [ deps.nodejs ];
  buildInputs = with pkgs; [ gradle nodejs file zlib deps.gradle ];
  propagatedBuildInputs = [ deps.react-native ];
  unpackPhase = ''
    runHook preUnpack

    # Copy project directory
    mkdir -p ${projectBuildDir}
    cp -a $src/. ${projectBuildDir}
    chmod u+w ${projectBuildDir}
    cd ${projectBuildDir}

    # Copy RN maven dependencies and make them writable, otherwise Gradle copy fails (since the top-level directory is read-only, Java isn't smart enough to copy the child files/folders into that target directory)
    mkdir -p ${mavenRepoDir}
    cp -a ${deps.gradle}/. ${mavenRepoDir}
    cp -a ${deps.react-native}/deps ${reactNativeDepsDir}
    find ${reactNativeDepsDir} -maxdepth 1 -type d -exec chmod -R u+w {} \;

    # Copy node_modules from Nix store
    rm -rf ${projectBuildDir}/node_modules
    mkdir -p ${projectBuildDir}/node_modules
    cp -a ${deps.nodejs}/node_modules/. ${projectBuildDir}/node_modules/

    # Ensure that module was correctly installed
    [ -d ${projectBuildDir}/node_modules/jsc-android/dist ] || exit 1

    # Adjust permissions
    chmod -R u+w ${projectBuildDir}

    # Create a dummy VERSION, since we don't want this expression to be invalidated just because the version changed
    echo '0.0.1' > ${projectBuildDir}/VERSION

    runHook postUnpack
  '';
  patchPhase = ''
    runHook prePatch

    prevSet=$-
    set -e

    patchShebangs ${projectBuildDir}

    function patchMavenSource() {
      set +e

      local targetGradleFile="$1"
      local source="$2"
      local deriv="$3"
      grep "$source" $targetGradleFile > /dev/null && \
        substituteInPlace $targetGradleFile --replace "$source" "$deriv"
    }

    function patchMavenSources() {
      set +e

      local targetGradleFile="$1"
      local deriv="$2"
      patchMavenSource $targetGradleFile 'mavenCentral()' 'mavenLocal()'
      patchMavenSource $targetGradleFile 'google()' 'mavenLocal()'
      patchMavenSource $targetGradleFile 'jcenter()' 'mavenLocal()'
      grep 'https://maven.google.com' $targetGradleFile > /dev/null && \
        substituteInPlace $targetGradleFile --replace 'https://maven.google.com' "$deriv"
      grep 'https://jitpack.io' $targetGradleFile > /dev/null && \
        substituteInPlace $targetGradleFile --replace 'https://jitpack.io' "$deriv"
    }

    # Patch maven and google central repositories with our own local directories. This prevents the builder from downloading Maven artifacts
    patchMavenSources 'android/build.gradle' '${deps.gradle}'
    for f in `find ${projectBuildDir}/node_modules/ -name build.gradle`; do
      patchMavenSources $f '${deps.gradle}'
    done

    # Do not add a BuildId to the generated libraries, for reproducibility
    substituteInPlace ${projectBuildDir}/node_modules/react-native/ReactAndroid/src/main/jni/Application.mk \
      --replace \
        '-Wl,--build-id' \
        '-Wl,--build-id=none'

    # Disable Gradle daemon and caching, since that causes rebuilds (and subsequently errors) anyway due to cache being considered stale
    substituteInPlace ${projectBuildDir}/android/gradle.properties \
      --replace \
        'org.gradle.jvmargs=-Xmx8704M' \
        'org.gradle.jvmargs=-Xmx8704M
    org.gradle.daemon=false
    org.gradle.caching=false'

    # Patch the path to nodejs in project.ext.react
    substituteInPlace ${projectBuildDir}/android/app/build.gradle \
      --replace \
        'nodeExecutableAndArgs: ["node"' \
        'nodeExecutableAndArgs: ["${pkgs.nodejs}/bin/node"'

    # Fix bugs in Hermes usage (https://github.com/facebook/react-native/issues/25601#issuecomment-510856047)
    # - Make PR builds also count as release builds
    # - Fix issue where hermes command is being called with same input/output file
    substituteInPlace ${projectBuildDir}/node_modules/react-native/react.gradle \
      --replace \
        'targetName.toLowerCase().contains("release")' \
        '!targetName.toLowerCase().contains("debug")'

    # Patch dependencies which are not yet ported to AndroidX
    npx jetify

    set $prevSet

    runHook postPatch
  '';
  installPhase = ''
    rm -rf $out
    mkdir -p $out/{project,.m2/repository}

    # TODO: maybe node_modules/react-native/ReactAndroid/build/{tmp,generated} can be discarded?
    cp -R ${mavenRepoDir} $out/.m2/
    cp -R ${projectBuildDir}/{android,node_modules}/ $out/project
  '';

  # The ELF types are incompatible with the host platform, so let's not even try
  # TODO: Use Android NDK to strip binaries manually
  dontPatchELF = true;
  dontStripHost = true;

  # Take whole sources into consideration when calculating sha
  outputHashMode = "recursive";
  outputHashAlgo = "sha256";
}
