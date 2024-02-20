{ stdenv, fetchurl, unzip, lib }:

let
  inherit (lib) getAttr;
in
stdenv.mkDerivation rec {
  pname = "xcbeautify";
  version = "1.1.1";
  arch = if stdenv.hostPlatform.isAarch64 then "arm64" else "x86_64";

  src = fetchurl {
    url = "https://github.com/tuist/xcbeautify/releases/download/${version}/xcbeautify-${version}-${arch}-apple-macosx.zip";
    sha256 = getAttr arch {
            arm64 = "sha256-VaZBWZNx5iZxjpsVbKQA4wVsigjlhArDCsQXY/RBDx4=";
            x86_64 = "sha256-Q1t4nHQu05mPqNRmL0KQukGRAHdkQHM7H24ar0isQTo=";
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
    homepage = "https://github.com/tuist/xcbeautify";
    license = licenses.mit;
    platforms = platforms.darwin;
  };
}
