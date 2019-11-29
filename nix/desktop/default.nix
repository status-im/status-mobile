{ stdenv, mkShell, callPackage, status-go,
  cmake, extra-cmake-modules, file, moreutils, go, darwin, nodejs }:

let
  inherit (stdenv.lib) catAttrs concatStrings optional unique;

  baseImageFactory = callPackage ./base-image { inherit stdenv; };
  snoreNotifySources = callPackage ./cmake/snorenotify { };
  qtkeychainSources = callPackage ./cmake/qtkeychain { };

  # main targets
  linux = callPackage ./linux { inherit stdenv status-go baseImageFactory; };
  macos = callPackage ./macos { inherit stdenv status-go darwin baseImageFactory; };
  windows = callPackage ./windows { inherit stdenv go baseImageFactory; };

  selectedSources =
    optional stdenv.isLinux linux ++
    optional stdenv.isLinux windows ++
    optional stdenv.isDarwin macos;

in rec {
  inherit linux macos windows;

  buildInputs = unique ([
    cmake
    extra-cmake-modules
    file
    moreutils
    snoreNotifySources
    qtkeychainSources
  ] ++ catAttrs "buildInputs" selectedSources);

  shell = mkShell {
    inherit buildInputs;
    shellHook = concatStrings (catAttrs "shellHook" (
      selectedSources ++ [ snoreNotifySources qtkeychainSources ]
    ));
  };
}
