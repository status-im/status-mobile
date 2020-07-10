{ lib, callPackage, mkShell, openjdk, androidPkgs }:

let
  inherit (lib)
    catAttrs concatStrings concatStringsSep fileContents makeBinPath
    getConfig optional attrValues mapAttrs attrByPath;

  # Metadata common to all builds of status-go
  meta = {
    description = "The Status Go module that consumes go-ethereum.";
    license = lib.licenses.mpl20;
    platforms = with lib.platforms; linux ++ darwin;
  };

  # Source can be changed with a local override from config
  source = callPackage ./source.nix { };

  # Params to be set at build time, important for About section and metrics
  goBuildParams = {
    GitCommit = source.rev;
    Version = source.cleanVersion;
  };

  # These are necessary for status-go to show correct version
  paramsLdFlags = attrValues (mapAttrs (name: value:
    "-X github.com/status-im/status-go/params.${name}=${value}"
  ) goBuildParams);

  goBuildLdFlags = paramsLdFlags ++ [
    "-s" # -s disabled symbol table
    "-w" # -w disables DWARF debugging information
  ];

  goBuildFlags = [ "-v" ];

in rec {
  shared = callPackage ./shared {
    inherit meta source;
  };

  mobile = callPackage ./mobile {
    inherit meta source goBuildFlags goBuildLdFlags;
  };

  nim-status = callPackage ./nim-status {
    inherit meta source shared;
  };

  shell = mkShell {
    inputsFrom = [ mobile.android mobile.ios ];
  };
}
