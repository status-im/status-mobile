{ stdenv, lib, pkgs, mkShell, callPackage
, status-go, qtkeychain-src  }:

let
  inherit (stdenv) isLinux isDarwin;
  inherit (lib) mapAttrs catAttrs optional unique mergeSh;

  # utilities
  baseImageFactory = callPackage ./base-image { };

  # main targets
  linux = callPackage ./linux { inherit status-go baseImageFactory; };
  macos = callPackage ./macos { inherit status-go baseImageFactory; };
  windows = callPackage ./windows { inherit baseImageFactory; };

  selectedSources =
    optional isLinux linux ++
    optional isLinux windows ++
    optional isDarwin macos;

  # default shell for desktop builds
  default = mkShell {
    buildInputs = with pkgs; unique ([
      file moreutils cmake
      extra-cmake-modules
      qtkeychain-src
    ] ++ (catAttrs "buildInputs" selectedSources));

    inputsFrom = [ status-go.desktop ]
      ++ (catAttrs "shell" selectedSources);

    # These variables are used by the Status Desktop CMake build script in:
    # - modules/react-native-status/desktop/CMakeLists.txt
    shellHook = ''
      export STATUS_GO_DESKTOP_INCLUDEDIR=${status-go.desktop}/include
      export STATUS_GO_DESKTOP_LIBDIR=${status-go.desktop}/lib
      # QT Keychain library sources
      export QTKEYCHAIN_SOURCES="${qtkeychain-src}/src"
    '';
  };

  # for merging default shell
  mergeDefaultShell = (key: val: { shell = mergeSh default [ val.shell ]; });

in {
  shell = default;
}
  # merge default shell with platform sub-shells
  // mapAttrs mergeDefaultShell { inherit linux windows macos; }
