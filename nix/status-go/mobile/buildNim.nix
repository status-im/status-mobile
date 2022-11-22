{ pkgs, stdenv, lib, fetchFromGitHub
# Dependencies
, xcodeWrapper
, srcRaw
, writeScript
, writeTextFile
, androidPkgs
, git 
, platform ? "android"
, arch ? "386"
, api ? "29" }:

let
  osId = builtins.elemAt (builtins.split "\-" stdenv.hostPlatform.system) 2;
  osArch = builtins.elemAt (builtins.split "\-" stdenv.hostPlatform.system) 0;

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
  pname = "nimCompiler";
  name = "nimCompiler";
  src = srcRaw.src;
  #version = lib.strings.substring 0 7 src.rev;
  buildInputs = with pkgs; [ wget git clang which tcl cmake];

  phases = [ "unpackPhase" "buildPhase" "installPhase" ];


  buildPhase = ''
    ${createNimbleLink}
    export HOME=$PWD
    make V=3 CC=clang CXX=clang build-nim
   '';

  installPhase = ''
    mkdir -p $out
    cp vendor/nimbus-build-system/vendor/Nim/bin/* $out/
  '';
}

