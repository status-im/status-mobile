{ stdenv, callPackage, pkgs,
  darwin, qt5, status-go, baseImageFactory }:

with darwin.apple_sdk.frameworks;

assert stdenv.isDarwin;

let
  inherit (stdenv.lib) concatStrings catAttrs;
  baseImage = baseImageFactory "macos";

in {
  buildInputs = [
    pkgs.gnupg22
    baseImage
    qt5.full
    AppKit
    Cocoa
    darwin.cf-private
    Foundation
    OpenGL
  ] ++ status-go.buildInputs;

  shellHook =
    concatStrings (catAttrs "shellHook" [ baseImage status-go ] ) + ''
      export NIX_TARGET_LDFLAGS="-F${CoreFoundation}/Library/Frameworks -framework CoreFoundation $NIX_TARGET_LDFLAGS"
      export QT_PATH="${qt5.full}"
      export QT_BASEBIN_PATH="${qt5.qtbase.bin}"
      export PATH="${qt5.full}/bin:$PATH"
    '';
}
