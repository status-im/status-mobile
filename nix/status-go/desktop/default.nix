{ lib, stdenv, utils, go, buildGoPackage
# object with source attributes
, meta , source
, goBuildFlags
, goBuildLdFlags
, outputFileName ? "libstatus.a" }:

let
  inherit (lib) concatStringsSep optionalString concatMapStrings;

  removeReferences = [ go ];
  removeExpr = refs: ''remove-references-to ${concatMapStrings (ref: " -t ${ref}") refs}'';

  hostSystem = stdenv.hostPlatform.system;

in buildGoPackage {
  pname = source.repo;
  version = "${source.cleanVersion}-${source.shortRev}";

  inherit meta;
  inherit (source) src goPackagePath;

  # Fixes Cgo related build failures (see https://github.com/NixOS/nixpkgs/issues/25959 )
  hardeningDisable = [ "fortify" ];

  # Ensure XCode is present, instead of failing at the end of the build
  preConfigure = optionalString stdenv.isDarwin utils.enforceXCodeAvailable;

  buildMessage = "Building desktop library";

  #GOOS=windows GOARCH=amd64 CGO_ENABLED=1 go build ${goBuildFlags} -buildmode=c-archive -o $out/${outputFileName} ./lib
  buildPhase = let
    CGO_LDFLAGS = concatStringsSep " " goBuildLdFlags;
  in ''
    pushd "$NIX_BUILD_TOP/go/src/${source.goPackagePath}" >/dev/null

    export GO111MODULE=off

    go build -o $out/${outputFileName} \
        ${concatStringsSep " " goBuildFlags} \
        -buildmode=c-archive \
        -ldflags='${CGO_LDFLAGS}' \
        ./lib

    popd >/dev/null
  '';

  # replace hardcoded paths to go package in /nix/store, otherwise Nix will fail the build
  fixupPhase = ''
    find $out -type f -exec ${removeExpr removeReferences} '{}' + || true
  '';

  installPhase = ''
    mkdir -p $out/lib/${hostSystem} $out/include
    mv $out/${outputFileName} $out/lib/${hostSystem}
    mv $out/libstatus.h $out/include
  '';

  outputs = [ "out" ];
}
