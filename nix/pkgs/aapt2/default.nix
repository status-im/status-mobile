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
  version = "3.5.3-5435860";

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
        linux = "05gln93wfj4l5b0zfn6ipkx0i9p0x928ygwkrcfyl58aslxg5gx2";
        osx = "0nzc3hq3fm847a3rvwdz26ln3inh50x44ml0dq486yz45qv9f7cs";
      };
    };
    sha = fetchurl {
      url = "${repoUrl}/${pkgPath}/${version}/${filenames.jar}.sha1";
      sha256 = getAttr platform {
        linux = "0rr7ly0f3w5jw0q985hmxmv8q2nlw1k72n6kl7kcmj4a7i479q90";
        osx = "0k7j54x627jsnl8zdcfj62jj8z0c857yqs3iwyvn29hd02v1b78m";
      };
    };
    pom = fetchurl {
      url = "${repoUrl}/${pkgPath}/${version}/${filenames.pom}";
      sha256 = "1kdjfmrd4h2qljsdlqmyskin0564csg0q8j7bynag17w511bn4d3";
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
