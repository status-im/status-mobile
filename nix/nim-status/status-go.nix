{ lib, buildGo114Package
# Dependencies
, go, androidPkgs
, callPackage
# build parameters
, platform ? "android"
, arch ? "386"
, api ? "23" }:

let
  inherit (lib) attrValues mapAttrs concatStringsSep concatMapStrings;

  # Source can be changed with a local override from config
  source = callPackage ./status-go-source.nix { };

  flags = callPackage ./getFlags.nix {inherit platform arch;};

  removeReferences = [ go ];
  removeExpr = refs: ''remove-references-to ${concatMapStrings (ref: " -t ${ref}") refs}'';

  # Params to be set at build time, important for About section and metrics
  goBuildParams = {
    GitCommit = source.rev;
    Version = source.cleanVersion;
  };

  # These are necessary for status-go to show correct version
  paramsLdFlags = attrValues (mapAttrs (name: value:
    "-X github.com/status-im/status-go/params.${name}=${value}"
  ) goBuildParams);

  goBuildLdFlags = concatStringsSep " " (paramsLdFlags ++ [
      "-s" # -s disabled symbol table
      "-w" # -w disables DWARF debugging information
    ]);

  # Shorthands for the built phase

  isAndroid = lib.hasPrefix "android" platform;
  isIOS = platform == "ios";

  buildMode = if isIOS then "c-archive" else "c-shared";
  libraryFileName = if isIOS then "./libstatus.a" else "./libstatus.so";

  goOs = if isAndroid then "android" else "darwin";

  goArch = 
    if isAndroid then arch
    else if isIOS then (if arch == "386" then "amd64" else arch)
    else throw "Unsupported platform!";

  goTags = if isAndroid then "" else " -tags ios ";

  # Metadata common to all builds of status-go
  meta = {
    description = "The Status Go module that consumes go-ethereum.";
    license = lib.licenses.mpl20;
    platforms = with lib.platforms; linux ++ darwin;
  };


in buildGo114Package rec {
  pname = source.repo;
  version = "${source.cleanVersion}-${source.shortRev}-${platform}-${arch}";

  inherit meta;
  inherit (source) src goPackagePath ;

  ANDROID_HOME = androidPkgs;
  ANDROID_NDK_HOME = "${androidPkgs}/ndk-bundle";

  preBuildPhase = ''
    ${flags.vars} 
    cd go/src/${goPackagePath}
    mkdir -p ./statusgo-lib
    go run cmd/library/*.go > ./statusgo-lib/main.go

  '';

  buildPhase = ''
    runHook preBuildPhase
    echo "Building shared library..."

    export GOOS=${goOs} GOARCH=${goArch} API=${api}

    export CGO_CFLAGS="${flags.compiler}"
    export CGO_LDFLAGS="${flags.linker} ${if isAndroid then "-Wl,-soname,libstatus.so" else ""}"
    export CGO_ENABLED=1

    ${flags.vars} 

    go build \
       -v \
      -buildmode=${buildMode} \
      -ldflags="${goBuildLdFlags}" \
      ${goTags} \
      -o ${libraryFileName} \
      $BUILD_FLAGS \
      ./statusgo-lib

    echo "Shared library built:"
    ls -la ./libstatus.*
  '';

  fixupPhase = ''
    find $out -type f -exec ${removeExpr removeReferences} '{}' + || true
  '';

  installPhase = ''
    mkdir -p $out
    cp ./${libraryFileName} $out/${libraryFileName}
    cp ./libstatus.h $out/status.h
  '';

  outputs = [ "out" ];
}
