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
    "patchHermesPhase"
    "patchJavaPhase"
    "patchYogaNodePackagePhase"
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
    substituteInPlace ./node_modules/react-native/ReactAndroid/src/main/jni/Application.mk --replace \
        '-Wl,--build-id' \
        '-Wl,--build-id=none'
  '';
  # Fix bugs in Hermes usage:
  # https://github.com/facebook/react-native/issues/25601#issuecomment-510856047
  # - Make PR builds also count as release builds
  # - Fix issue where hermes command is being called with same input/output file
  patchHermesPhase = ''
    substituteInPlace ./node_modules/react-native/react.gradle --replace \
        'targetName.toLowerCase().contains("release")' \
        '!targetName.toLowerCase().contains("debug")'
  '';
  # Patch Java files in modules which are not yet ported to AndroidX
  patchJavaPhase = ''
    ${nodejs}/bin/node ./node_modules/jetifier/bin/jetify
  '';
  # Patch React Native Yoga.cpp file
  # FIXME: Remove this once release newer than 1.19.0 is used which includes:
  # https://github.com/facebook/yoga/commit/f174de70
  patchYogaNodePackagePhase = ''
    substituteInPlace ./node_modules/react-native/ReactCommon/yoga/yoga/Yoga.cpp --replace \
        'node->getLayout().hadOverflow() |' \
        'node->getLayout().hadOverflow() ||'
  '';
  installPhase = ''
    mkdir -p $out
    cp -R node_modules $out/
  '';

  # The ELF types are incompatible with the host platform, so let's not even try
  # TODO: Use Android NDK to strip binaries manually
  dontPatchELF = true;
  dontStripHost = true;

  # Take whole sources into consideration when calculating sha
  outputHashMode = "recursive";
  outputHashAlgo = "sha256";
}
