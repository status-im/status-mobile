{ stdenv, fetchurl, lib }:

stdenv.mkDerivation rec {
  pname = "idb-companion";
  version = "1.1.8";

  src = fetchurl {
    url = "https://github.com/facebook/idb/releases/download/v${version}/idb-companion.universal.tar.gz";
    sha256 = "sha256-O3LMappbGiKhiCBahAkNOilDR6hGGA79dVzxo8hI4+c=";
  };

  buildInputs = [ ];

  unpackPhase = ''
    tar -xzf $src
  '';

  installPhase = ''
    mkdir -p $out/bin
    cp -r ./* $out/bin/
  '';

  meta = with lib; {
    description = "A powerful command line tool for automating iOS simulators and devices";
    homepage = "https://github.com/facebook/idb";
    license = licenses.mit;
    platforms = platforms.darwin;
  };
}
