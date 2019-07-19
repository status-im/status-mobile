{ stdenv, callPackage,
  conan, nsis, go }:

assert stdenv.isLinux;

let
  baseImage = callPackage ./base-image { };

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
