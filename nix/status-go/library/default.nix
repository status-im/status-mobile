{ lib, stdenv, meta, source, buildGoPackage,
  go-bindata, mockgen, protoc-gen-go, protobuf3_20 }:

buildGoPackage {
  pname = source.repo;
  version = "${source.cleanVersion}-${source.shortRev}";

  inherit meta;
  inherit (source) src goPackagePath;

  nativeBuildInputs = [
    go-bindata mockgen protoc-gen-go protobuf3_20
  ];

  phases = ["unpackPhase" "configurePhase" "buildPhase"];

  # https://pkg.go.dev/net#hdr-Name_Resolution
  # https://github.com/status-im/status-mobile/issues/19736
  # https://github.com/status-im/status-mobile/issues/19581
  # TODO: try removing when go is upgraded to 1.22
  GODEBUG = "netdns=cgo+2";

  # Since go 1.21 status-go compiled library includes references to cgo runtime.
  # FIXME: Remove this when go 1.23 or later versions fix this madness.
  allowGoReference = true;

  preBuild = ''
    pushd go/src/$goPackagePath
    go run cmd/library/*.go > $NIX_BUILD_TOP/main.go
    make generate SHELL=$SHELL GO111MODULE=on GO_GENERATE_CMD='go generate'
    popd
  '';

  # Build the Go library
  # ld flags and netgo tag are necessary for integration tests to work on MacOS
  # https://github.com/status-im/status-mobile/issues/20135
  buildPhase = ''
    runHook preBuild
    go build \
      -buildmode='c-archive' \
      ${lib.optionalString stdenv.isDarwin "-ldflags=-extldflags=-lresolv"} \
      -tags='gowaku_skip_migrations gowaku_no_rln ${lib.optionalString stdenv.isDarwin "netgo"}' \
      -o "$out/libstatus.a" \
      $NIX_BUILD_TOP/main.go
    runHook postBuild
  '';
}
