{ pkgs, stdenv, lib, fetchFromGitHub
# Dependencies
, xcodeWrapper
, writeScript
, androidPkgs
, newScope
, git 
, platform ? "android"
, arch ? "386"
, api ? "23" } :


let
  callPackage = newScope {};
  src = pkgs.fetchgit {
    url = "https://github.com/status-im/nim-status";
    rev = "918a3d4389d58ea8b5de85fc9fb5c8c704c63659";
    sha256 = "0p3s65dkxphcpkz6hnacfd7l5aj9b80mcgcmlqjfz4121dggyhai";
    fetchSubmodules = false;
  };

  flags = callPackage ./getFlags.nix {platform = platform; arch = arch;};
in 
  stdenv.mkDerivation rec {
  name = "nim-status-go_lib";
  inherit src;
  buildInputs = with pkgs; [ nim ];

  phases = ["unpackPhase" "preBuildPhase" "buildPhase" "installPhase"];

  preBuildPhase = ''
    echo 'switch("passC", "${flags.compiler}")' >> config.nims
    echo 'switch("passL", "${flags.linker}")' >> config.nims
    echo 'switch("cpu", "${flags.nimCpu}")' >> config.nims
    echo 'switch("os", "${flags.nimPlatform}")' >> config.nims
  '';

  buildPhase = ''
    ${flags.vars}
    echo -e "Building Nim-Status"
    nim c \
      --app:staticLib \
      --header \
      --nimcache:nimcache/nim_status_go \
      --noMain \
      --threads:on \
      --tlsEmulation:off \
      -o:nim_status_go \
      nim_status/c/go/shim.nim
  '';

  installPhase = ''
    mkdir $out
    cp nimcache/nim_status_go/shim.h $out/nim_status_go.h
    mv nim_status_go.a $out/
  '';
}
