# This method patches Node.js dependencies by taking the
# result of yarn2nix and symlinking what is fine, and 
# copying and modifying what needs to be adjusted.

{ stdenv, lib, pkgs, patchMavenSources, coreutils }:

nodePkgs: mavenPkgs:

stdenv.mkDerivation {
  name = "${nodePkgs.name}-patched";

  phases = [ "unpackPhase" "patchPhase" "installPhase" ];

  # First symlink all modules as is
  # WARNING: Metro has issues when dealing with symlinks!
  unpackPhase = ''
    mkdir -p ./node_modules/
    for module in $(ls ${nodePkgs}/node_modules); do
      ln -s ${nodePkgs}/node_modules/$module ./node_modules/
    done
    cp -r ${nodePkgs}/node_modules/.bin ./node_modules/
  '';

  # Then patch the modules that have build.gradle files
  patchPhase = ''
    # Patch maven and google central repositories with our own local directories.
    # This prevents the builder from downloading Maven artifacts
    for modBuildGradle in $(find -L ./node_modules -name build.gradle); do
      relativeToNode=''${modBuildGradle#*node_modules/}
      moduleName=''${relativeToNode%%/*}
      if [[ -L ./node_modules/$moduleName ]]; then
        unlink ./node_modules/$moduleName
        cp -r ${nodePkgs}/node_modules/$moduleName ./node_modules/
      fi
      chmod +w -R ./node_modules/$relativeToNode
      ${patchMavenSources} $modBuildGradle '${mavenPkgs}'
    done

    patchShebangs ./node_modules

    # Do not add a BuildId to the generated libraries, for reproducibility
    substituteInPlace ./node_modules/react-native/ReactAndroid/src/main/jni/Application.mk --replace \
        '-Wl,--build-id' \
        '-Wl,--build-id=none'

    # Fix bugs in Hermes usage:
    # https://github.com/facebook/react-native/issues/25601#issuecomment-510856047
    # - Make PR builds also count as release builds
    # - Fix issue where hermes command is being called with same input/output file
    substituteInPlace ./node_modules/react-native/react.gradle --replace \
        'targetName.toLowerCase().contains("release")' \
        '!targetName.toLowerCase().contains("debug")'
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
