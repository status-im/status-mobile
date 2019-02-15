{ stdenv, pkgs }:

with pkgs;
with stdenv; 

{
  buildInputs = lib.optional isLinux [ conan nsis ];
}
