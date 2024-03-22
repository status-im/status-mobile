{ stdenv, fetchurl, unzip, lib }:

let
  inherit (lib) getAttr;
in
stdenv.mkDerivation rec {
  pname = "xcbeautify";
  version = "1.4.0";
  arch = if stdenv.hostPlatform.isAarch64 then "arm64" else "x86_64";


  src = fetchurl {
    url = "https://github.com/cpisciotta/xcbeautify/releases/download/${version}/xcbeautify-${version}-${arch}-apple-macosx.zip";
    sha256 = getAttr arch {
            arm64 = "sha256-4b4mXT5IfNOS8iOrZASDhTrmOehG4mePcoiKxR+IdZk=";
            x86_64 = "sha256-adEfAK7n3Q/Yd1deyJx7htX7hZaGDztEeBv4z2A0wzg=";
        };
    };

  buildInputs = [ unzip ];

  unpackPhase = ''
    unzip $src
  '';

  installPhase = ''
    install -D xcbeautify $out/bin/xcbeautify
  '';

  meta = with lib; {
    description = "A little beautifier tool for xcodebuild";
    homepage = "https://github.com/cpisciotta/xcbeautify";
    license = licenses.mit;
    platforms = platforms.darwin;
  };
}
