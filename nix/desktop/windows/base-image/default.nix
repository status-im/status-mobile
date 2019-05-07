{ pkgs, stdenv, fetchurl }:

with pkgs;

assert stdenv.isLinux;

stdenv.mkDerivation rec {
  name = "StatusIm-Windows-base-image";
  version = "20181113";

  src =
    if stdenv.hostPlatform.system == "x86_64-linux" then
      fetchurl {
        url = "https://desktop-app-files.ams3.digitaloceanspaces.com/${name}_${version}.zip";
        sha256 = "1wrxcss63zlwspmw76k549z72hcycxzd9iw4cdh98l4hs2ayzsk3";
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

  meta = {
    description = "A base image for Windows Status Desktop release distributions";
    homepage = https://desktop-app-files.ams3.digitaloceanspaces.com/;
    license = stdenv.lib.licenses.gpl3;
    maintainers = [ stdenv.lib.maintainers.pombeirp ];
    platforms = stdenv.lib.platforms.linux;
  };
}
