{ stdenv, fetchurl, unzip }:

assert stdenv.isLinux;

let
  package = stdenv.mkDerivation rec {
    name = "StatusIm-Windows-base-image";
    version = "20190515";

    src =
      if stdenv.hostPlatform.system == "x86_64-linux" then
        fetchurl {
          url = "https://desktop-app-files.ams3.digitaloceanspaces.com/${name}_${version}.zip";
          sha256 = "0wkq0khllms2hnbznb1j8l8yfw6z7phzrdg4ndyik20jkl0faj8f";
        }
      else throw "${name} is not supported on ${stdenv.hostPlatform.system}";

    nativeBuildInputs = [ unzip ];

    phases = [ "unpackPhase" "installPhase" ];
    unpackPhase = ''
      mkdir -p $out/src
      unzip $src -d $out/src
    '';
    installPhase = ''
      runHook preInstall

      echo $out
      ls $out -al

      runHook postInstall
    '';

    meta = with stdenv.lib; {
      description = "A base image for Windows Status Desktop release distributions";
      homepage = https://desktop-app-files.ams3.digitaloceanspaces.com/;
      license = licenses.gpl3;
      maintainers = [ maintainers.pombeirp ];
      platforms = platforms.linux;
    };
  };

in package // {
  shellHook = ''
    export STATUSREACT_WINDOWS_BASEIMAGE_PATH="${package}/src"
  '';
}
