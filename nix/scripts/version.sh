#!/usr/bin/env bash
# WARNING: Versions 2.14, 2.15, 2.16 have an issue creating gcroots:
# https://github.com/NixOS/nix/issues/8564
export NIX_VERSION="2.13.4"
export NIX_PACKAGE="nixVersions.nix_2_13"
export NIX_INSTALL_URL="https://nixos.org/releases/nix/nix-${NIX_VERSION}/install"
export NIX_INSTALL_SHA256="a9908cc48f5886b4f22172bdd2f9657873276fd295e78c6ed97fb308c6d284d0"
export NIX_INSTALL_PATH="/tmp/nix-install-${NIX_VERSION}"
