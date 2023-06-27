#!/usr/bin/env bash

# This script is used by the Makefile to have an implicit nix-shell.
# The following environment variables modify the script behavior:
# - TARGET: This attribute is passed via --attr to Nix, defining the scope.
# - _NIX_PURE: This variable allows for making the shell pure with the use of --pure.
#     Take note that this makes Nix tools like `nix-build` unavailable in the shell.
# - _NIX_KEEP: This variable allows specifying which env vars to keep for Nix pure shell.

GIT_ROOT=$(cd "${BASH_SOURCE%/*}" && git rev-parse --show-toplevel)
source "${GIT_ROOT}/scripts/colors.sh"
source "${GIT_ROOT}/nix/scripts/source.sh"

export TERM=xterm # fix for colors
shift # we remove the first -c from arguments

if [[ -z "${TARGET}" ]]; then
    export TARGET="default"
    echo -e "${YLW}Missing TARGET, assuming default target.${RST} See nix/README.md for more details." 1>&2
fi

# Minimal shell with just Nix sourced, useful for `make nix-gc`.
if [[ "${TARGET}" == "nix" ]]; then
    eval $@
    exit 0
fi
if [[ -n "${IN_NIX_SHELL}" ]] && [[ -n "${NIX_SHELL_TARGET}" ]]; then
    if [[ "${NIX_SHELL_TARGET}" == "${TARGET}" ]]; then
        echo -e "${YLW}Nix shell for TARGET=${TARGET} is already active.${RST}" >&2
        exit 0
    else
        # Nesting nix shells does not work due to how we detect already present shell.
        echo -e "${RED}Cannot nest Nix shells with different targets!${RST}" >&2
        exit 1
    fi
fi

entryPoint="default.nix"
nixArgs=(
    "--show-trace"
    "--attr shells.${TARGET}"
)

# This variable allows specifying which env vars to keep for Nix pure shell
# The separator is a colon
if [[ -n "${_NIX_KEEP}" ]]; then
    nixArgs+=("--keep ${_NIX_KEEP//,/ --keep }")
fi

# Not all builds are ready to be run in a pure environment
if [[ -n "${_NIX_PURE}" ]]; then
    nixArgs+=("--pure")
    pureDesc='pure '
fi

# Hack fix for missing Android SDK for aarch64 on Darwin. See systemOverride in `nix/pkgs.nix`.
if [[ "${TARGET}" =~ ^(android-sdk|android|gradle|keytool|status-go)$ ]]; then
    os=$(uname -s | tr '[:upper:]' '[:lower:]')
    export NIXPKGS_SYSTEM_OVERRIDE="x86_64-${os}"
fi

echo -e "${GRN}Configuring ${pureDesc}Nix shell for target '${TARGET}'...${RST}" 1>&2

# Save derivation from being garbage collected
"${GIT_ROOT}/nix/scripts/gcroots.sh" "shells.${TARGET}"

# ENTER_NIX_SHELL is the fake command used when `make shell` is run.
# It is just a special string, not a variable, and a marker to not use `--run`.
if [[ "${@}" == "ENTER_NIX_SHELL" ]]; then
    export NIX_SHELL_TARGET="${TARGET}"
    exec nix-shell ${nixArgs[@]} --keep NIX_SHELL_TARGET ${entryPoint}
else
    exec nix-shell ${nixArgs[@]} --run "$@" ${entryPoint}
fi
