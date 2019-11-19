#!/usr/bin/env bash

set -e

function log() { echo "$@" 1>&2; }

# helpers for getting related paths in Nix store
function getSources()   { nix-store --query --binding src "${1}"; }
function getOutputs()   { nix-store --query --outputs "${1}"; }
function getDrvFiles()  { nix-store --query --deriver "${1}"; }
function getReferrers() { nix-store --query --referrers "${1}"; }
function getRoots()     { nix-store --query --roots "${1}"; }

function findRelated() {
    path="${1}"
    found+=("${path}")
    if [[ "${path}" =~ .*.chroot ]]; then
        log " ! Chroot:     ${path}"
        return
    elif [[ "${path}" =~ .*.lock ]]; then
        log " ! Lock:       ${path}"
        return
    elif [[ "${path}" =~ .*status-react-shell.drv ]]; then
        echo -n "${path}"
        return
    fi
    log " ? Checking:   ${path}"
    drv=$(getDrvFiles "${path}")
    # if drv is unknown-deriver then path is a source
    if [[ "${drv}" == "unknown-deriver" ]]; then
        drv=$(getReferrers "${path}" | head -n1)
        src="${path}"
    elif [[ -f "${drv}" ]]; then
        src=$(getSources "${drv}")
    fi
    # empty paths means this is a source
    if [[ -z "${drv}" ]]; then
        echo -n "${src}"
        return
    fi
    if [[ $(getRoots "${drv}" | wc -l) -eq 0 ]]; then
        log " - Derivation: ${drv}"
        log " - Source:     ${src}"
        found+=("${drv}" "${src}")
    fi

    printf '%s\n' "${found[@]}"
}

# used to find things to delete based on a regex
function findByRegex() {
    regex="${1}"

    log "Searching by regex: '${regex}'"
    # search for matching entries in the store
    drvPaths=$(
      nix-store --gc --print-dead 2> /dev/null | grep -E "${regex}"
    )

    # list of store entries to delete
    declare -a found
    
    # for each entry find the source and derivation
    for mainPath in ${drvPaths}; do
        findRelated "${mainPath}"
    done
}

# used to find things to delete based on a given path
function findByResult() {
    mainPath="${1}"
    log "Searching by result: '${mainPath}'"

    # list of store entries to delete
    declare -a found

    findRelated "${mainPath}"
}

log "Cleanup of /nix/store..."

# This is an optional CLI argument
nixResultPath="${1}"
if [[ -n "${nixResultPath}" ]]; then
    # if provided we can narrow down what to clean based on result path
    toDelete=$(findByResult "${nixResultPath}")
else 
    # use regular expression that should match all status-react build artifacts
    toDelete=$(findByRegex '.*-status-react-(shell|source|build|patched-npm-gradle-modules).*')
fi

# remove duplicates and return
toDelete=$(printf '%s\n' "${toDelete[@]}" | sort | uniq)

log "Deleting..."
nix-store --ignore-liveness --delete ${toDelete[@]}
