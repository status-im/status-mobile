# This is the Android Asset Packaging Tool(AAPT2).
# It is used by Gradle to package Android app resources.
# See: https://developer.android.com/studio/command-line/aapt2

{ lib, stdenv, pkgs, fetchurl }:

let
  inherit (lib) getAttr;
  inherit (stdenv) isLinux isDarwin;

  pname = "aapt2";
  # Warning: This must be the same as gradlePluginVersion android/gradle.properties
  # also referenced in nix/deps/gradle/deps_hack.sh
  version = "8.1.1-10154469";

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
        linux = "sha256-p54GGvEfAo0yk8euVO7QTu/c3zuityZhyGdhFSV6w+E=";
        osx = "sha256-bO4ljdUEfbuns7EyT1FKGLqNGz+0bms5XsplXvzD2T0=";
      };
    };
    sha = fetchurl {
      url = "${repoUrl}/${pkgPath}/${version}/${filenames.jar}.sha1";
      sha256 = getAttr platform {
        linux = "sha256-oUJOQ9j/sJ7Yt0V9WOrRmuqYI+2UWZxeefJ/WyNoWgw=";
        osx = "sha256-w+q2j4MKtIOihPMaJxuIP9IwXvRIyBxgRIJLox4E7ac=";
      };
    };
    pom = fetchurl {
      url = "${repoUrl}/${pkgPath}/${version}/${filenames.pom}";
      sha256 = "sha256-UdUGCoMmEfJxBS2demYKGdG4lzd2DqQbDUCHt/h7Ohg=";
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
