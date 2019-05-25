{ stdenv, fetchurl, unzip }:

let
  package = stdenv.mkDerivation rec {
    name = "StatusImAppBundle";
    version = "20190515";

    src =
      if stdenv.hostPlatform.system == "x86_64-darwin" then
        fetchurl {
          url = "https://desktop-app-files.ams3.digitaloceanspaces.com/Status_${version}.app.zip";
          sha256 = "1255jgdp0apqh7qfp752nww91iq39x5mm7rf0wazq2vjahfr4pc5";
        }
      else throw "${name} is not supported on ${stdenv.hostPlatform.system}";

    nativeBuildInputs = [ unzip ];

    phases = [ "unpackPhase" ];
    unpackPhase = ''
      mkdir -p $out/src
      unzip $src -d $out/src
    '';

    meta = with stdenv.lib; {
      description = "A base image for macOS Status Desktop release distributions";
      homepage = https://desktop-app-files.ams3.digitaloceanspaces.com/;
      license = licenses.gpl3;
      maintainers = [ maintainers.pombeirp ];
      platforms = platforms.darwin;
    };
  };

in package // {
  shellHook = ''
    export STATUSREACT_MACOS_BASEIMAGE_PATH="${package}/src"
  '';
}
