{ config, stdenv, stdenvNoCC, callPackage,
  pkgs, xcodeWrapper, mkFilter, fetchurl, nodejs, bash, zlib, procps,
  status-go, projectNodePackage }:

with stdenv;

let
  src =
    let path = ./../../..;
    in builtins.path { # We use builtins.path so that we can name the resulting derivation, otherwise the name would be taken from the checkout directory, which is outside of our control
      inherit path;
      name = "status-react-npm-deps-source";
      filter =
        # Keep this filter as restrictive as possible in order to avoid unnecessary rebuilds and limit closure size
        mkFilter {
          dirRootsToInclude = [ "mobile_files" ];
          dirsToExclude = [ ".git" ".svn" "CVS" ".hg" ];
          filesToInclude = [ ".babelrc" ];
          root = path;
        };
    };

  npmDeps = callPackage ./npm-deps.nix { inherit stdenvNoCC bash zlib src projectNodePackage status-go; };

  selectedSources = [ npmDeps status-go ];

in {
  inherit xcodeWrapper;

  buildInputs = [ xcodeWrapper procps ] ++ lib.catAttrs "buildInputs" selectedSources;
  shellHook = ''
    ${lib.concatStrings (lib.catAttrs "shellHook" selectedSources)}
    
    $STATUS_REACT_HOME/nix/mobile/reset-node_modules.sh "${npmDeps.buildInputs}" && \
    $STATUS_REACT_HOME/nix/mobile/ios/fix-node_modules-permissions.sh && \
    $STATUS_REACT_HOME/nix/mobile/ios/install-pods-and-status-go.sh || \
    exit
  '';
}
