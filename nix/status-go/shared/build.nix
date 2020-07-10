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
  inherit (lib) attrNames getAttr strings concatStringsSep concatMapStrings;

  removeReferences = [ go ];
  removeExpr = refs: ''remove-references-to ${concatMapStrings (ref: " -t ${ref}") refs}'';

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

  iosArchMap = {
    "386" = "x86_64";
    "arm" = "armv7";
    "arm64" = "arm64";
  };
  iosSdk = if arch == "386" then "iphonesimulator" else "iphoneos";
  iosArch = getAttr arch iosArchMap;
  compilerFlags = if platform == "android" || platform == "androideabi" then
  "\"-isysroot $ANDROID_NDK_HOME/toolchains/llvm/prebuilt/${osId}-${osArch}/sysroot -target ${androidTarget}${api}\""
  else
  "\"-isysroot $(xcrun --sdk ${iosSdk} --show-sdk-path) -miphonesimulator-version-min=7.0 -fembed-bitcode -arch ${iosArch}\"";

  linkerFlags = if platform == "android" || platform == "androideabi" then
  "\" --sysroot $ANDROID_NDK_HOME/toolchains/llvm/prebuilt/${osId}-${osArch}/sysroot \
  -target ${androidTarget}${api} -v -Wl,-soname,libstatus.so\""
  else
  "\"--sysroot $(xcrun --sdk ${iosSdk} --show-sdk-path) -miphonesimulator-version-min=7.0 -fembed-bitcode -arch ${iosArch}\"";

  buildMode = if platform == "ios" then "c-archive" else "c-shared";
  libraryFileName = if platform == "ios" then "./libstatus.a" else "./libstatus.so";

  compilerVars = if platform == "android" || platform == "androideabi" then
      ''
        export CC=$ANDROID_NDK_HOME/toolchains/llvm/prebuilt/${osId}-${osArch}/bin/clang
        export CXX=$ANDROID_NDK_HOME/toolchains/llvm/prebuilt/${osId}-${osArch}/bin/clang++
        export PATH=$ANDROID_NDK_HOME/toolchains/llvm/prebuilt/${osId}-${osArch}/bin:$PATH
      '' else ''
        export PATH=${xcodeWrapper}/bin:$PATH 
        export CC=$(xcrun --sdk ${iosSdk} --find clang) 
        export CXX=$(xcrun --sdk ${iosSdk} --find clang++)
        '';
  goOs = if platform == "android" || platform == "androideabi" then
    "android"
    else "darwin";

  goArch = if platform == "android" || platform == "androideabi" then
  if arch == "386" then "386" 
  else if platform == "androideabi" then "arm "else "arm64"
    else
    if arch == "386" then "amd64" else 
    if arch == "arm" then "arm" else "arm64";

  goTags = if platform == "android" || platform == "androideabi" then
  ""
  else " -tags ios ";

in buildGo114Package rec {
  pname = source.repo;
  version = "${source.cleanVersion}-${source.shortRev}-${platform}-${arch}";

  inherit meta compilerVars goOs goArch goTags targetArch platform ldArch buildMode 
  compilerFlags linkerFlags libraryFileName;
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

    #export LIBRARY_PATH=$ANDROID_NDK_HOME/platforms/android-$API/arch-${ldArch}/usr/lib
    export CGO_CFLAGS=${compilerFlags}
    export CGO_LDFLAGS=${linkerFlags}
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
    set -x
    find $out -type f -exec ${removeExpr removeReferences} '{}' + || true
  '';

  installPhase = ''
    mkdir -p $out
    cp ./libstatus.* $out/
  '';

  outputs = [ "out" ];
}
