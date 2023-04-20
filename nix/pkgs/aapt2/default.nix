# This is the Android Asset Packaging Tool(AAPT2).
# It is used by Gradle to package Android app resources.
# See: https://developer.android.com/studio/command-line/aapt2

{ lib, stdenv, pkgs, fetchurl }:

let
  inherit (lib) getAttr optionals;
  inherit (stdenv) isLinux isDarwin;

  pname = "aapt2";
  # Warning: This must be the same as gradlePluginVersion android/gradle.properties
  version = "4.1.0-6503028";

  pkgPath = "com/android/tools/build/aapt2";
  repoUrl = "https://dl.google.com/dl/android/maven2";

  platform =
    if isLinux then "linux" else
    if isDarwin then "osx" else
    throw "Unknown platform!";

  filenames = {
    jar = "${pname}-${version}-${platform}.jar";
    pom = "${pname}-${version}.pom";
  };

  urls = {
    jar = fetchurl {
      url = "${repoUrl}/${pkgPath}/${version}/${filenames.jar}";
      sha256 = getAttr platform {
        linux = "sha256-oxlBy5aJcb+FgHvy6Qmbi33GsubEuXVbYSHuD4O1wIY=";
        osx = "sha256-zRWrjfV6P6MQaLlwNi7CBhI35U+osNfVvyL0T89NbuI=";
      };
    };
    sha = fetchurl {
      url = "${repoUrl}/${pkgPath}/${version}/${filenames.jar}.sha1";
      sha256 = getAttr platform {
        linux = "sha256-GlvbIDXnrjUga/NkmODJUZX+K5UtU/0fua5a8QY02+E=";
        osx = "sha256-83WpYPsyVk/E9nddy2qUSAh+cBZ1PsS0N9516Tga35o=";
      };
    };
    pom = fetchurl {
      url = "${repoUrl}/${pkgPath}/${version}/${filenames.pom}";
      sha256 = "sha256-p7JdQj7hl/cjiVzT2ZFts1lLI9xlOOnnadXr0vDVhTs=";
    };
  };

in stdenv.mkDerivation {
  inherit pname version;

  srcs = with urls; [ jar sha pom ];
  phases = [ "unpackPhase" ]
    ++ optionals isLinux [ "patchPhase" ]; # OSX binaries don't need patchelf
  buildInputs = with pkgs; [ zip unzip patchelf ];

  unpackPhase = ''
    mkdir -p $out
    for src in $srcs; do
      filename=$(stripHash $src)
      cp $src $out/$filename
    done
  '';

  # On Linux, we need to patch the interpreter in Java packages
  # that contain native executables to use Nix's interpreter instead.
  patchPhase = ''
    # We need an stdenv with a compiler
    [[ -n "$NIX_CC" ]] || exit 1

    # Patch executables from maven dependency to use Nix's interpreter
    tmpDir=$(mktemp -d)
    unzip $out/${filenames.jar} -d $tmpDir
    for exe in `find $tmpDir/ -type f -executable`; do
      patchelf --set-interpreter "$(cat $NIX_CC/nix-support/dynamic-linker)" $exe
    done

    # Rebuild the .jar file with patched binaries
    pushd $tmpDir > /dev/null
    chmod u+w $out/${filenames.jar}
    zip -fr $out/${filenames.jar}
    chmod $out/${filenames.jar} --reference=$out/${filenames.jar}.sha1
    popd > /dev/null
    rm -rf $tmpDir
  '';
}
