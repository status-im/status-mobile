{ stdenv, compose }:

#
# This derivation simply symlinks some stuff to get
# shorter paths as libexec/android-sdk is quite the mouthful.
#
stdenv.mkDerivation {
  name = "${compose.androidsdk.name}-mod";
  phases = [ "symlinkPhase" ];
  symlinkPhase = ''
    mkdir -p $out
    ln -s "${compose.androidsdk}/bin" $out/bin
    for d in ${compose.androidsdk}/libexec/android-sdk/*; do
      ln -s $d $out/$(basename $d)
    done
  '';
}
