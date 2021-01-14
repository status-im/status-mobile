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
    rev = "3355743cfd8d89cfa8d3b8e6d40c48e62e72d36a";
    sha256 = "1l7559m3xkzwkm202rkjq3a8cz52k7z9vxn9y2adc30cp197wc82";
    # To get the compressed Nix sha256, use:
    # nix-prefetch-url --unpack https://github.com/${ORG}/nixpkgs/archive/${REV}.tar.gz
  };

  # Status specific configuration defaults
  defaultConfig = import ./config.nix;

  # Override some packages and utilities
  pkgsOverlay = import ./overlay.nix;
in
  # import nixpkgs with a config override
  (import nixpkgsSrc) {
    config = defaultConfig // config;
    overlays = [ pkgsOverlay ];
  }
