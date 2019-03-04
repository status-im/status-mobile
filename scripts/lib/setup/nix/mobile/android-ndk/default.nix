###
### TODO: This Nix expression is a poor-man's package that just wraps the Android NDK download
### Since the executables' dependencies don't point to /nix/store, it isn't a pure package
### so it won't work e.g. on NixOS
###
{ stdenv, fetchzip }:

let
  ndk-version = "r17c";
  archives = {
    macosx = fetchzip {
      url = "https://dl.google.com/android/repository/android-ndk-${ndk-version}-darwin-x86_64.zip";
      sha256 = "0299m2l8jp773r9y1l9jbyy4xw2mvsz8gqv5b409gwclq8apip1w";
    };
    linux = fetchzip {
      url = "https://dl.google.com/android/repository/android-ndk-${ndk-version}-linux-x86_64.zip";
      sha256 = "02q1hy423syl868jdyaxjm44hn59cmni5019r811vinagvq3m7qi";
    };
  };

in stdenv.mkDerivation rec {
  name = "android-ndk";
  version = ndk-version;

  src = if stdenv.isLinux then archives.linux else archives.macosx;

  phases = [ "unpackPhase" "installPhase" ];
  installPhase = ''
    mkdir $out
    cp -rv . $out
  '';

  meta = {
    description = "The Android NDK is a toolset that lets you implement parts of your app in native code, using languages such as C and C++. For certain types of apps, this can help you reuse code libraries written in those languages.";
    homepage = https://developer.android.com/ndk;
    license = stdenv.lib.licenses.asl20;
    maintainers = [ stdenv.lib.maintainers.pombeirp ];
    platforms = stdenv.lib.platforms.unix;
  };
}
