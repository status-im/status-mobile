{ pkgs, stdenv, lib, fetchFromGitHub
# Dependencies
, xcodeWrapper
, srcRaw
, writeScript
, writeTextFile
, androidPkgs
, git 
, nimBinary 
, platform ? "android"
, arch ? "386"
, api ? "29" }:

let
  osId = builtins.elemAt (builtins.split "\-" stdenv.hostPlatform.system) 2;
  osArch = builtins.elemAt (builtins.split "\-" stdenv.hostPlatform.system) 0;

  ANDROID_NDK_HOME = "${androidPkgs.ndk}";

  
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

  ldDirMap = {
    "386" = "x86";
    "arm" = "armeabi-v7a"; 
    "arm64" = "arm64-v8a";
  };

  # Shorthands for the built phase
  targetArch = lib.getAttr arch targetArchMap;
  ldArch = lib.getAttr arch ldArchMap;
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
    then "amd64" else "${lib.getAttr arch nimCpuMap}";
  nimPlatform = "${(if platform == "ios" then "ios" else "android")}";

  iosSdk = if arch == "386" then "iphonesimulator" else "iphoneos";
  iosArch = lib.getAttr arch iosArchMap;

  isAndroid = lib.hasPrefix "android" platform;
  isIOS = platform == "ios";


  buildType = lib.getConfig "build-type" "";


  routeHeader = builtins.readFile ./route.h;

  PROJECT_ROOT = srcRaw.src;

  # androidIncludes = stdenv.mkDerivation {
  #   name = "nim-status-android-includes";
  #   buildInputs = [ pkgs.coreutils pkgs.gnused ];
  #   builder = writeScript "nim-android-includes.sh"
  #   ''
  #     export PATH=${pkgs.coreutils}/bin:${pkgs.patch}/bin
  #     mkdir $out
  #     cd $out
  #     cp ${ANDROID_NDK_HOME}/sysroot/usr/include/stdio.h .
  #     patch -u stdio.h -i ${stdioPatch}
  #   '';
  # };

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

  compilerFlags = if isAndroid then
    "--sysroot ${ANDROID_NDK_HOME}/toolchains/llvm/prebuilt/${osId}-${osArch}/sysroot -target ${androidTarget}${api} -fPIC -I ${ANDROID_NDK_HOME}/sources/cxx-stl/llvm-libc++/include/ -I${PROJECT_ROOT}/vendor/nimbus-build-system/vendor/Nim-csources-v1/c_code"
    else if isIOS then
    # TODO The conditional for -miphoneos-version-min=8.0 is required,
    # otherwise Nim will complain that thread-local storage is not supported for the current target
    # when expanding 'NIM_THREADVAR' macro
    "-isysroot $(xcrun --sdk ${iosSdk} --show-sdk-path) -I${PROJECT_ROOT}/vendor/nimbus-build-system/vendor/Nim-csources-v1/c_code -fembed-bitcode -arch ${iosArch} ${if arch == "arm" then "" else "-miphoneos-version-min=8.0"} -I${iosIncludes}"
    else throw "Unsupported platform!";

  hostMap = {
      "386" = "x86";
      "arm" = "arm";
      "arm64" = "aarch64";
    };
  hostFlag = if isAndroid then androidTarget else lib.getAttr arch hostMap;


  ldDirMap1 = {
    "386" = "i686-linux-android";
    "arm" = "arm-linux-androideabi";
    "arm64" = "aarch64-linux-android";
  };

  linkerFlags = if isAndroid then
  "--sysroot ${ANDROID_NDK_HOME}/toolchains/llvm/prebuilt/${osId}-${osArch}/sysroot -target ${androidTarget}"
  else if isIOS then
  "--sysroot $(xcrun --sdk ${iosSdk} --show-sdk-path) -fembed-bitcode -arch ${iosArch}"
  else throw "Unsupported platform!";

  cppLibPathPrefix = "${ANDROID_NDK_HOME}/sources/cxx-stl/llvm-libc++/libs/${lib.getAttr arch ldDirMap}/";

  cppLibPathPrefix1 = "${ANDROID_NDK_HOME}/toolchains/llvm/prebuilt/${osId}-${osArch}/sysroot/usr/lib/${lib.getAttr arch ldDirMap1}/${api}/";

  androidToolPathPrefix = "${ANDROID_NDK_HOME}/toolchains/llvm/prebuilt/${osId}-${osArch}/bin";
  androidToolPrefix = "${androidToolPathPrefix}/${targetArch}-linux-${platform}";
  compilerVars = if isAndroid then
    ''
      export PATH=${androidToolPathPrefix}:$PATH
      export AR=${androidToolPrefix}-ar
      export AS=${androidToolPrefix}-as
      export NM=${androidToolPrefix}-nm
      export RANLIB=${androidToolPrefix}-ranlib
      export CC=${androidToolPathPrefix}/clang
      export CXX=${androidToolPathPrefix}/clang
      export LD=${androidToolPathPrefix}/ld

      # This is important, otherwise Nim might not use proper tooling
      mkdir bin
      ln -s $AR bin/ar
      ln -s $AS bin/as
      ln -s $CC bin/gcc
      ln -s $RANLIB bin/ranlib

      touch bin/git
      chmod +x bin/git
      export PATH=./bin:$PATH
    ''
    else if isIOS then
    ''
      mkdir bin
      touch bin/git
      chmod +x bin/git
      export PATH=`pwd`/bin:$PATH

      export PATH=${xcodeWrapper}/bin:$PATH
      export CC=$(xcrun --sdk ${iosSdk} --find clang)
      export CXX=$(xcrun --sdk ${iosSdk} --find clang++)
      export CMAKE_CXX_COMPILER=$(xcrun --sdk ${iosSdk} --find clang++)
      export CFLAGS="${compilerFlags}"
      export LDFLAGS="${linkerFlags}"
    ''
    else throw "Unsupported platform!";
 
  # src = pkgs.fetchgit {
  #   url = "https://github.com/status-im/nim-status";
  #   rev = "33241ee463291ed912681eff278658dc1dba1dcb";
  #   sha256 = "1ki32s13zrwjzhbdyzfzj29mj3hkib2iwqwcwvlan9699p4v19iq";
  #   leaveDotGit = true;
  #   fetchSubmodules = false;
  # };

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
  pname = "liblcproxy";
  name = "liblcproxy";
  src = srcRaw.src;
  #version = lib.strings.substring 0 7 src.rev;
  buildInputs = with pkgs; [ wget git clang which tcl cmake];

  phases = [ "unpackPhase" "preBuildPhase" "buildPhase" "installPhase" ];

  preBuildPhase = ''
      sed -E -i 's|^(.*)-C vendor/nim-nat-traversal/vendor/miniupnp/miniupnpc(.*)|\1 -C vendor/nim-nat-traversal/vendor/miniupnp/miniupnpc CFLAGS="$(CFLAGS)" CC="$(CC)"\2|g' vendor/nimbus-build-system/makefiles/targets.mk


    export HOME=$PWD
    echo 'switch("passC", "${compilerFlags}")' > config.nims
    echo 'switch("passL", "${linkerFlags}")' >> config.nims
    echo 'switch("cpu", "${nimCpu}")' >> config.nims
    echo 'switch("os", "${nimPlatform}")' >> config.nims

    ${createNimbleLink}

    ${compilerVars}
    export PATH=${nimBinary}:$PATH
    which clang
    echo $PATH
  '';


  buildPhase = ''
    make V=3 CFLAGS="${compilerFlags}" LDFLAGS="${linkerFlags}" CC=clang CXX=clang LD=ld OS=${nimHostOs} USE_SYSTEM_NIM=1 USE_SYSTEM_LIBS=1 NIMFLAGS="-d:libbacktraceUseSystemLibs" liblcproxy

   '';

  installPhase = ''
    mkdir -p $out
    cp build/liblcproxy/liblcproxy* $out/
    cp build/liblcproxy/lcproxy.h $out/
    cp build/liblcproxy/nimbase.h $out/
  '';
}

