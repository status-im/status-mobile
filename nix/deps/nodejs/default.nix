{ lib, yarn2nix-moretea }:

# Create a yarn package for our project that contains all the dependecies.
yarn2nix-moretea.mkYarnModules rec {
  pname = "status-react";
  name = "${pname}-node-deps-${version}";
  version = lib.fileContents ../../../VERSION;
  yarnLock = ../../../yarn.lock;
  packageJSON = ../../../package.json;
}
