{ lib, yarn2nix-moretea }:

let
  version = lib.fileContents ../../../VERSION;
  yarnLock = ../../../mobile/js_files/yarn.lock;
  packageJSON = ../../../mobile/js_files/package.json;
  packageJSONContent = lib.importJSON packageJSON;
in
  # Create a yarn package for our project that contains all the dependecies.
  yarn2nix-moretea.mkYarnModules rec {
    pname = "status-react";
    name = "${pname}-node-deps-${version}";
    inherit version packageJSON yarnLock;
  }
