{ stdenv, lib
# Dependencies
, xcodeWrapper
, androidPkgs
, platform ? "android"
, arch ? "386"
, fromNim ? false
, api ? "23" } :


let

  #
  # iOS-specific
  #

  iosSdk = if arch == "386" then "iphonesimulator" else "iphoneos";

  # Used for the -arch parameter
  # passed to clang during iOS builds
  iosArch = lib.getAttr arch {
    "386" = "x86_64";
    "arm" = "armv7";
    "arm64" = "arm64";
  };

  isIOS = platform == "ios";
  iosToolPath = "${xcodeWrapper}/bin";


  #
  # Android-specific
  #
  ANDROID_NDK_HOME = "${androidPkgs}/ndk-bundle";
  
  isAndroid = lib.hasPrefix "android" platform;

  # Specify host system in order to pick proper toolchain during Android compilation
  osId = builtins.elemAt (builtins.split "\-" stdenv.hostPlatform.system) 2;
  osArch = builtins.elemAt (builtins.split "\-" stdenv.hostPlatform.system) 0;

  # Used for the -target parameter
  # passed to clang during Android builds
  androidTargetArch = lib.getAttr arch {
    "386" = "i686";
    "arm" = "arm";
    "arm64" = "aarch64";
  };
  androidTarget = "${if arch == "arm" then "armv7a" else androidTargetArch}-linux-${platform}";


  androidToolPath = "${ANDROID_NDK_HOME}/toolchains/llvm/prebuilt/${osId}-${osArch}/bin";
  androidToolPathPrefix = "${androidToolPath}/${androidTargetArch}-linux-${platform}";


  #
  # Compiler/linker flags
  #
  isysroot = if isAndroid then 
    "${ANDROID_NDK_HOME}/toolchains/llvm/prebuilt/${osId}-${osArch}/sysroot"             
    else "$(xcrun --sdk ${iosSdk} --show-sdk-path)";

  compilerFlags = if isAndroid then
      "-isysroot ${isysroot} -target ${androidTarget}${api} -fPIC"
      else if isIOS then
      # TODO The conditional for -miphoneos-version-min=8.0 is required,
      # otherwise Nim will complain that thread-local storage is not supported for the current target
      # when expanding 'NIM_THREADVAR' macro

      "-isysroot ${isysroot} -fembed-bitcode -arch ${iosArch} ${if fromNim && arch == "arm" then "" else "-m${iosSdk}-version-min=8.0"}"
      else throw "Unsupported platform!";

  linkerFlags = if isAndroid then  
  "--sysroot ${isysroot} -target ${androidTarget}${api}"
  else if isIOS then
  "--sysroot ${isysroot} -fembed-bitcode -arch ${iosArch} ${if fromNim && arch == "arm" then "" else "-m${iosSdk}-version-min=8.0"}"
  else throw "Unsupported platform!";

  compilerVars = if isAndroid then
    ''
      export PATH=${androidToolPath}:$PATH
      export AR=${androidToolPathPrefix}-ar
      export RANLIB=${androidToolPathPrefix}-ranlib
      # go build will use this
      export CC=${androidToolPath}/clang

      # This is important, otherwise Nim might not use proper tooling
      mkdir bin
      ln -s $AR bin/ar
      ln -s $RANLIB bin/ranlib
      export PATH=`pwd`/bin:$PATH
    ''
    else if isIOS then
    ''
      export PATH=${iosToolPath}:$PATH
    ''
    else throw "Unsupported platform!";

  toolPath = if isAndroid then androidToolPath else iosToolPath;


  #
  # Nim-specific
  #

  # Arg arch -> Nim arch
  nimCpuMap = {
    "386" = "i386";
    "x86_64" = "amd64"; 
    "arm" = "arm"; 
    "arm64" = "arm64";
  };

  nimCpu = if arch=="386" && platform == "ios" then "amd64" else "${lib.getAttr arch nimCpuMap}";
  nimPlatform = "${(if platform == "ios" then "ios" else "android")}";

in {
  "compiler" = compilerFlags;
  "linker" = linkerFlags;
  "vars" = compilerVars;
  "isysroot" = isysroot;
  "toolPath" = toolPath;
  "nimCpu" = nimCpu;
  "nimPlatform" = nimPlatform;
}
