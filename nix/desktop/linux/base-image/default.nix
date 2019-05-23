{ stdenv, callPackage, fetchurl, nodejs, unzip }:

let
  ubuntu-server = callPackage ../../ubuntu-server { target-os = "linux"; inherit nodejs; };
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

    nativeBuildInputs = [ unzip ubuntu-server ];

    phases = [ "unpackPhase" ];
    unpackPhase = ''
      mkdir -p $out/src
      unzip $src -d $out/src
    '';

    meta = with stdenv.lib; {
      description = "A base image for Linux Status Desktop release distributions";
      homepage = https://desktop-app-files.ams3.digitaloceanspaces.com/;
      license = licenses.gpl3;
      maintainers = [ maintainers.pombeirp ];
      platforms = platforms.linux;
    };
  };

in package // {
  shellHook = ''
    export STATUSREACT_LINUX_BASEIMAGE_PATH="${package}/src"
  '';
}
