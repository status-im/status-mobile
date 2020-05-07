# This is the Android Asset Packaging Tool(AAPT2).
# It is used by Gradle to package Android app resources.
# See: https://developer.android.com/studio/command-line/aapt2

{ stdenv, deps, pkgs, fetchurl }:

let
  inherit (pkgs) zip unzip;

  pname = "aapt2";
  # Warning: This must be the same as gradlePluginVersion android/gradle.properties
  version = "3.5.3-5435860";

  pkgPath = "com/android/tools/build/aapt2";
  repoUrl = "https://dl.google.com/dl/android/maven2";

  filenames = {
    jar = "${pname}-${version}-linux.jar";
    pom = "${pname}-${version}.pom";
  };

  urls = {
    jar = fetchurl {
      url = "${repoUrl}/${pkgPath}/${version}/${filenames.jar}";
      sha256 = "05gln93wfj4l5b0zfn6ipkx0i9p0x928ygwkrcfyl58aslxg5gx2";
    };
    sha = fetchurl {
      url = "${repoUrl}/${pkgPath}/${version}/${filenames.jar}.sha1";
      sha256 = "0rr7ly0f3w5jw0q985hmxmv8q2nlw1k72n6kl7kcmj4a7i479q90";
    };
    pom = fetchurl {
      url = "${repoUrl}/${pkgPath}/${version}/${filenames.pom}";
      sha256 = "1kdjfmrd4h2qljsdlqmyskin0564csg0q8j7bynag17w511bn4d3";
    };
  };

in stdenv.mkDerivation {
  inherit pname version;

  srcs = with urls; [ jar sha pom ];
  phases = [ "unpackPhase" "patchPhase" ];

  unpackPhase = ''
    mkdir -p $out
    for src in $srcs; do
      local filename=$(basename $src)
      cp $src $out/''${filename#*-}
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
      patchelf --set-interpreter "$(cat $NIX_CC/nix-support/dynamic-linker)" $exe
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
