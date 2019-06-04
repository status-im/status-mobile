#!/usr/bin/env bash

set -Eeu

. ~/.nix-profile/etc/profile.d/nix.sh

GIT_ROOT=$(cd "${BASH_SOURCE%/*}" && git rev-parse --show-toplevel)

rm -rf .nix-gcroots
mkdir .nix-gcroots

drv=$(nix-instantiate ${GIT_ROOT}/shell.nix)
refs=$(nix-store --query --references $drv)
nix-store -r $refs --indirect --add-root $GIT_ROOT/.nix-gcroots/shell.dep
