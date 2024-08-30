# This file controls the pinned version of nixpkgs we use for our Nix environment
# as well as which versions of package we use, including their overrides.
{ config ? { } }:

let
  # For testing local version of nixpkgs
  #nixpkgsSrc = (import <nixpkgs> { }).lib.cleanSource "/home/jakubgs/work/nixpkgs";

  # We follow release 24-05 of nixpkgs
  # https://github.com/NixOS/nixpkgs/releases/tag/24.05
  nixpkgsSrc = builtins.fetchTarball {
    url = "https://github.com/NixOS/nixpkgs/archive/a72e2a58d01698b77dd373f598c776a53090e97d.tar.gz";
    sha256 = "sha256:0g7byc7kk2sp3irrq510mx2ddf0l101wb1xfm86v3p6z5n7y0ag1";
  };

  # Status specific configuration defaults
  defaultConfig = {
    android_sdk.accept_license = true;
    allowUnfree = true;
  };

  # Override some packages and utilities
  pkgsOverlay = import ./overlay.nix;

  # Fix for lack of Android SDK for M1 Macs.
  systemOverride = let
    inherit (builtins) currentSystem getEnv;
    envSystemOverride = getEnv "NIXPKGS_SYSTEM_OVERRIDE";
  in
    if envSystemOverride != "" then
      envSystemOverride
    else
      currentSystem;
in
  # import nixpkgs with a config override
  (import nixpkgsSrc) {
    config = defaultConfig // config;
    system = systemOverride;
    overlays = [ pkgsOverlay ];
  }
