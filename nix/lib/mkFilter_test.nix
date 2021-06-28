# This file is an example of the syntax required to call a Nix function
# and serves to test mkFilter.nix.
# 
# nix-instantiate --strict --json --eval ./mkFilter_test.nix | jq
# [
#   {
#     "pass": true,
#     "path": "/ABS/PATH/android/subdir",
#   },
#   {
#     "pass": true,
#     "path": "/ABS/PATH/ios",
#   }
#   ...
# ]

let
  pkgs = import <nixpkgs> { };
  mkFilter = pkgs.callPackage ./mkFilter.nix { inherit (pkgs) lib; };
  absPath = "/ABS/PATH";
  filter = mkFilter {
    root = absPath;
    include = [ "android" ".*included.*" "sub/folder/.*" ];
    exclude = [ ".*excluded.*" ];
  };
  tests = [
    { path = "/WRONG/ABS/PATH"; type = "directory";
      expected = false; }
    { path = "${absPath}/.git"; type = "directory";
      expected = false; }
    { path = "${absPath}/included"; type = "file";
      expected = true; }
    { path = "${absPath}/android/included"; type = "directory";
      expected = true; }
    { path = "${absPath}/sub"; type = "file";
      expected = true; }
    { path = "${absPath}/sub/folder"; type = "file";
      expected = true; }
    { path = "${absPath}/sub/folder/file"; type = "file";
      expected = true; }
    { path = "${absPath}/sub/folder/xyz/file"; type = "file";
      expected = true; }
    { path = "${absPath}/android/sub/included"; type = "directory";
      expected = true; }
    { path = "${absPath}/android/included/excluded"; type = "directory";
      expected = false; }
    { path = "${absPath}/android/excluded"; type = "directory";
      expected = false; }
    { path = "${absPath}/ios/included"; type = "directory";
      expected = true; }
    { path = "${absPath}/ios/subfile"; type = "file";
      expected = false; }
  ];
  # make paths absolute
  testsAbs = builtins.map (
    t: t // { path = "${absPath}/${t.path}"; }
  ) tests;
in builtins.map (
    t: let
      rval = (filter t.path t.type);
    in {
      path = t.path;
      pass = t.expected == rval;
    }
  ) tests
