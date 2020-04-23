{ stdenv, lib, mkShell, conan, nsis, go, baseImageFactory }:

assert lib.assertMsg stdenv.isLinux "Building Windows app can work only on Linux!";

let
  baseImage = baseImageFactory "windows";

in rec {
  buildInputs = lib.optionals stdenv.isLinux [
    conan
    nsis
    baseImage
    go # Needed for Windows build only
  ];

  shell = mkShell {
    inherit buildInputs;
    shellHook = ''
      ${baseImage.shellHook}
      unset QT_PATH
    '';
  };
}
