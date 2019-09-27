# This file controls the pinned version of nixpkgs we use for our Nix environment
{ config ? { android_sdk.accept_license = true; },
  pkgs ? (import ((import <nixpkgs> { }).fetchFromGitHub {
    name = "nixpkgs-source";
    owner = "status-im";
    repo = "nixpkgs";
    rev = "f54e1c6441868450e947f6171b129dfb90a91653";
    sha256 = "13cf3pi277ifg57l11kmjk08vrpv6km2jfxq7sp63zxw5bgkiqpd";
    # To get the compressed Nix sha256, use:
    # nix-prefetch-url --unpack https://github.com/${ORG}/nixpkgs/archive/${REV}.tar.gz
    # The last line will be the hash.
  })) { inherit config; } }:

  {
    inherit pkgs config;
  }
