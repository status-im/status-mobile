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
    ${baseImage.shellHook}
    unset QT_PATH
  '';
}
