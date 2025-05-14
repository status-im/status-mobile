# This file controls the pinned version of nixpkgs we use for our Nix environment
# as well as which versions of package we use, including their overrides.
{ config ? { } }:

let
  # For testing local version of nixpkgs
  #nixpkgsSrc = (import <nixpkgs> { }).lib.cleanSource "/home/jakubgs/work/nixpkgs";

  # We follow release 24-05 of nixpkgs
  # https://github.com/NixOS/nixpkgs/releases/tag/24.05
  nixpkgsSrc = builtins.fetchTarball {
    url = "https://github.com/NixOS/nixpkgs/archive/df27247e6f3e636c119e2610bf12d38b5e98cc79.tar.gz";
    sha256 = "sha256:0bbvimk7xb7akrx106mmsiwf9nzxnssisqmqffla03zz51d0kz2n";
  };

  # FIXME: remove this additional source when nixpkgs includes gradle 8.8 in stable channel
  # We use a commit from the unstable channel of nixpkgs for gradle 8.8
  gradleNixpkgsSrc = builtins.fetchTarball {
    url = "https://github.com/NixOS/nixpkgs/archive/48d567fc7b299de90f70a48e4263e31f690ba03e.tar.gz";
    sha256 = "sha256:0zynbk52khdfhg4qfv26h3r5156xff5p0cga2cin7b07i7lqminh";
  };

  # FIXME: remove this additional source when nixpkgs includes NDK 27 in stable channel
  # We use a commit from nixpkgs that includes NDK 27.0.12077973 for Android SDK 35 compatibility
  ndkNixpkgsSrc = builtins.fetchTarball {
    url = "https://github.com/NixOS/nixpkgs/archive/0338ca8d155cfee05e50018e6c431cbe184d133f.tar.gz";
    sha256 = "1hw4ysnkp26a8kqcmq4qz9ggqsilky560xkim7xcibzzj34byhsv";
  };

  # Status specific configuration defaults
  defaultConfig = {
    android_sdk.accept_license = true;
    allowUnfree = true;
  };

  # Override some packages and utilities
  pkgsOverlay = import ./overlay.nix;

  # FIXME: remove this additional source when nixpkgs includes gradle 8.8 in stable channel
  gradleOverlay = final: prev: {
    gradle = (import gradleNixpkgsSrc {
      inherit (prev) system;
      config = defaultConfig // config;
    }).gradle_8.override { java = prev.openjdk17_headless; };
  };

  # FIXME: remove this additional source when nixpkgs includes NDK 27 in stable channel
  # Overlay to provide NDK 27 from newer nixpkgs for Android SDK 35 compatibility
  ndkOverlay = final: prev: {
    androidenv = (import ndkNixpkgsSrc {
      inherit (prev) system;
      config = defaultConfig // config;
    }).androidenv;
  };

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
    overlays = [ pkgsOverlay gradleOverlay ndkOverlay ];
  }
