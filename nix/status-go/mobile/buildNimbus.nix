{ pkgs, stdenv, lib, fetchFromGitHub
# Dependencies
, xcodeWrapper
, srcRaw
, writeScript
, writeTextFile
, androidPkgs
, git 
, nimCompiler 
, platform ? "android"
, arch ? "386"
, api ? "29" }:

let
  osId = builtins.elemAt (builtins.split "\-" stdenv.hostPlatform.system) 2;
  osArch = builtins.elemAt (builtins.split "\-" stdenv.hostPlatform.system) 0;

  
  # Arg arch -> Nim arch
  nimCpuMap = {
    "x86" = "i386";
    "x86-64" = "amd64"; 
    "arm" = "arm"; 
    "arm64" = "arm64";
  };

  iosArchMap = {
    "x86" = "x86_64";
    "x86-64" = "x86_64";
    "arm" = "armv7";
    "arm64" = "arm64";
  };

  nimCpu = if platform == "ios" && arch == "x86"
    then "amd64" else "${lib.getAttr arch nimCpuMap}";
  nimPlatform = "${(if platform == "ios" then "ios" else "android")}";

  iosSdk = if arch == "x86" then "iphonesimulator" else "iphoneos";
  iosArch = lib.getAttr arch iosArchMap;

  isAndroid = lib.hasPrefix "android" platform;
  isIOS = platform == "ios";


  buildType = lib.getConfig "build-type" "";


  routeHeader = builtins.readFile ./route.h;

  PROJECT_ROOT = srcRaw.src;

  iosIncludes = stdenv.mkDerivation {
    name = "nim-status-ios-includes";
    buildInputs = [ pkgs.coreutils ];
    builder = writeScript "nim-ios-includes.sh"
    ''
      export PATH=${pkgs.coreutils}/bin
      mkdir $out
      cd $out
      mkdir net
      echo "${routeHeader}" > net/route.h
    '';
  };


  ANDROID_NDK_HOME = "${androidPkgs.sdk}/ndk-bundle";
  
  
  androidToolchain = "${ANDROID_NDK_HOME}/toolchains/llvm/prebuilt/${osId}-${osArch}";
  androidTargetArchMap = {
    "x86" = "i686-linux-android";
    "x86-64" = "x86_64-linux-android";
    "arm" = "armv7a-linux-androideabi";
    "arm64" = "aarch64-linux-android";
  };

  androidTargetArch = lib.getAttr arch androidTargetArchMap;

  iosVersion = "9.0";

  sysroot = if isAndroid then "${androidToolchain}/sysroot" else "$(xcrun --sdk ${iosSdk} --show-sdk-path)";  
  compilerFlags = if isAndroid then
    "--sysroot ${sysroot} -fPIC -target ${androidTargetArch}${api}"
    else if isIOS then
    # TODO The conditional for -miphoneos-version-min=8.0 is required,
    # otherwise Nim will complain that thread-local storage is not supported for the current target
    # when expanding 'NIM_THREADVAR' macro
    "-isysroot ${sysroot} -fembed-bitcode -target ${iosArch}-apple-ios${iosVersion} ${if arch == "arm" then "" else "-miphoneos-version-min=${iosVersion}"} -I${iosIncludes}"
    else throw "Unsupported platform!";

  linkerFlags = if isAndroid then
  "--sysroot ${sysroot} -target ${androidTargetArch}${api}"
  else if isIOS then
  "--sysroot ${sysroot} -fembed-bitcode -target ${iosArch}-apple-ios${iosVersion} ${if arch == "arm" then "" else "-miphoneos-version-min=${iosVersion}"} -v"
  else throw "Unsupported platform!";
  
  ldDirMap = {
    "x86" = "i686";
    "x86-64" = "x86_64";
    "arm" = "armeabi-v7a"; 
    "arm64" = "arm64-v8a";
  };

  cmakeArgs = if isAndroid then
    "-DCMAKE_SYSTEM_NAME=Android -DCMAKE_SYSTEM_VERSION=${api} -DCMAKE_ANDROID_ARCH_ABI=${lib.getAttr arch ldDirMap} -DCMAKE_ANDROID_NDK=${ANDROID_NDK_HOME}"
  else if isIOS then "-DCMAKE_SYSTEM_NAME=iOS -DCMAKE_OSX_SYSROOT=\"$(CMAKE_SYSROOT)\" -DCMAKE_OSX_ARCHITECTURES=${iosArch} -DCMAKE_OSX_DEPLOYMENT_TARGET=9.3 -DCMAKE_XCODE_ATTRIBUTE_ONLY_ACTIVE_ARCH=NO -DCMAKE_IOS_INSTALL_COMBINED=YES"
  else throw "Unsupported platform!";
  #clangPath = if isAndroid then "${androidToolchain}/bin" else "";
  #clangName = if isAndroid then "${androidTargetArch}${api}-clang" else "";

  compilerVars = if isAndroid then
    ''
      export PATH=${androidToolchain}/bin:$PATH
      export CC=clang
      export CXX=clang++
      export AR=${androidToolchain}/bin/llvm-ar
      export NM=${androidToolchain}/bin/llvm-nm
      export RANLIB=${androidToolchain}/bin/llvm-ranlib
      export LD=${androidToolchain}/bin/ld
      export AS=$CC
      export CFLAGS="${compilerFlags}"
      export LDFLAGS="${linkerFlags}"

      mkdir bin
      touch bin/git
      chmod +x bin/git
      export PATH=./bin:$PATH
    ''
    else if isIOS then
    ''
      export PATH=${xcodeWrapper}/bin:$PATH
      export CC=clang #$(xcrun --sdk ${iosSdk} --find clang)
      export CXX=clang++ #$(xcrun --sdk ${iosSdk} --find clang++)
      export LD=clang
      export CFLAGS="${compilerFlags}"
      export LDFLAGS="${linkerFlags}"

      mkdir bin
      touch bin/git
      chmod +x bin/git
      export PATH=./bin:$PATH

      export CMAKE_SYSROOT=${sysroot}
    ''
    else throw "Unsupported platform!";
 
  src = srcRaw;


  nimHostOs = if osId == "darwin" then "Darwin"
              else if osId == "linux" then "Linux"
              else "Windows_NT";
  createNimbleLink = writeTextFile {
    name = "createNimbleLink.sh";
    text = ''
    mkdir -p vendor/.nimble/pkgs

    curdir=`pwd`
    for dir in vendor/*/;
    do
      baseDirName=`basename ''${dir}`
      dirName=vendor/.nimble/pkgs/$baseDirName-\#head
      echo $dirName
      mkdir -p $dirName

      packageDir="''${curdir}/vendor/''${baseDirName}"
      if [ -d "''${packageDir}/src" ]; then
        packageDir="''${packageDir}/src"
      fi
      echo "''${packageDir}" > ''${dirName}/''${baseDirName}.nimble-link
      echo "''${packageDir}" >> ''${dirName}/''${baseDirName}.nimble-link
    done
    '';
    executable = true;
  };

in stdenv.mkDerivation rec {
  pname = "libverifproxy";
  name = "libverifproxy";
  src = srcRaw.src;
  #version = lib.strings.substring 0 7 src.rev;
  buildInputs = with pkgs; [ wget git which tcl cmake libtool binutils];

  phases = [ "unpackPhase" "preBuildPhase" "buildPhase" "installPhase" ];

  preBuildPhase = ''

    sed -E -i 's|^(LIBBACKTRACE_SED := true.*)|\1\nCMAKE_ARGS := ${cmakeArgs}|g' vendor/nim-libbacktrace/Makefile

    sed -E -i 's|^(.*)(useNews\* = )(.*)|\1\2 false|g' vendor/nim-json-rpc/json_rpc/clients/config.nim
    sed -E -i 's|(.*XOPEN_SOURCE.*)|#\1|g' vendor/nim-nat-traversal/vendor/miniupnp/miniupnpc/Makefile

    export HOME=$PWD

    ${createNimbleLink}

    ${compilerVars}
    export PATH=${nimCompiler}/bin:$PATH
    #export PATH=${pkgs.binutils}/bin:$PATH
    which clang
    echo $PATH
  '';


  buildPhase = ''
    which ar
    set -x
    make V=3 OS=${nimHostOs} \
      CC=clang CXX=clang USE_SYSTEM_NIM=1 \
      NIMFLAGS="\
      --passC:\"${compilerFlags}\" \
      --passL:\"${linkerFlags}\" \
      --debuginfo:off \
      --cc:clang \
      --listcmd \
      --cpu:${nimCpu} \
      --os:${nimPlatform} \
      -d:disableMarchNative" \
    libverifproxy

    echo "### ls build/libverifproxy"
    ls -l build/libverifproxy/*
   '';

  installPhase = ''
    set -x
    mkdir -p $out
    cp build/libverifproxy/libverifproxy* $out/
    cp build/libverifproxy/verifproxy.h $out/
    cp build/libverifproxy/nimbase.h $out/
  '';
}

