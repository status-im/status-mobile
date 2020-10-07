{ stdenv, lib, fetchFromGitHub, buildGo114Package
# Dependencies
, xcodeWrapper
, go, androidPkgs
# metadata and status-go source
, meta, source
# build parameters
, platform ? "android"
, arch ? "386"
, api ? "23" }:

let
  inherit (lib) attrNames attrValues getAttr mapAttrs strings concatStringsSep concatMapStrings;

  removeReferences = [ go ];
  removeExpr = refs: ''remove-references-to ${concatMapStrings (ref: " -t ${ref}") refs}'';

  # Params to be set at build time, important for About section and metrics
  goBuildParams = {
    GitCommit = source.rev;
    Version = source.cleanVersion;
  };

  targetArchMap = rec {
    "386" = "i686";
    "arm" = "armv7a";
    "arm64" = "aarch64";
  };
  ldArchMap = {
    "386" = "x86";
    "arm" = "arm";
    "arm64" = "arm64";
  };

  # Shorthands for the built phase
  targetArch = getAttr arch targetArchMap;
  ldArch = getAttr arch ldArchMap;

  osId = builtins.elemAt (builtins.split "\-" stdenv.hostPlatform.system) 2;
  osArch = builtins.elemAt (builtins.split "\-" stdenv.hostPlatform.system) 0;

  androidTarget = targetArch + "-linux-" + platform;

  isAndroid = lib.hasPrefix "android" platform;
  isIOS = platform == "ios";

  iosArchMap = {
    "386" = "x86_64";
    "arm" = "armv7";
    "arm64" = "arm64";
  };

  iosSdk = if arch == "386" then "iphonesimulator" else "iphoneos";
  iosArch = getAttr arch iosArchMap;

  compilerFlags = if isAndroid then
  "-isysroot $ANDROID_NDK_HOME/toolchains/llvm/prebuilt/${osId}-${osArch}/sysroot -target ${androidTarget}${api}"
  else if isIOS then
  "-isysroot $(xcrun --sdk ${iosSdk} --show-sdk-path) -miphonesimulator-version-min=7.0 -fembed-bitcode -arch ${iosArch}"
  else throw "Unsupported platform!";

  linkerFlags = if isAndroid then
  " --sysroot $ANDROID_NDK_HOME/toolchains/llvm/prebuilt/${osId}-${osArch}/sysroot \
  -target ${androidTarget}${api} -v -Wl,-soname,libstatus.so"
  else if isIOS then
  "--sysroot $(xcrun --sdk ${iosSdk} --show-sdk-path) -miphonesimulator-version-min=7.0 -fembed-bitcode -arch ${iosArch}"
  else throw "Unsupported platform!";

  #linkerFlags = linkerFlags1 + (concatStringsSep " " goBuildLdFlags);

  buildMode = if isIOS then "c-archive" else "c-shared";
  libraryFileName = if isIOS then "./libstatus.a" else "./libstatus.so";

  compilerVars = if isAndroid then 
      ''
        export CC=$ANDROID_NDK_HOME/toolchains/llvm/prebuilt/${osId}-${osArch}/bin/clang
        export CXX=$ANDROID_NDK_HOME/toolchains/llvm/prebuilt/${osId}-${osArch}/bin/clang++
        export PATH=$ANDROID_NDK_HOME/toolchains/llvm/prebuilt/${osId}-${osArch}/bin:$PATH
      '' 
      else if isIOS then 
      ''
        export PATH=${xcodeWrapper}/bin:$PATH 
        export CC=$(xcrun --sdk ${iosSdk} --find clang) 
        export CXX=$(xcrun --sdk ${iosSdk} --find clang++)
      ''
      else throw "Unsupported platform!";

  goOs = if isAndroid then "android" else "darwin";

  goArch = 
    if isAndroid then arch
    else if isIOS then (if arch == "386" then "amd64" else arch)
    else throw "Unsupported platform!";

  goTags = if isAndroid then "" else " -tags ios ";

in buildGo114Package rec {
  pname = source.repo;
  version = "${source.cleanVersion}-${source.shortRev}-${platform}-${arch}";

  inherit (source) src goPackagePath ;

  ANDROID_HOME = androidPkgs;
  ANDROID_NDK_HOME = "${androidPkgs}/ndk-bundle";

  preBuildPhase = ''
    cd go/src/${goPackagePath}
    mkdir -p ./statusgo-lib
    go run cmd/library/*.go > ./statusgo-lib/main.go

    ${compilerVars} 
  '';

  buildPhase = ''
    runHook preBuildPhase
    echo "Building shared library..."

    export GOOS=${goOs} GOARCH=${goArch} API=${api}
    export TARGET=${targetArch}-linux-${platform}

    export CGO_CFLAGS="${compilerFlags}"
    export CGO_LDFLAGS="${linkerFlags}"
    export CGO_ENABLED=1

    echo "Building for target: $TARGET"

    ${compilerVars} 

    go build \
      -buildmode=${buildMode} \
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
    cp ./libstatus.* $out/
  '';

  outputs = [ "out" ];
}
