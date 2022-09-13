# This derivation patches Node.js dependencies by
# taking the result of yarn2nix and symlinking what is fine,
# and copying and modifying what needs to be adjusted.

{ stdenv, deps, nodejs, patchutils, patchMavenSources }:

stdenv.mkDerivation {
  name = "${deps.nodejs.name}-patched";

  buildInputs = [ patchutils ];

  # This patch was introduced in this PR (https://github.com/status-im/status-mobile/pull/13885)
  # This bug has been fixed in later versions of `react-native-reanimated` (> 3.0.0), but upgrading
  # is blocked for Status because of other migration issues
  patches = [
    ./react-native-reanimated-2.3.3-flatlist-fix.patch
  ];

  phases = [
    "unpackPhase"
    "patchPhase"
    "patchGradlePhase"
    "patchBuildIdPhase"
    "patchHermesPhase"
    "patchJavaPhase"
    "installPhase"
  ];

  # First symlink all modules as is, copy those that we patch.
  # WARNING: Metro has issues when dealing with symlinks!
  unpackPhase = ''
    toPatch=$(lsdiff --strip 2 $patches)
    mkdir -p ./node_modules/
    for module in $(ls ${deps.nodejs}/node_modules); do
      if [[ $toPatch =~ ^$module/.* ]]; then
        cp -r ${deps.nodejs}/node_modules/$module ./node_modules/
        chmod u+w -R ./node_modules/$module
      else
        ln -s ${deps.nodejs}/node_modules/$module ./node_modules/
      fi
    done
    cp -r ${deps.nodejs}/node_modules/.bin ./node_modules/
  '';

  # Patch build.gradle files in 'react-native-*' dependencies to replace
  # maven and google central repositories with our own local directories.
  # This prevents the builder from downloading Maven artifacts
  patchGradlePhase = ''
    for modBuildGradle in $(find -L ./node_modules -name build.gradle); do
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
