# This file controls the pinned version of nixpkgs we use for our Nix environment
# as well as which versions of package we use, including their overrides.
{ config ? { } }:

let
  # For testing local version of nixpkgs
  #nixpkgsSrc = (import <nixpkgs> { }).lib.cleanSource "/home/jakubgs/work/nixpkgs";

  # using a fork of nixpkgs since we need changes from https://github.com/NixOS/nixpkgs/pull/269479
  # on top of https://github.com/NixOS/nixpkgs/releases/tag/23.11
  nixpkgsSrc = builtins.fetchTarball {
    url = "https://github.com/status-im/nixpkgs/archive/79fe42779f675c3bf12c840a2946ebdfb6bd6d96.tar.gz";
    sha256 = "sha256:1d2y2zfl0jxwz61z4p5i7srkfdlcndgsvwy46jqg16vx8jkckhiq";
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
