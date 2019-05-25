#!/usr/bin/env bash

GIT_ROOT=$(git rev-parse --show-toplevel)

rm -rf .nix-gcroots
mkdir .nix-gcroots

drv=$(nix-instantiate shell.nix)
refs=$(nix-store --query --references $drv)
nix-store -r $refs --indirect --add-root $GIT_ROOT/.nix-gcroots/shell.dep
