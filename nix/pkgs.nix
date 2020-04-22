# This file controls the pinned version of nixpkgs we use for our Nix environment
# as well as which versions of package we use, including their overrides.
{ config ? { } }:

let
  inherit (import <nixpkgs> { }) fetchFromGitHub lib;

  # For testing local version of nixpkgs
  #nixpkgsSrc = (import <nixpkgs> { }).lib.cleanSource "/home/jakubgs/work/nixpkgs";

  nixpkgsSrc = fetchFromGitHub {
    name = "nixpkgs-source";
    owner = "status-im";
    repo = "nixpkgs";
    rev = "6dacca5eb43a8bfb02fb09331df607d4465a28e9";
    sha256 = "0whwzll9lvrq4gg5j838skg7fqpvb55w4z7y44pzib32k613y2qn";
    # To get the compressed Nix sha256, use:
    # nix-prefetch-url --unpack https://github.com/${ORG}/nixpkgs/archive/${REV}.tar.gz
    # The last line will be the hash.
  };

  defaultConfig = {
    android_sdk.accept_license = true;
    # Android Env still needs old OpenSSL
    permittedInsecurePackages = [ "openssl-1.0.2u" ];
    # Override some package versions
    packageOverrides = pkgs: rec {
      # utilities
      mkFilter = import ./tools/mkFilter.nix { inherit (pkgs.stdenv) lib; };
      mkShell = import ./tools/mkShell.nix { inherit pkgs; stdenv = pkgs.stdenvNoCC; };
      mergeSh = import ./tools/mergeSh.nix { inherit (pkgs.stdenv) lib; };

      # custom packages
      openjdk = pkgs.openjdk8_headless;
      nodejs = pkgs.nodejs-12_x;
      yarn = pkgs.yarn.override { inherit nodejs; };
      go = pkgs.callPackage ./patched-go { baseGo = pkgs.go_1_14; };

      # custom builders
      buildGoPackage = pkgs.buildGo114Package.override { inherit go; };
    };
  };
  pkgs = (import nixpkgsSrc) { config = defaultConfig // config; };
in
  pkgs
