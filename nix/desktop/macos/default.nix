{ stdenv, lib, callPackage, mkShell,
  gnupg22, darwin, qt5custom, status-go, baseImageFactory }:

assert lib.assertMsg stdenv.isDarwin "Building MacOS app can work only on MacOS!";

let
  inherit (lib) concatStrings catAttrs;
  inherit (darwin.apple_sdk.frameworks)
    AppKit Cocoa Foundation OpenGL CoreFoundation;

  baseImage = baseImageFactory "macos";

in {
  shell = mkShell {
    buildInputs = [
      gnupg22 baseImage qt5custom
      darwin.cf-private
      AppKit Cocoa Foundation OpenGL
    ];

    inputsFrom = [
      status-go baseImage
    ];

    shellHook = ''
      export NIX_TARGET_LDFLAGS="-F${CoreFoundation}/Library/Frameworks -framework CoreFoundation $NIX_TARGET_LDFLAGS"
      export QT_PATH="${qt5custom}"
      export QT_BASEBIN_PATH="${qt5custom}/bin"
      export PATH="$QT_BASEBIN_PATH/bin:$PATH"
    '';
  };
}
