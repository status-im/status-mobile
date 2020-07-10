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
    name = "openssl-source.tar.gz";
    url = "https://www.openssl.org/source/openssl-1.1.1h.tar.gz";
    sha256 = "5c9ca8774bd7b03e5784f26ae9e9e6d749c9da2438545077e6b3d755a06595d9";

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

  configureArch = if isAndroid then "android-${lib.getAttr arch androidConfigureArchMap}"
                  else if isIOS then "${lib.getAttr arch iosConfigureArchMap}-xcrun"
                  else throw "Unsupported platform!";
  configureFlags = if isAndroid then "-D__ANDROID_API__=${api}"
                   else if isIOS then ""
                   else throw "Unsupported platform!";

  osId = builtins.elemAt (builtins.split "\-" stdenv.hostPlatform.system) 2;
  osArch = builtins.elemAt (builtins.split "\-" stdenv.hostPlatform.system) 0;

  targetArchMap = rec {
    "386" = "i686";
    "arm" = "arm";
    "arm64" = "aarch64";
  };
  targetArch = lib.getAttr arch targetArchMap;
  androidTarget = targetArch + "-linux-" + platform;
  iosSdk = if arch == "386" then "iphonesimulator" else "iphoneos";
  androidToolPathPrefix = "${ANDROID_NDK_HOME}/toolchains/llvm/prebuilt/${osId}-${osArch}/bin/${targetArch}-linux-${platform}";
  compilerFlags = if isAndroid then ""
      else if isIOS then
      "-isysroot $(xcrun --sdk ${iosSdk} --show-sdk-path) -fembed-bitcode -arch ${iosArch} -miphoneos-version-min=8.0"
      else throw "Unsupported platform!";

  linkerFlags = if isAndroid then ""
   else if isIOS then
  "--sysroot $(xcrun --sdk ${iosSdk} --show-sdk-path) -fembed-bitcode -arch ${iosArch} -v"
  else throw "Unsupported platform!";



  compilerVars = if isAndroid then
    ''
      export PATH=${ANDROID_NDK_HOME + "/toolchains/llvm/prebuilt/${osId}-${osArch}/bin"}:$PATH
      ln -s ${androidToolPathPrefix}-ar ar
      ln -s ${androidToolPathPrefix}-ranlib ranlib
      export PATH=.:$PATH
    ''
    else if isIOS then
    ''
      export PATH=${xcodeWrapper}/bin:$PATH
      export CC=$(xcrun --sdk ${iosSdk} --find clang)
      export CXX=$(xcrun --sdk ${iosSdk} --find clang++)
    ''
    else throw "Unsupported platform!";



in stdenv.mkDerivation rec {
  name = "openssl_lib";
  inherit src ANDROID_NDK_HOME;
  buildInputs = with pkgs; [ perl ];

  phases = ["unpackPhase" "configurePhase" "buildPhase" "installPhase"];

  configurePhase = ''
    export PATH=${pkgs.perl}/bin:$PATH
    ${compilerVars}
    patchShebangs .
	  ./Configure ${configureArch} ${configureFlags} CFLAGS="${compilerFlags}" LDFLAGS="${linkerFlags}" -no-shared
  '';

  buildPhase = ''
    make
  '';

  installPhase = ''
    mkdir -p $out
    cp libcrypto.a $out
    cp libssl.a $out
  '';
}
