# This file controls the pinned version of nixpkgs we use for our Nix environment
# as well as which versions of package we use, including their overrides.
{ config ? { } }:

let
  # For testing local version of nixpkgs
#  nixpkgsSrc = (import <nixpkgs> { }).lib.cleanSource "/Users/siddarthkumar/code/status-im/nixpkgs";

  # We follow the master branch of official nixpkgs.
  nixpkgsSrc = builtins.fetchTarball {
    url = "https://github.com/status-im/nixpkgs/archive/c98aece0ad1fb23e277d2c6eccf3ff5a582cacb3.tar.gz";
    sha256 = "sha256:0avkxl93r4nh754mrrgs2wvjd6fnwjnf49gf1qk5x89nypb86659";
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
