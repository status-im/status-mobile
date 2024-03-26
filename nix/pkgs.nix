# This file controls the pinned version of nixpkgs we use for our Nix environment
# as well as which versions of package we use, including their overrides.
{ config ? { } }:

let
  # For testing local version of nixpkgs
  #nixpkgsSrc = (import <nixpkgs> { }).lib.cleanSource "/home/jakubgs/work/nixpkgs";

  # We follow a fork of nixpkgs where we bump gradle to 8.3 and update build tools
  nixpkgsSrc = builtins.fetchTarball {
    url = "https://github.com/status-im/nixpkgs/archive/44d8b23ebbe40ed16ef1aecef6c5354657c48b19.tar.gz";
    sha256 = "sha256:0s7kjnc8drchzp268yzbv5snc4702hswcns9hrncjq4im8cv6mf9";
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
