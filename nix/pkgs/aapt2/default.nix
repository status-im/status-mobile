# This is the Android Asset Packaging Tool(AAPT2).
# It is used by Gradle to package Android app resources.
# See: https://developer.android.com/studio/command-line/aapt2

{ lib, stdenv, deps, pkgs, fetchurl }:

let
  inherit (lib) getAttr optionals;
  inherit (pkgs) zip unzip patchelf;
  inherit (stdenv) isLinux isDarwin;

  pname = "aapt2";
  # Warning: This must be the same as gradlePluginVersion android/gradle.properties
  version = "4.2.2-7147631";

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
        linux = "0sh2rrahlfrqsmhgppvrb0s8wb76nab4cldsb0lsbw4q819x8ddq";
        osx = "0hhvxyr0jxci01qqkcrn80h4qs3b43lqxpbss0xvrkd1nwdilmnb";
      };
    };
    sha = fetchurl {
      url = "${repoUrl}/${pkgPath}/${version}/${filenames.jar}.sha1";
      sha256 = getAttr platform {
        linux = "02w5pn0c551c1rng5ak4jx339b8631n0q26biyygq1f53hnvv110";
        osx = "156jbw4xm43902c0cqrag9l1s3fg6i1irsdz8m8659i9530ywwpx";
      };
    };
    pom = fetchurl {
      url = "${repoUrl}/${pkgPath}/${version}/${filenames.pom}";
      sha256 = "1clls4l2agfiff7pbrjqm8ajgdbwgi5jynimpj2avyxyxy6ci0sx";
    };
  };

in stdenv.mkDerivation {
  inherit pname version;

  srcs = with urls; [ jar sha pom ];
  phases = [ "unpackPhase" ]
    ++ optionals isLinux [ "patchPhase" ]; # OSX binaries don't need patchelf
  buildInputs = [ zip unzip patchelf ];

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
    ${unzip}/bin/unzip $out/${filenames.jar} -d $tmpDir
    for exe in `find $tmpDir/ -type f -executable`; do
      ${patchelf}/bin/patchelf --set-interpreter "$(cat $NIX_CC/nix-support/dynamic-linker)" $exe
    done

    # Rebuild the .jar file with patched binaries
    pushd $tmpDir > /dev/null
    chmod u+w $out/${filenames.jar}
    ${zip}/bin/zip -fr $out/${filenames.jar}
    chmod $out/${filenames.jar} --reference=$out/${filenames.jar}.sha1
    popd > /dev/null
    rm -rf $tmpDir
  '';
}
