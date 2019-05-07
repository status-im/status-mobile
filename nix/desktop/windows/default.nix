{ stdenv, pkgs }:

with pkgs;
with stdenv;

assert isLinux;

let
  baseImage = callPackage ./base-image { };

in
{
  buildInputs = lib.optionals isLinux [
    conan
    nsis
    baseImage
    go # Needed for Windows build only
  ];

  shellHook = ''
    export STATUSREACT_WINDOWS_BASEIMAGE_PATH="${baseImage}/src"
    unset QT_PATH
  '';
}
