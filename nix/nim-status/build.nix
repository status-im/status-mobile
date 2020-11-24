{ pkgs, stdenv, lib, fetchFromGitHub
# Dependencies
, xcodeWrapper
, srcRaw
, writeScript
, openssl
, pcre
, writeTextFile
, androidPkgs
, git 
, platform ? "android"
, arch ? "386"
, api ? "29" }:

let
  osId = builtins.elemAt (builtins.split "\-" stdenv.hostPlatform.system) 2;
  osArch = builtins.elemAt (builtins.split "\-" stdenv.hostPlatform.system) 0;


  stdioPatch = ./stdio.patch;
  ANDROID_NDK_HOME = "${androidPkgs}/ndk-bundle";

  
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
  nimOverride = ./nim_status.nim;
  applyNimOverride = 
    if (lib.elem buildType ["release" "nightly"]) then
    "cp ${nimOverride} src/nim_status/c/nim_status.nim" else "";

  routeHeader = builtins.readFile ./route.h;

  androidIncludes = stdenv.mkDerivation {
    name = "nim-status-android-includes";
    buildInputs = [ pkgs.coreutils pkgs.gnused ];
    builder = writeScript "nim-android-includes.sh"
    ''
      export PATH=${pkgs.coreutils}/bin:${pkgs.patch}/bin
      mkdir $out
      cd $out
      cp ${ANDROID_NDK_HOME}/sysroot/usr/include/stdio.h .
      patch -u stdio.h -i ${stdioPatch}
    '';
  };
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
    "--sysroot ${ANDROID_NDK_HOME}/sysroot -target ${androidTarget}${api} -fPIC -I ${pcre}/include -I ${ANDROID_NDK_HOME}/sources/cxx-stl/llvm-libc++/include/ "
    else if isIOS then
    # TODO The conditional for -miphoneos-version-min=8.0 is required,
    # otherwise Nim will complain that thread-local storage is not supported for the current target
    # when expanding 'NIM_THREADVAR' macro
    "-isysroot $(xcrun --sdk ${iosSdk} --show-sdk-path) -fembed-bitcode -arch ${iosArch} -I${iosIncludes} ${if arch == "arm" then "" else "-miphoneos-version-min=8.0"}"
    else throw "Unsupported platform!";

  sqliteCompilerFlags = if isAndroid then
  "-isysroot ${ANDROID_NDK_HOME}/toolchains/llvm/prebuilt/${osId}-${osArch}/sysroot -target ${androidTarget}${api}"
  else if isIOS then
  "-isysroot $(xcrun --sdk ${iosSdk} --show-sdk-path) -miphonesimulator-version-min=7.0 -fembed-bitcode -arch ${iosArch}"
  else throw "Unsupported platform!";

  sqliteLinkerFlags = if isAndroid then
  " --sysroot ${ANDROID_NDK_HOME}/toolchains/llvm/prebuilt/${osId}-${osArch}/sysroot -target ${androidTarget}${api} -v -Wl,-soname,libstatus.so"
  else if isIOS then
  "--sysroot $(xcrun --sdk ${iosSdk} --show-sdk-path) -miphonesimulator-version-min=7.0 -fembed-bitcode -arch ${iosArch}"
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
  "--sysroot ${ANDROID_NDK_HOME}/platforms/android-${api}/arch-${ldArch} -target ${androidTarget}"
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

      # This is important, otherwise Nim might not use proper tooling
      mkdir bin
      ln -s $AR bin/ar
      ln -s $AS bin/as
      ln -s $CC bin/gcc
      ln -s $RANLIB bin/ranlib
      export PATH=./bin:$PATH
    ''
    else if isIOS then
    ''
      export PATH=${xcodeWrapper}/bin:$PATH
      export CC=$(xcrun --sdk ${iosSdk} --find clang)
      export CXX=$(xcrun --sdk ${iosSdk} --find clang++)
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

  opensslSrc = stdenv.mkDerivation rec {
    pname = "openssl";
    version = "1.1.1g";
    phases = [ "unpackPhase" "installPhase"];
    src = pkgs.fetchurl {
      name = "openssl-nim-status.tar.gz";
      url = "https://bintray.com/homebrew/bottles/download_file?file_path=openssl%401.1-1.1.1g.high_sierra.bottle.tar.gz";
      sha256 = "1xx0zhmkal5d8jblqy83v3wmybqrq1pb20l5pz49sj26m9pxkfpf";
    };
    installPhase = ''
      set -x
      mkdir -p $out
      cp -r 1.1.1g/* $out/
    '';
    outputs = [ "out" ];
  };
  pcreSrc = stdenv.mkDerivation rec {
    pname = "pcre";
    version = "8.44";
    src = pkgs.fetchurl {
      name = "pcre-nim-status.tar.gz";
      url = "https://bintray.com/homebrew/bottles/download_file?file_path=pcre-8.44.high_sierra.bottle.tar.gz";
      sha256 = "19d2inr7psigyj65c2gqhvk9753352ym9viw1k84g623w58i7smf";
    };

    installPhase = ''
      mkdir -p $out
      cp -r 8.44/* $out/
    '';
    
    outputs = [ "out" ];
  };

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
  pname = "nim-status";
  version = lib.strings.substring 0 7 src.rev;
  inherit src;
  buildInputs = with pkgs; [ libtool wget git gcc which patchedNim tcl];

  GIT_SSL_CAINFO = "${pkgs.cacert}/etc/ssl/certs/ca-bundle.crt";
  phases = [ "unpackPhase" "preBuildPhase" "buildPhase" "installPhase" ];

  preBuildPhase = ''
    sed -E -i 's|^(.*)-DNATPMP_MAX_RETRIES=4(.*)|\1 -DNATPMP_MAX_RETRIES=4 $(CFLAGS)\2|g' vendor/nim-waku/vendor/nimbus-build-system/makefiles/targets.mk


    sed -E -i 's/^CC := gcc$/CC := clang/g' vendor/nimbus-build-system/makefiles/variables.mk
    sed -E -i 's/^CC := gcc$/CC := clang/g' vendor/nim-waku/vendor/nimbus-build-system/makefiles/variables.mk

    sed -E -i 's/(.*deps-common.*)/#\1/g' vendor/nim-sqlcipher/Makefile
    sed -E -i 's/(.*update-common.*)/#\1/g' vendor/nim-sqlcipher/Makefile

    sed -E -i 's/git clean \-dfx/true/g' vendor/nim-sqlcipher/Makefile
    sed -E -i 's/git stash/true/g' vendor/nim-sqlcipher/Makefile
    sed -E -i 's|2> /dev/null||g' vendor/nim-sqlcipher/Makefile
    sed -E -i 's/^(.*\.\/configure)/\1 --host=${hostFlag} /g' vendor/nim-sqlcipher/Makefile


    echo 'switch("passC", "${compilerFlags}")' >> config.nims
    echo 'switch("passL", "${linkerFlags}")' >> config.nims
    echo 'switch("cpu", "${nimCpu}")' >> config.nims
    echo 'switch("os", "${nimPlatform}")' >> config.nims

    echo 'switch("passC", "${compilerFlags}")' >> vendor/nim-sqlcipher/config.nims
    echo 'switch("passL", "${linkerFlags}")' >> vendor/nim-sqlcipher/config.nims
    echo 'switch("cpu", "${nimCpu}")' >> vendor/nim-sqlcipher/config.nims
    echo 'switch("os", "${nimPlatform}")' >> vendor/nim-sqlcipher/config.nims


    sed -E -i '3 i switch("passL", "-L${cppLibPathPrefix}")' vendor/nim-sqlcipher/vendor/nimterop/nimterop/toast.nims
    sed -E -i '3 i switch("passL", "-L${cppLibPathPrefix1}")' vendor/nim-sqlcipher/vendor/nimterop/nimterop/toast.nims
    sed -E -i '3 i switch("passL", "-static-libstdc++")' vendor/nim-sqlcipher/vendor/nimterop/nimterop/toast.nims
    sed -E -i '3 i switch("passL", "-latomic")' vendor/nim-sqlcipher/vendor/nimterop/nimterop/toast.nims
    sed -E -i '3 i switch("passL", "-lc")' vendor/nim-sqlcipher/vendor/nimterop/nimterop/toast.nims

    ${createNimbleLink}
    cd vendor/nim-sqlcipher
    ${createNimbleLink}
    cd ../..


    ${applyNimOverride}


    patchShebangs .
  '';


  buildPhase = ''
    ${compilerVars}

    make V=3 SQLITE_CFLAGS="${sqliteCompilerFlags}" SQLITE_LDFLAGS="${sqliteLinkerFlags}" CFLAGS="${compilerFlags}" LDFLAGS="${linkerFlags}" CC=clang OS=${nimHostOs} USE_SYSTEM_NIM=1  PCRE_INCLUDE_DIR=${pcre}/include PCRE_LIB_DIR=${pcre}/lib SSL_INCLUDE_DIR=${openssl}/include SSL_LIB_DIR=${openssl}/lib 
   '';

  installPhase = ''
    mkdir -p $out
    cp vendor/nimbus-build-system/vendor/Nim-csources/c_code/nimbase.h $out/
    mv build/nim_status.a $out/libnim_status.a
    mv build/nim_status.h $out/
  '';
}
