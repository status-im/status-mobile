{ pkgs, stdenv, fetchurl }:

with pkgs;

let
  package = stdenv.mkDerivation rec {
    name = "StatusImAppImage";
    version = "20190515";

    src =
      if stdenv.hostPlatform.system == "x86_64-linux" then
        fetchurl {
          url = "https://desktop-app-files.ams3.digitaloceanspaces.com/${name}_${version}.zip";
          sha256 = "0g7qa97cr0f9807sfd3khxiqz575i9kxi6lfda350ilaw8lnnfv2";
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
