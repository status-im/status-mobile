# This is the Android Asset Packaging Tool(AAPT2).
# It is used by Gradle to package Android app resources.
# See: https://developer.android.com/studio/command-line/aapt2

{ lib, stdenv, pkgs, fetchurl }:

let
  inherit (lib) getAttr;
  inherit (stdenv) isLinux isDarwin;

  pname = "aapt2";
  # Warning: This must be the same as gradlePluginVersion android/gradle.properties
  version = "7.2.2-7984345";

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
        linux = "sha256-BKtK5f7lxp0YdQU4AFGL933vjEAykQ1RiKpDwfc32oI=";
        osx = "sha256-OORgWnGQS0GwqZrSg8sFQb4FM23hrYHwmarcYd8EO/0=";
      };
    };
    sha = fetchurl {
      url = "${repoUrl}/${pkgPath}/${version}/${filenames.jar}.sha1";
      sha256 = getAttr platform {
        linux = "sha256-4F4REWQLj8hdnh1tIwRVgdWJjUBhvvINP56nIscwePA=";
        osx = "sha256-n0jFUphzlFnNacolbXoNmNHs/hXfktVpYRlJ1JW9ukU=";
      };
    };
    pom = fetchurl {
      url = "${repoUrl}/${pkgPath}/${version}/${filenames.pom}";
      sha256 = "sha256-XWEn9Zvxv0hgYU2yUSlZ4OiguZy1bz+Gh0wRrm4d9GQ=";
    };
  };

in stdenv.mkDerivation {
  inherit pname version;

  srcs = with urls; [ jar sha pom ];
  # patchelf is Linux specific and won't work on Darwin Platform
  phases = if isDarwin then ["unpackPhase"] else ["unpackPhase" "patchPhase" "installPhase"];
  buildInputs = with pkgs; [ zip unzip patchelf ];

  unpackPhase = ''
    mkdir -p $out
    for src in $srcs; do
      filename=$(stripHash $src)
      cp $src $out/$filename
    done
    tmpDir=$(mktemp -d)
    unzip $out/${filenames.jar} -d $tmpDir
  '';

  # On Linux, we need to patch the interpreter in Java packages
  # that contain native executables to use Nix's interpreter instead.
  patchPhase = ''
    # Patch executables from maven dependency to use Nix's interpreter
    patchelf --set-interpreter "$(cat $NIX_CC/nix-support/dynamic-linker)" $tmpDir/aapt2
  '';

  # Rebuild the .jar file with patched binaries
  installPhase = ''
    pushd $tmpDir > /dev/null
    chmod u+w $out/${filenames.jar}
    zip -fr $out/${filenames.jar}
    chmod $out/${filenames.jar} --reference=$out/${filenames.jar}.sha1
    popd > /dev/null
  '';
}
