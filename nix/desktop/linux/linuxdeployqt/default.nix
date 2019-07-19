{ pkgs, stdenv, fetchFromGitHub, appimagekit }:

with pkgs;

stdenv.mkDerivation rec {
  pname = "linuxdeployqt";
  version = "20181215";
  owner = "probonopd";
  repo = "linuxdeployqt";
  rev = "600fc20ea73ee937a402a2bb6b3663d93fcc1d4b";
  sha256 = "05kvkfbhsyadlcggl63rhrw5s36d8qxs8gyihrjn2cjk42xx8r7j";

  src =
    if stdenv.hostPlatform.system == "x86_64-linux" then
      fetchFromGitHub {
        name = "${repo}-${stdenv.lib.strings.substring 0 7 rev}-source";
        inherit owner repo rev sha256;
      }
    else throw "${name} is not supported on ${stdenv.hostPlatform.system}";

  # Add our own patch to make linuxdeployqt correctly include all /nix/store rpaths to LD_LIBRARY_PATH so we don't have to calculate that ourselves
  patches = [ ./linuxdeployqt.patch ];

  buildInputs = [ qt5.qtbase appimagekit ];
  nativeBuildInputs = [ wget ];

  buildPhase = ''
    qmake
    make
  '';

  installPhase = ''
    runHook preInstall

    mkdir -p $out/bin
    cp -r bin/linuxdeployqt $out/bin/

    runHook postInstall
  '';

  meta = {
    description = "Makes Linux applications self-contained by copying in the libraries and plugins that the application uses, and optionally generates an AppImage. Can be used for Qt and other applications";
    homepage = https://github.com/probonopd/linuxdeployqt/;
    license = stdenv.lib.licenses.gpl3;
    maintainers = [ stdenv.lib.maintainers.pombeirp ];
    platforms = stdenv.lib.platforms.linux;
  };
}
