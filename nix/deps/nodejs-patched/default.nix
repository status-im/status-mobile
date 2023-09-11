# This derivation patches Node.js dependencies by
# taking the result of yarn2nix and symlinking what is fine,
# and copying and modifying what needs to be adjusted.

{ stdenv, deps, nodejs, patchMavenSources }:

stdenv.mkDerivation {
  name = "${deps.nodejs.name}-patched";
  phases = [
    "unpackPhase"
    "patchGradlePhase"
    "patchBuildIdPhase"
    "patchKeyChainPhase"
    "patchReactNativeCameraKitPhase"
    "installPhase"
  ];

  # First symlink all modules as is
  # WARNING: Metro has issues when dealing with symlinks!
  unpackPhase = ''
    mkdir -p ./node_modules/
    for module in $(ls ${deps.nodejs}/node_modules); do
      ln -s ${deps.nodejs}/node_modules/$module ./node_modules/
    done
    cp -r ${deps.nodejs}/node_modules/.bin ./node_modules/
  '';

  # Patch build.gradle files in 'react-native-*' dependencies to replace
  # maven and google central repositories with our own local directories.
  # This prevents the builder from downloading Maven artifacts
  patchGradlePhase = ''
    gradleConfigs=$(
      find -L ./node_modules \
        -name build.gradle -or \
        -name build.gradle.kts -or \
        -name settings.gradle -or \
        -name settings.gradle.kts
    )
    for modBuildGradle in $gradleConfigs; do
      relativeToNode=''${modBuildGradle#*node_modules/}
      moduleName=''${relativeToNode%%/*}
      if [[ -L ./node_modules/$moduleName ]]; then
        unlink ./node_modules/$moduleName
        cp -r ${deps.nodejs}/node_modules/$moduleName ./node_modules/
        chmod u+w -R ./node_modules/$moduleName
      fi
      ${patchMavenSources} $modBuildGradle
    done
  '';

  # Do not add a BuildId to the generated libraries, for reproducibility
    patchBuildIdPhase = ''
      substituteInPlace ./node_modules/react-native/ReactAndroid/src/main/jni/CMakeLists.txt --replace \
          '-Wl,--build-id' \
          '-Wl,--build-id=none'
    '';

  installPhase = ''
    mkdir -p $out
    cp -R node_modules $out/
  '';

  # Remove gradle-test-logger-plugin:
  # https://github.com/oblador/react-native-keychain/issues/595
  # TODO: remove this patch when we this library fixes above issue
  patchKeyChainPhase = ''
   sed -i -e '/classpath/d' \
          -e '/apply plugin: "com\.adarshr\.test-logger"/d' \
          ./node_modules/react-native-keychain/android/build.gradle
  '';

  # Fix for :react-native-camera-kit:compileDebugKotlin FAILED
  # Error : CKCamera.kt: (183, 17): 'onScale' overrides nothing
  # fix from : https://github.com/teslamotors/react-native-camera-kit/issues/535#issuecomment-1506229244
  # note by library author : https://github.com/teslamotors/react-native-camera-kit/pull/551#issuecomment-1615305719
  # TODO: remove this patch when we react-native-camera-kit releases a stable v14
  patchReactNativeCameraKitPhase = ''
    substituteInPlace ./node_modules/react-native-camera-kit/android/src/main/java/com/rncamerakit/CKCamera.kt --replace \
      'override fun onScale(detector: ScaleGestureDetector?)' \
      'override fun onScale(detector: ScaleGestureDetector)'
  '';

  # The ELF types are incompatible with the host platform, so let's not even try
  # TODO: Use Android NDK to strip binaries manually
  dontPatchELF = true;
  dontStripHost = true;

  # Take whole sources into consideration when calculating sha
  outputHashMode = "recursive";
  outputHashAlgo = "sha256";
}
