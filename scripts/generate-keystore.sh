#!/usr/bin/env bash

set -euf pipefail

GIT_ROOT=$(cd "${BASH_SOURCE%/*}" && git rev-parse --show-toplevel)
source "${GIT_ROOT}/scripts/colors.sh"

function property() {
    grep "${2}" "${1}" | cut -d'=' -f2
}

function property_gradle() {
    property ${GIT_ROOT}/android/gradle.properties ${1}
}

TARGET=${1:-debug}

CURRENT_DIR=$(cd "$(dirname "$0")" && pwd)
KEYSTORE_FILE=$(property_gradle 'STATUS_RELEASE_STORE_FILE')
KEYSTORE_FILE=${KEYSTORE_FILE/#\~/$HOME}
STATUS_RELEASE_STORE_PASSWORD=$(property_gradle 'STATUS_RELEASE_STORE_PASSWORD')
STATUS_RELEASE_KEY_ALIAS=$(property_gradle 'STATUS_RELEASE_KEY_ALIAS')
STATUS_RELEASE_KEY_PASSWORD=$(property_gradle 'STATUS_RELEASE_KEY_PASSWORD')

if [[ -e "${KEYSTORE_FILE}" ]]; then
    echo -e "${YLW}Keystore file already exists:${RST} ${KEYSTORE_FILE}" > /dev/stderr
    exit 0
fi

KEYSTORE_DIR=$(dirname "${KEYSTORE_FILE}")
[[ -d $KEYSTORE_DIR ]] || mkdir -p $KEYSTORE_DIR

echo "Generating keystore ${KEYSTORE_FILE}" > /dev/stderr

keytool -genkey -v \
    -keyalg RSA \
    -keysize 2048 \
    -validity 10000 \
    -dname "CN=, OU=, O=, L=, S=, C="
    -keystore "${KEYSTORE_FILE}" \
    -alias "${STATUS_RELEASE_KEY_ALIAS}" \
    -storepass "${STATUS_RELEASE_STORE_PASSWORD}" \
    -keypass "${STATUS_RELEASE_KEY_PASSWORD}" \
    > /dev/stderr

echo "${KEYSTORE_FILE}"
