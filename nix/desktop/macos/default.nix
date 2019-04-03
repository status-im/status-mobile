{ stdenv, pkgs }:

with pkgs;
with stdenv;

let
  baseImage = callPackage ./base-image { };

in
{
  buildInputs = [ baseImage ];

  shellHook = ''
    export STATUSREACT_MACOS_BASEIMAGE_PATH="${baseImage}/src"
  '';
}
