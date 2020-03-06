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

KEYSTORE_PASSWORD=$(property_gradle 'KEYSTORE_PASSWORD')
KEYSTORE_ALIAS=$(property_gradle 'KEYSTORE_ALIAS')
KEYSTORE_KEY_PASSWORD=$(property_gradle 'KEYSTORE_KEY_PASSWORD')

# Allow Makefile modify Keystore path if necessary
if [[ -z ${KEYSTORE_PATH} ]]; then
    KEYSTORE_PATH=$(property_gradle 'KEYSTORE_FILE')
fi
# Replace ~ with proper absolute path
KEYSTORE_PATH=${KEYSTORE_PATH/#\~/$HOME}

if [[ -e "${KEYSTORE_PATH}" ]]; then
    echo -e "${YLW}Keystore file already exists:${RST} ${KEYSTORE_PATH}" > /dev/stderr
    echo "${KEYSTORE_PATH}"
    exit 0
fi

KEYSTORE_DIR=$(dirname "${S/TATUS_RELEASE_STORE_FILE}")
[[ -d $KEYSTORE_DIR ]] || mkdir -p $KEYSTORE_DIR

echo -e "${GRN}Generating keystore...${RST}" > /dev/stderr

keytool -genkey -v \
    -keyalg RSA \
    -keysize 2048 \
    -validity 10000 \
    -dname "CN=, OU=, O=, L=, S=, C=" \
    -keystore "${KEYSTORE_PATH}" \
    -alias "${KEYSTORE_ALIAS}" \
    -storepass "${KEYSTORE_PASSWORD}" \
    -keypass "${KEYSTORE_KEY_PASSWORD}" \
    > /dev/stderr

echo "${KEYSTORE_PATH}"
