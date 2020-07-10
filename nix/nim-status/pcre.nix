{ pkgs, stdenv, lib, fetchFromGitHub
# Dependencies
, xcodeWrapper
, writeScript
, androidPkgs
, git 
, platform ? "android"
, arch ? "386"
, api ? "23" } :


let
  src = pkgs.fetchurl {
    name = "pcre-source.tar.gz";
    url = "https://ftp.pcre.org/pub/pcre/pcre-8.44.tar.gz";
    sha256 = "0a3pnhzhd8wipmqq9dcn3phb494hkn47pxqsf8skj3xxyd5gvjmf";

  };

  androidConfigureArchMap = {
    "386" = "x86";
    "arm" = "arm";
    "arm64" = "arm64";
  };

  iosConfigureArchMap = {
    "386" = "iossimulator";
    "arm" = "ios";
    "arm64" = "ios64";
  };

  ANDROID_NDK_HOME = "${androidPkgs}/ndk-bundle";


  iosArchMap = {
    "386" = "x86_64";
    "arm" = "armv7";
    "arm64" = "arm64";
  };
  iosArch = lib.getAttr arch iosArchMap;
  isAndroid = lib.hasPrefix "android" platform;
  isIOS = platform == "ios";

  osId = builtins.elemAt (builtins.split "\-" stdenv.hostPlatform.system) 2;
  osArch = builtins.elemAt (builtins.split "\-" stdenv.hostPlatform.system) 0;

  targetArchMap = rec {
    "386" = "i686";
    "arm" = "arm";
    "arm64" = "aarch64";
  };
  targetArch = lib.getAttr arch targetArchMap;
  iosSdk = if arch == "386" then "iphonesimulator" else "iphoneos";
  androidTarget = targetArch + "-linux-" + platform;

  ldArchMap = {
      "386" = "x86";
      "arm" = "arm";
      "arm64" = "arm64";
  };


  ldArch = lib.getAttr arch ldArchMap;
  androidToolPathPrefix = "${ANDROID_NDK_HOME}/toolchains/llvm/prebuilt/${osId}-${osArch}/bin/${targetArch}-linux-${platform}";
  isysroot = if isAndroid then 
    "${ANDROID_NDK_HOME}/toolchains/llvm/prebuilt/${osId}-${osArch}/sysroot"             
    else "$(xcrun --sdk ${iosSdk} --show-sdk-path)";

  compilerFlags = if isAndroid then
      "-isysroot ${isysroot} -target ${androidTarget}${api} -fPIC"
      else if isIOS then
      "-isysroot ${isysroot} -fembed-bitcode -arch ${iosArch} -miphoneos-version-min=8.0"
      else throw "Unsupported platform!";

  linkerFlags = if isAndroid then  
  "--sysroot ${isysroot} -target ${androidTarget}${api}"
  else if isIOS then
  "--sysroot $(xcrun --sdk ${iosSdk} --show-sdk-path) -fembed-bitcode -arch ${iosArch} -v"
  else throw "Unsupported platform!";



  compilerVars = if isAndroid then
    ''
      export PATH=${ANDROID_NDK_HOME + "/toolchains/llvm/prebuilt/${osId}-${osArch}/bin"}:$PATH
      export AR=${androidToolPathPrefix}-ar
      export AS=${androidToolPathPrefix}-as
      export LD=${androidToolPathPrefix}-ld
      export NM=${androidToolPathPrefix}-nm
      export CC=clang
      export RANLIB=${androidToolPathPrefix}-ranlib
    ''
    else if isIOS then
    ''
      export PATH=${xcodeWrapper}/bin:$PATH
      export CC=$(xcrun --sdk ${iosSdk} --find clang)
      export CXX=$(xcrun --sdk ${iosSdk} --find clang++)
    ''
    else throw "Unsupported platform!";


    hostMap = {
      "386" = "x86";
      "arm" = "arm";
      "arm64" = "aarch64";
    };
    hostFlag = if isAndroid then androidTarget else lib.getAttr arch hostMap;

in stdenv.mkDerivation rec {
  name = "pcre_lib";
  inherit src ANDROID_NDK_HOME;
  #buildInputs = with pkgs; [ perl ];

  phases = ["unpackPhase" "configurePhase" "buildPhase" "installPhase"];

  configurePhase = ''
    ${compilerVars}
    patchShebangs .
	  ./configure --enable-utf --with-sysroot=${isysroot} --host=${hostFlag} CFLAGS="${compilerFlags}" LDFLAGS="${linkerFlags}" --disable-shared --disable-cpp
  '';

  buildPhase = ''
    ${compilerVars}
    make
  '';

  installPhase = ''
    mkdir -p $out/include
    cp .libs/libpcre.a $out
    cp pcre.h $out/include
  '';
}
