# This file controls the pinned version of nixpkgs we use for our Nix environment
# as well as which versions of package we use, including their overrides.
{ config ? { } }:

let
  inherit (import <nixpkgs> { }) fetchFromGitHub;
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
  defaultConfig = {
    android_sdk.accept_license = true;
    # Override some package versions
    packageOverrides = pkgs: rec {
      # utilities
      mkFilter = import ./tools/mkFilter.nix { inherit (pkgs.stdenv) lib; };
      mkShell = import ./tools/mkShell.nix { inherit pkgs; stdenv = pkgs.stdenvNoCC; };
      mergeSh = import ./tools/mergeSh.nix { inherit (pkgs.stdenv) lib; };

      # custom packages
      nodejs = pkgs.nodejs-10_x;
      yarn = pkgs.yarn.override { inherit nodejs; };
      clojure = pkgs.clojure.overrideAttrs (old: rec { version = "1.10.0.411"; });
      go = pkgs.callPackage ./patched-go { baseGo = pkgs.go_1_13; };

      # custom builders
      buildGoPackage = pkgs.buildGoPackage.override { inherit go; };
    };
  };
  pkgs = (import (fetchFromGitHub nixpkgsSrc)) { config = defaultConfig // config; };
in
  pkgs
