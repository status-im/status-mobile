#!/usr/bin/env bash

GIT_ROOT=$(cd "${BASH_SOURCE%/*}" && git rev-parse --show-toplevel)
source "${GIT_ROOT}/nix/scripts/source.sh"

if [[ ! -x "$(command -v nix-prefetch-url)" ]]; then
    echo "No 'nix-prefetch-url' utility found!" >&2
    exit 1
fi

set -ef

# urlencode <string>
urlencode() {
    old_lc_collate=$LC_COLLATE
    LC_COLLATE=C
    local length="${#1}"
    for (( i = 0; i < length; i++ )); do
        local c="${1:$i:1}"
        case $c in
            [a-zA-Z0-9.~_-]) printf '%s' "$c";;
            *)               printf '%%%02X' "'$c";;
        esac
    done
    LC_COLLATE=$old_lc_collate
}

VERSION_FILE="${GIT_ROOT}/status-go-version.json"
SCRIPT_FILE="$(basename "$0")"

STATUS_GO_REPO="${STATUS_GO_REPO:=status-go}"
STATUS_GO_OWNER="${STATUS_GO_OWNER:=status-im}"
REPO_URL="https://github.com/${STATUS_GO_OWNER}/${STATUS_GO_REPO}"
STATUS_GO_VERSION=$1

HELP_MESSAGE=$(cat <<-END
This is a tool for upgrading status-go to a given version in:
${VERSION_FILE}
Which is then used by Nix derivations to build status-go for the app.
If the given name matches both a branch and a tag the tag is used.

Usage:
    ${SCRIPT_FILE} {version}

Examples:

    # Using branch name
    ${SCRIPT_FILE} feature-abc-xyz

    # Using tag name
    ${SCRIPT_FILE} v2.1.1

    # Using commit SHA1
    ${SCRIPT_FILE} 1a2b3c4d

    # Using PR number
    ${SCRIPT_FILE} PR-2134
END
)

if [ "$1" = "-h" ] || [ "$1" = "--help" ]; then
    echo "${HELP_MESSAGE}"
    exit 1
fi

if [ $# -eq 0 ]; then
    echo "Need to provide a status-go version!"
    echo
    echo "${HELP_MESSAGE}"
    exit 1
fi

# If prefixed with # we assume argument is a PR number
if [[ "${STATUS_GO_VERSION}" = PR-* ]]; then
    STATUS_GO_VERSION="refs/pull/${STATUS_GO_VERSION#"PR-"}/head"
fi

# ls-remote finds only tags, branches, and pull requests, but can't find commits
STATUS_GO_MATCHING_REFS=$(git ls-remote ${REPO_URL} ${STATUS_GO_VERSION})

# It's possible that there's both a branch and a tag matching the given version
STATUS_GO_BRANCH_SHA1=$(echo "${STATUS_GO_MATCHING_REFS}" | grep 'refs/heads' | cut -f1)

# Prefer tag over branch if both are found
if [[ -n "${STATUS_GO_TAG_SHA1}" ]]; then
    STATUS_GO_COMMIT_SHA1="${STATUS_GO_TAG_SHA1}"
elif [[ -n "${STATUS_GO_BRANCH_SHA1}" ]]; then
    STATUS_GO_COMMIT_SHA1="${STATUS_GO_BRANCH_SHA1}"
elif [[ "${#STATUS_GO_VERSION}" -gt 4 ]]; then
    STATUS_GO_COMMIT_SHA1="${STATUS_GO_VERSION}"
else
    echo "ERROR: Input not a tag or branch, but too short to be a SHA1!" >&2
    exit 1
fi

STATUS_GO_SHA256=$(nix-prefetch-url --unpack ${REPO_URL}/archive/$(urlencode ${STATUS_GO_VERSION}).zip --name status-go-archive.zip)

cat << EOF > ${VERSION_FILE}
{
    "_comment": "THIS SHOULD NOT BE EDITED BY HAND.",
    "_comment": "Instead use: scripts/update-status-go.sh <rev>",
    "owner": "${STATUS_GO_OWNER}",
    "repo": "${STATUS_GO_REPO}",
    "version": "${STATUS_GO_VERSION}",
    "commit-sha1": "${STATUS_GO_COMMIT_SHA1}",
    "src-sha256": "${STATUS_GO_SHA256}"
}
EOF

echo "SHA-1 for ${STATUS_GO_VERSION} is ${STATUS_GO_COMMIT_SHA1}.
SHA-256 for source archive is ${STATUS_GO_SHA256}
Owner is ${STATUS_GO_OWNER}"
