{ stdenv, mkShell, conan, nsis, go, baseImageFactory }:

assert stdenv.isLinux;

let
  baseImage = baseImageFactory "windows";

in rec {
  buildInputs = stdenv.lib.optionals stdenv.isLinux [
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
