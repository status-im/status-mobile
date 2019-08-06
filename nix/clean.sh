#!/usr/bin/env bash

set -e

function getSources() {
    nix-store --query --binding src "${1}"
}

function getOutputs() {
    nix-store --query --outputs "${1}"
}

function getDrvFiles() {
    nix-store --query --deriver "${1}"
}

function getReferrers() {
    nix-store --query --referrers "${1}"
}

# list of store entries to delete
declare -a toDelete

echo "Cleanup of /nix/store after build..."

# regular expression that should match all status-react build artifacts
searchRegex='.*-status-react-(shell|source|build).*'

# search for matching entries in the store
drvPaths=$(
  find /nix/store -maxdepth 1 -type d -regextype egrep -regex "${searchRegex}"
)

# for each entry find the source and derivation
for path in ${drvPaths}; do
    toDelete+=("${path}")
    if [[ "${path}" =~ .*.chroot ]]; then
        echo " ! Chroot:    ${path}"
        continue
    fi
    echo " ? Checking:   ${path}"
    drv=$(getDrvFiles "${path}")
    # if drv is unknown-deriver then path is a source
    if [[ "${drv}" == "unknown-deriver" ]]; then
        drv=$(getReferrers "${path}")
        src="${path}"
    elif [[ -f "${drv}" ]]; then
        src=$(getSources "${drv}")
    fi
    echo " - Derivation: ${drv}"
    echo " - Source:     ${src}"

    toDelete+=("${drv}" "${src}")
done

# remove dupicates
cleanToDelete=$(echo "${toDelete[@]}" | sort | uniq)

echo "Deleting..."
nix-store --delete ${cleanToDelete[@]}
