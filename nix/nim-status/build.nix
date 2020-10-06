{ pkgs, stdenv, lib, fetchFromGitHub
# Dependencies
, xcodeWrapper
, git, androidPkgs
, platform ? "android"
, arch ? "386"
, api ? "23" }:

let
  inherit (lib) attrNames getAttr strings concatStringsSep concatMapStrings;

  osId = builtins.elemAt (builtins.split "\-" stdenv.hostPlatform.system) 2;
  osArch = builtins.elemAt (builtins.split "\-" stdenv.hostPlatform.system) 0;

  ANDROID_HOME = androidPkgs;
  ANDROID_NDK_HOME = "${androidPkgs}/ndk-bundle";

  nimIosPatch = pkgs.fetchurl { url = "https://patch-diff.githubusercontent.com/raw/nim-lang/Nim/pull/15268.patch"; sha256 = "04jg0ngqlyzwrc19rafx4s257my6bhd27myv4p78hmsyzhp36za6"; };
  patchedNim = pkgs.nim.overrideAttrs (_: { 
    version = "pr-15268"; 
    patches = [ nimIosPatch ]; 
  });

  targetArchMap = rec {
    "386" = "i686";
    "arm" = "arm";
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
  androidTarget = targetArch + "-linux-" + platform;

  # Arg arch -> Nim arch
  nimCpuMap = {
    "386" = "i386";
    "x86_64" = "amd64"; 
    "arm" = "arm"; 
    "arm64" = "arm64";
  };

  iosArchMap = {
    "386" = "x86_64";
    "arm" = "armv7";
    "arm64" = "arm64";
  };

  nimCpu = if platform == "ios" && arch == "386"
    then "--cpu:amd64" else "--cpu:${getAttr arch nimCpuMap} ";
  nimPlatform = "--os:${(if platform == "ios" then "ios" else "android")} ";
  iosSdk = if arch == "386" then "iphonesimulator" else "iphoneos";
  iosArch = getAttr arch iosArchMap;

  isAndroid = lib.hasPrefix "android" platform;
  isIOS = platform == "ios";

  compilerFlags = if isAndroid then
    "-isysroot ${ANDROID_NDK_HOME}/sysroot -target ${androidTarget}${api}"
    else if isIOS then
    "-isysroot $(xcrun --sdk ${iosSdk} --show-sdk-path) -fembed-bitcode -arch ${iosArch}"
    else throw "Unsupported platform!";

    linkerFlags = if isAndroid then
    "--sysroot ${ANDROID_NDK_HOME}/platforms/android-${api}/arch-${ldArch} -target ${androidTarget}"
    else if isIOS then
    "--sysroot $(xcrun --sdk ${iosSdk} --show-sdk-path) -fembed-bitcode -arch ${iosArch}"
    else throw "Unsupported platform!";

    compilerVars = if platform == "android" || platform == "androideabi" then
      "PATH=${ANDROID_NDK_HOME + "/toolchains/llvm/prebuilt/${osId}-${osArch}/bin"}:$PATH "
      else "PATH=${xcodeWrapper}/bin:$PATH \
            CC=$(xcrun --sdk ${iosSdk} --find clang) \
            CXX=$(xcrun --sdk ${iosSdk} --find clang++) ";

    arPath = "${ANDROID_NDK_HOME + "/toolchains/llvm/prebuilt/${osId}-${osArch}/bin/${targetArch}-linux-${platform}-ar"}";
    ranlibPath = "${ANDROID_NDK_HOME + "/toolchains/llvm/prebuilt/${osId}-${osArch}/bin/${targetArch}-linux-${platform}-ranlib "}";
 

in stdenv.mkDerivation rec {
  name = "nim-status"; # TODO: use pname and version
  buildInputs = with pkgs; [ patchedNim ];
  src = fetchFromGitHub {
    owner = "status-im";
    repo = "nim-status";
    name = "nim-status";
    rev = "113ab223795fa44f9f6d3ecb9a0da7e033022ea9";
    sha256 = "19nvn8b0n1r4nqjiq14057iwyr2m4a37i33yvf3ngjik26jyyzxk";
  };

  phases = [ "unpackPhase" "preBuildPhase" "buildPhase" "installPhase" ];

  preBuildPhase = ''
    echo 'switch("passC", "${compilerFlags}")' >> config.nims
    echo 'switch("passL", "${linkerFlags}")' >> config.nims
  '';

  buildPhase = ''
    echo "os_id: " ${osId}
    export PATH=.:$PATH
    ln -s ${arPath} ar
    ln -s ${ranlibPath} ranlib
    ${compilerVars} \
    nim c \
      --app:staticLib \
      --header \
      ${nimCpu} \
      ${nimPlatform} \
      --noMain \
      --nimcache:nimcache/nim_status \
      -o:build/libnim_status.a \
      src/nim_status.nim
   '';

  installPhase = ''
    mkdir -p $out
    mv nimcache/nim_status/nim_status.h $out/
    mv libnim_status.a $out/
    mv src/nimbase.h $out/
  '';
}
