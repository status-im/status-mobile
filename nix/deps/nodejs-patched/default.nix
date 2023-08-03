# This derivation patches Node.js dependencies by
# taking the result of yarn2nix and symlinking what is fine,
# and copying and modifying what needs to be adjusted.

{ stdenv, deps, nodejs, patchMavenSources }:

stdenv.mkDerivation {
  name = "${deps.nodejs.name}-patched";
  phases = [
    "unpackPhase"
    "patchGradlePhase"
    "patchReactNativePhase"
    "patchPodPhase"
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

  installPhase = ''
    mkdir -p $out
    cp -R node_modules $out/
  '';

#  Fix glog configuration issue in react-native:
#  https://github.com/facebook/react-native/issues/33966
#  TODO: remove this patch when we reach react-native 0.71.4
  patchReactNativePhase = ''
    substituteInPlace ./node_modules/react-native/scripts/ios-configure-glog.sh --replace \
      'sed -i' \
      'sed -i.bak -e'
    substituteInPlace ./node_modules/react-native/scripts/ios-configure-glog.sh --replace \
       'src/glog/logging.h.in' \
       'src/glog/logging.h.in && rm src/glog/logging.h.in.bak'
     substituteInPlace ./node_modules/react-native/scripts/ios-configure-glog.sh --replace \
        'src/config.h.in' \
        'src/config.h.in && rm src/config.h.in.bak'
  '';

#  Fix pod issue in react-native 0.67.5:
#  https://stackoverflow.com/questions/71248072/no-member-named-cancelbuttontintcolor-in-jsnativeactionsheetmanagerspecsh
#  TODO: remove this patch when maybe after 0.68.5
  patchPodPhase = ''
    substituteInPlace ./node_modules/react-native/React/CoreModules/RCTActionSheetManager.mm --replace \
          '[RCTConvert UIColor:options.cancelButtonTintColor() ? @(*options.cancelButtonTintColor()) : nil];' \
          '[RCTConvert UIColor:options.tintColor() ? @(*options.tintColor()) : nil];'
  '';

  # The ELF types are incompatible with the host platform, so let's not even try
  # TODO: Use Android NDK to strip binaries manually
  dontPatchELF = true;
  dontStripHost = true;

  # Take whole sources into consideration when calculating sha
  outputHashMode = "recursive";
  outputHashAlgo = "sha256";
}
