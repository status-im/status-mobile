{ pkgs, stdenv, fetchFromGitHub }:

let
  version = "0.8.90";
  rev = "d3c606c55adf8c2c2747556055652b3469f6c4c2"; # This revision will get used in https://github.com/status-im/react-native-keychain/blob/master/desktop/CMakeLists.txt#L45
  sha256 = "1gqw3g0j46aswncm8fgy419lp1fp2y2nild82hs18xra5albvf3i";
  package = stdenv.mkDerivation {
    name = "qtkeychain-patched-source";
    version = "${version}-${stdenv.lib.strings.substring 0 7 rev}";

    src = fetchFromGitHub {
      inherit rev sha256;
      owner = "status-im";
      repo = "qtkeychain";
      name = "qtkeychain-source-${version}";
    };

    phases = [ "unpackPhase" ];
    unpackPhase = ''
      mkdir -p $out/src
      cp -r $src/* $out/src/
      substituteInPlace $out/src/CMakeLists.txt \
        --replace "cmake_minimum_required(VERSION 2.8.11)" "cmake_minimum_required(VERSION 3.12.1)" \
        --replace "project(qtkeychain)" "project(qtkeychain VERSION ${version})" \
        --replace "set(QTKEYCHAIN_VERSION 0.8.90)" "set(QTKEYCHAIN_VERSION ${version})" \
        --replace "{QTKEYCHAIN_VERSION}\" VARIABLE_PREFIX SNORE" "QTKEYCHAIN_VERSION VARIABLE_PREFIX SNORE" \
        --replace "\"\$QTKEYCHAIN_VERSION" qtkeychain
    '';

    meta = with stdenv.lib; {
      description = "Patched sources for qtkeychain, a platform-independent Qt API for storing passwords securely";
      homepage = https://github.com/status-im/qtkeychain;
      license = licenses.bsd3;
      maintainers = [ maintainers.pombeirp ];
      platforms = with platforms; darwin ++ linux;
    };
  };

in package // {
  shellHook = (package.shellHook or "") + ''
    export QTKEYCHAIN_SOURCES="${package}/src"
  '';
}
