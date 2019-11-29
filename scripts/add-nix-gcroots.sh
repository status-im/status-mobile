#!/usr/bin/env bash

if [[ -z "${IN_NIX_SHELL}" ]]; then
    echo "Remember to call 'make shell'!"
    exit 1
fi

set -Eeu

GIT_ROOT=$(cd "${BASH_SOURCE%/*}" && git rev-parse --show-toplevel)

rm -rf .nix-gcroots
mkdir .nix-gcroots

drv=$(nix-instantiate --argstr target all --add-root ${GIT_ROOT}/shell.nix)
refs=$(nix-store --query --references $drv)
nix-store -r $refs --indirect --add-root $GIT_ROOT/.nix-gcroots/shell.dep
