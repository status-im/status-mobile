{ pkgs, stdenv, fetchurl }:

with pkgs;

let
  package = stdenv.mkDerivation rec {
    name = "StatusImAppImage";
    version = "20181208";

    src =
      if stdenv.hostPlatform.system == "x86_64-linux" then
        fetchurl {
          url = "https://desktop-app-files.ams3.digitaloceanspaces.com/${name}_${version}.zip";
          sha256 = "15c6p5v6325kj2whc298dn1dyigi0yzk2nzh1y10d03aqr4j8mp5";
        }
      else throw "${name} is not supported on ${stdenv.hostPlatform.system}";

    nativeBuildInputs = [ unzip ];

    phases = [ "unpackPhase" ];
    unpackPhase = ''
      mkdir -p $out/src
      unzip $src -d $out/src
    '';

    meta = {
      description = "A base image for Linux Status Desktop release distributions";
      homepage = https://desktop-app-files.ams3.digitaloceanspaces.com/;
      license = stdenv.lib.licenses.gpl3;
      maintainers = [ stdenv.lib.maintainers.pombeirp ];
      platforms = stdenv.lib.platforms.linux;
    };
  };

in package // {
  shellHook = ''
    export STATUSREACT_LINUX_BASEIMAGE_PATH="${package}/src"
  '';
}