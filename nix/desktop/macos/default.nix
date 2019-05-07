{ stdenv, pkgs }:

with pkgs;
with stdenv;
with darwin.apple_sdk.frameworks;

assert isDarwin;

let
  baseImage = callPackage ./base-image { };

in
{
  buildInputs = [ baseImage ] ++
    [ AppKit Cocoa darwin.cf-private Foundation OpenGL ];

  shellHook = ''
    ${baseImage.shellHook}
    export NIX_TARGET_LDFLAGS="-F${CoreFoundation}/Library/Frameworks -framework CoreFoundation $NIX_TARGET_LDFLAGS"
  '';
}
