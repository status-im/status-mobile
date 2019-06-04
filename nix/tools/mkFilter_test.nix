{ pkgs ? import <nixpkgs> { },
  lib ? pkgs.stdenv.lib }:

# This file serves as an example of the syntax required to call a Nix function, and serves to test mkFilter.nix
# 
# nix-shell --show-trace nix/tools/mkFilter_test.nix
# /home/pedro/src/github.com/status-im/status-react/android/1 = true
# /home/pedro/src/github.com/status-im/status-react/ios = false

let
  mkFilter-fn = pkgs.callPackage ./mkFilter.nix { inherit lib; };
  mkFilter = mkFilter-fn {
    dirRootsToInclude = [ "android" ];
    # dirsToExclude ? [],  # Base names of directories to exclude
    # filesToInclude ? [], # Relative path of files to include
    # filesToExclude ? [], # Relative path of files to exclude
    root = "/home/pedro/src/github.com/status-im/status-react";
  };
  tests = [
    {
      path = "/home/pedro/src/github.com/status-im/status-react/android/1";
      type = "directory";
    }
    {
      path = "/home/pedro/src/github.com/status-im/status-react/ios";
      type = "directory";
    }
  ];
  boolToString = b: if b then "true" else "false";

in pkgs.mkShell {
  shellHook = (lib.concatMapStrings (t: ''
    echo "${t.path} = ${boolToString (mkFilter t.path t.type)}";
  '') tests) + ''
    exit
  '';
}
