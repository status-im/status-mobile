{ lib, stdenv, utils, buildGo117Package
, go, xcodeWrapper
# object with source attributes
, meta, source}:

let
  inherit (stdenv) isDarwin;
  inherit (lib) optional;

in buildGo117Package {
  pname = source.repo;
  version = "${source.cleanVersion}-${source.shortRev}";

  inherit meta;
  inherit (source) src goPackagePath;

  phases = ["unpackPhase" "configurePhase" "buildPhase"];

  preBuild = ''
    pushd go/src/$goPackagePath
    go run cmd/library/*.go > $NIX_BUILD_TOP/main.go
    popd
  '';

  # Build the Go library
  buildPhase = ''
    runHook preBuild

    go build -buildmode=c-archive -o $out/libstatus.a  $NIX_BUILD_TOP/main.go

    runHook postBuild
  '';
}
