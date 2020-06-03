#
# Generates an ad-hoc and temporary keystore for signing debug/pr builds.
#
# WARNING: Do NOT use this to make a keystore that needs to be secret!
#          Using a derivation will store the inputs in a .drv file.
#
{ stdenv, lib, pkgs }:

let
  inherit (lib) getAttr;

  gradleProps = pkgs.gradlePropParser ../../../android/gradle.properties;

  # Loading defaults from gradle.properties which should be safe.
  KEYSTORE_ALIAS = getAttr "KEYSTORE_ALIAS" gradleProps;
  KEYSTORE_PASSWORD = getAttr "KEYSTORE_PASSWORD" gradleProps;
  KEYSTORE_KEY_PASSWORD = getAttr "KEYSTORE_KEY_PASSWORD" gradleProps;

in stdenv.mkDerivation {
  name = "status-react-android-keystore";

  buildInputs = [ pkgs.openjdk8 ];

  phases = [ "generatePhase" ];
  generatePhase = ''
    keytool -genkey -v \
        -keyalg RSA \
        -keysize 2048 \
        -validity 10000 \
        -deststoretype pkcs12 \
        -dname "CN=, OU=, O=, L=, S=, C=" \
        -keystore "$out" \
        -alias "${KEYSTORE_ALIAS}" \
        -storepass "${KEYSTORE_PASSWORD}" \
        -keypass "${KEYSTORE_KEY_PASSWORD}" \
        >&2
  '';

  shellHook = ''
    export KEYSTORE_ALIAS="${KEYSTORE_ALIAS}"
    export KEYSTORE_PASSWORD="${KEYSTORE_PASSWORD}"
    export KEYSTORE_KEY_PASSWORD="${KEYSTORE_KEY_PASSWORD}"
  '';
}
