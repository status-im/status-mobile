{ config, stdenv, androidComposition }:

stdenv.mkDerivation rec {
  name = "licensed-android-sdk";
  version = "licensed";
  phases = [ "installPhase" ];
  installPhase = ''
    mkdir -p $out/libexec/android-sdk
    ln -s "${androidComposition.androidsdk}/bin" $out/bin
    for d in ${androidComposition.androidsdk}/libexec/android-sdk/*; do
      ln -s $d $out/$(basename $d)
    done
  '' + stdenv.lib.optionalString config.android_sdk.accept_license ''
    mkdir -p $out/licenses
    echo -e "\n601085b94cd77f0b54ff86406957099ebe79c4d6" > "$out/licenses/android-googletv-license"
    echo -e "\n24333f8a63b6825ea9c5514f83c2829b004d1fee" > "$out/licenses/android-sdk-license"
    echo -e "\n84831b9409646a918e30573bab4c9c91346d8abd" > "$out/licenses/android-sdk-preview-license"
    echo -e "\nd975f751698a77b662f1254ddbeed3901e976f5a" > "$out/licenses/intel-android-extra-license"
    echo -e "\n33b6a2b64607f11b759f320ef9dff4ae5c47d97a" > "$out/licenses/google-gdk-license"
  '';
}