#!/usr/bin/env bash

# Check if inside Nix shell
if [[ -z "${IN_NIX_SHELL}" ]]; then
    echo "ERROR: This command must be run inside a Nix shell. Please ensure you're inside a nix shell and try again."
    exit 1
fi
