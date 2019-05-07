{ pkgs, stdenv, fetchurl }:

with pkgs;

let
    package = stdenv.mkDerivation rec {
    name = "StatusImAppBundle";
    version = "20181113";

    src =
      if stdenv.hostPlatform.system == "x86_64-darwin" then
        fetchurl {
          url = "https://desktop-app-files.ams3.digitaloceanspaces.com/Status_${version}.app.zip";
          sha256 = "0n8n6p60dwsr4q5v4vq8fffcy5qmqhp03yy95k66q4yic72r0hhz";
        }
      else throw "${name} is not supported on ${stdenv.hostPlatform.system}";

    nativeBuildInputs = [ unzip ];

    phases = [ "unpackPhase" ];
    unpackPhase = ''
      mkdir -p $out/src
      unzip $src -d $out/src
    '';

    meta = {
      description = "A base image for macOS Status Desktop release distributions";
      homepage = https://desktop-app-files.ams3.digitaloceanspaces.com/;
      license = stdenv.lib.licenses.gpl3;
      maintainers = [ stdenv.lib.maintainers.pombeirp ];
      platforms = stdenv.lib.platforms.darwin;
    };
  };

in package // {
  shellHook = ''
    export STATUSREACT_MACOS_BASEIMAGE_PATH="${package}/src"
  '';
}