# This file controls the pinned version of nixpkgs we use for our Nix environment
{ config ? { android_sdk.accept_license = true; },
  pkgs ? (import ((import <nixpkgs> { }).fetchFromGitHub {
    owner = "status-im";
    repo = "nixpkgs";
    rev = "db492b61572251c2866f6b5e6e94e9d70e7d3021";
    sha256 = "188r7gbcrxi20nj6xh9bmdf3lbjwb94v9s0wpacl7q39g1fca66h";
    name = "nixpkgs-source";
  })) { inherit config; } }:

  {
    inherit pkgs config;
  }
