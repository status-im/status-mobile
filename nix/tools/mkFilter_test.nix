# This file is an example of the syntax required to call a Nix function
# and serves to test mkFilter.nix.
# 
# nix-instantiate --strict --json --eval ./mkFilter_test.nix
# [
#   {
#     "expected": true,
#     "path": "/home/pedro/src/github.com/status-im/status-react/android/1",
#     "result": true
#   },
#   {
#     "expected": true,
#     "path": "/home/pedro/src/github.com/status-im/status-react/ios",
#     "result": false
#   }
# ]

{ pkgs ? import <nixpkgs> { },
  lib ? pkgs.stdenv.lib }:

let
  mkFilter = pkgs.callPackage ./mkFilter.nix { inherit lib; };
  absPath = "/ABS/PROJECT/PATH";
  filter = mkFilter {
    dirRootsToInclude = [ "android" ];
    # dirsToExclude ? [],  # Base names of directories to exclude
    # filesToInclude ? [], # Relative path of files to include
    # filesToExclude ? [], # Relative path of files to exclude
    root = absPath;
  };
  tests = [
    {
      a = {
        path = "${absPath}/android/1";
        type = "directory";
      };
      e = true;
    }
    {
      a = {
        path = "${absPath}/ios";
        type = "directory";
      };
      e = false;
    }
  ];
  boolToString = b: if b then "true" else "false";

in builtins.map (
    t: let
      rval = (filter t.a.path t.a.type);
    in {
      path = t.a.path;
      pass = t.e == rval;
    }
  ) tests
