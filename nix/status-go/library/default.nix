{ buildGoPackage
# object with source attributes
, meta, source}:

buildGoPackage {
  pname = source.repo;
  version = "${source.cleanVersion}-${source.shortRev}";

  inherit meta;
  inherit (source) src goPackagePath;

  phases = ["unpackPhase" "configurePhase" "buildPhase"];

  # https://pkg.go.dev/net#hdr-Name_Resolution
  # https://github.com/status-im/status-mobile/issues/19736
  # https://github.com/status-im/status-mobile/issues/19581
  # TODO: try removing when go is upgraded to 1.22
  GODEBUG = "netdns=cgo+2";

  preBuild = ''
    pushd go/src/$goPackagePath
    go run cmd/library/*.go > $NIX_BUILD_TOP/main.go
    popd
  '';

  # Build the Go library
  buildPhase = ''
    runHook preBuild
    go build \
      -buildmode='c-archive' \
      -tags='gowaku_skip_migrations gowaku_no_rln' \
      -o "$out/libstatus.a" \
      $NIX_BUILD_TOP/main.go
    runHook postBuild
  '';
}
