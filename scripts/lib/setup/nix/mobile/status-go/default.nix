{ stdenv, buildGoPackage, pkgs, fetchFromGitHub }:

buildGoPackage rec {
  name = "status-go-${version}";
  version = "0.23.0-beta.8";

  goPackagePath = "github.com/status-im/status-go";
  src = fetchFromGitHub {
    owner = "status-im";
    repo = "status-go";
    rev = "develop";
    sha256 = "0j4g59qdi4vmd27ykm1ga93l2ipf8cj6ym2szapaq39zjjxxygbr";
    #rev = "version";
    #sha256 = "12wal1x67hm3qjq6rqib69pxix4jxjb8bkkgvsfdjhf7z0ylx3px";
  };

  # Fix for usb-related segmentation faults on darwin
  propagatedBuildInputs =
    stdenv.lib.optionals stdenv.isDarwin [ pkgs.libobjc pkgs.IOKit ];

  # Fixes Cgo related build failures (see https://github.com/NixOS/nixpkgs/issues/25959 )
  hardeningDisable = [ "fortify" ];

  # preBuild = ''
  #   export GOPATH=$GOPATH:$NIX_BUILD_TOP/go/src/${goPackagePath}/Godeps/_workspace
  # '';

#  buildPhase = ''
#    make statusgo
#  '';

  meta = {
    description = "The Status module that consumes go-ethereum.";
    homepage = https://github.com/status-im/status-go;
    license = stdenv.lib.licenses.mpl20;
    maintainers = [ stdenv.lib.maintainers.pombeirp ];
    platforms = stdenv.lib.platforms.unix;
  };
}
