{ pkgs, stdenv, fetchFromGitHub }:

let
  version = "0.7.1";
  rev = "9d54904e4896ab6c3c3a52f97381e5948b455970"; # This revision will get used in modules/react-native-desktop-notification/desktop/CMakeLists.txt#L71
  sha256 = "0ix1qgx877nw9mlbbqsgkis4phkkf4ax2ambziy2w48hg6ai0x4d";
  package = stdenv.mkDerivation {
    name = "snorenotify-patched-source";
    version = "${version}-${stdenv.lib.strings.substring 0 7 rev}";

    src = fetchFromGitHub {
      inherit rev sha256;
      owner = "status-im";
      repo = "snorenotify";
      name = "snorenotify-source-${version}";
    };

    phases = [ "unpackPhase" ];
    unpackPhase =
      let
        inherit (stdenv.lib) versions;
      in ''
      mkdir -p $out/src
      cp -r $src/* $out/src/
      substituteInPlace $out/src/CMakeLists.txt \
          --replace "cmake_minimum_required( VERSION 2.8.12 )" "" \
          --replace "project( SnoreNotify )" "cmake_minimum_required( VERSION 3.12.1 )
project( SnoreNotify VERSION ${version} )" \
          --replace "set(SNORE_VERSION_MAJOR 0)" "set(SNORE_VERSION_MAJOR ${versions.major version} )" \
          --replace "set(SNORE_VERSION_MINOR 7)" "set(SNORE_VERSION_MINOR ${versions.minor version} )" \
          --replace "set(SNORE_VERSION_PATCH 1)" "set(SNORE_VERSION_PATCH ${versions.patch version} )"
      substituteInPlace $out/src/src/libsnore/CMakeLists.txt \
        --replace "{SNORE_VERSION_MAJOR}" "SNORE_VERSION_MAJOR" \
        --replace "{SNORE_VERSION_MINOR}" "SNORE_VERSION_MINOR" \
        --replace "{SNORE_VERSION_PATCH}" "SNORE_VERSION_PATCH" \
        --replace "ecm_setup_version(\"\$SNORE_VERSION_MAJOR.\$SNORE_VERSION_MINOR.\$SNORE_VERSION_PATCH\"" "ecm_setup_version(SnoreNotify"
    '';

    meta = {
      description = "Patched sources for Snorenotify, a multi platform Qt notification framework. Using a plugin system it is possible to create notifications with many different notification systems on Windows, Mac OS and Unix and mobile Devices";
      homepage = https://github.com/status-im/snorenotify;
      license = stdenv.lib.licenses.lgpl3;
      maintainers = with stdenv.lib.maintainers; [ pombeirp ];
      platforms = with stdenv.lib.platforms; darwin ++ linux;
    };
  };

in package // {
  shellHook = (package.shellHook or "") + ''
    export SNORENOTIFY_SOURCES="${package}/src"
  '';
}
