# This file controls the pinned version of nixpkgs we use for our Nix environment
let
  inherit (import <nixpkgs> { }) fetchFromGitHub;
  defaultConfig = { android_sdk.accept_license = true; };
  nixpkgsSrc = {
    name = "nixpkgs-source";
    owner = "status-im";
    repo = "nixpkgs";
    rev = "d9b2c6b8add87098adb8ee34042e39d15f3658c4";
    sha256 = "19wnp97nlkyd825pjd9vm9rngy3gcil9bwqncwscivsmaz5c7l37";
    # To get the compressed Nix sha256, use:
    # nix-prefetch-url --unpack https://github.com/${ORG}/nixpkgs/archive/${REV}.tar.gz
    # The last line will be the hash.
  };

in {
  config ? { },
  config' ? defaultConfig // config,
  pkgs ? (import (fetchFromGitHub nixpkgsSrc)) { config = config'; }
}:

{
  inherit pkgs;
  config = config';
}
