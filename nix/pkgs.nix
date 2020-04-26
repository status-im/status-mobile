# This file controls the pinned version of nixpkgs we use for our Nix environment
# as well as which versions of package we use, including their overrides.
{ config ? { } }:

let
  inherit (import <nixpkgs> { }) fetchFromGitHub;

  # For testing local version of nixpkgs
  #nixpkgsSrc = (import <nixpkgs> { }).lib.cleanSource "/home/jakubgs/work/nixpkgs";

  # Our own nixpkgs fork with custom fixes
  nixpkgsSrc = fetchFromGitHub {
    name = "nixpkgs-source";
    owner = "status-im";
    repo = "nixpkgs";
    rev = "6dacca5eb43a8bfb02fb09331df607d4465a28e9";
    sha256 = "0whwzll9lvrq4gg5j838skg7fqpvb55w4z7y44pzib32k613y2qn";
    # To get the compressed Nix sha256, use:
    # nix-prefetch-url --unpack https://github.com/${ORG}/nixpkgs/archive/${REV}.tar.gz
  };

  # Override some packages and utilities
  pkgsOverlay = import ./overlay.nix;

  # we want these packages easily available via callPackage
  defaultConfig = {
    android_sdk.accept_license = true;
    # Android Env still needs old OpenSSL
    permittedInsecurePackages = [ "openssl-1.0.2u" ];
  };

in
  # import nixpkgs with a config override
  (import nixpkgsSrc) {
    config = defaultConfig // config;
    overlays = [ pkgsOverlay ];
  }
