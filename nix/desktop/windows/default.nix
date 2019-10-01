{ stdenv, callPackage,
  conan, nsis, go, baseImageFactory }:

assert stdenv.isLinux;

let
  baseImage = baseImageFactory "windows";

in {
  buildInputs = stdenv.lib.optionals stdenv.isLinux [
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
