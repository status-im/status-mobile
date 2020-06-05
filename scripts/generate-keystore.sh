#!/usr/bin/env bash

set -ef pipefail

GIT_ROOT=$(cd "${BASH_SOURCE%/*}" && git rev-parse --show-toplevel)
source "${GIT_ROOT}/scripts/colors.sh"

function property() {
    grep "${2}" "${1}" | cut -d'=' -f2
}

function gradle_property() {
    property ${GIT_ROOT}/android/gradle.properties ${1}
}

function env_var_or_gradle_prop() {
    VAR_NAME="${1}"
    if [[ -n "${!VAR_NAME}" ]]; then
        echo "${!VAR_NAME}"
    else
        gradle_property "${VAR_NAME}"
    fi
}

KEYSTORE_PASSWORD=$(env_var_or_gradle_prop 'KEYSTORE_PASSWORD')
KEYSTORE_ALIAS=$(env_var_or_gradle_prop 'KEYSTORE_ALIAS')
KEYSTORE_KEY_PASSWORD=$(env_var_or_gradle_prop 'KEYSTORE_KEY_PASSWORD')
KEYSTORE_PATH=$(env_var_or_gradle_prop 'KEYSTORE_PATH')

# Use absolute path for Keystore
KEYSTORE_PATH=${KEYSTORE_PATH/#\~/$HOME}

if [[ -e "${KEYSTORE_PATH}" ]]; then
    echo -e "${YLW}Keystore file already exists:${RST} ${KEYSTORE_PATH}" >&2
    echo "${KEYSTORE_PATH}"
    exit 0
fi

KEYSTORE_DIR=$(dirname "${KEYSTORE_PATH}")
[[ -d $KEYSTORE_DIR ]] || mkdir -p $KEYSTORE_DIR

echo -e "${GRN}Generating keystore...${RST}" >&2

keytool -genkey -v \
    -keyalg RSA \
    -keysize 2048 \
    -validity 10000 \
    -deststoretype pkcs12 \
    -dname "CN=, OU=, O=, L=, S=, C=" \
    -keystore "${KEYSTORE_PATH}" \
    -alias "${KEYSTORE_ALIAS}" \
    -storepass "${KEYSTORE_PASSWORD}" \
    -keypass "${KEYSTORE_KEY_PASSWORD}" \
    > /dev/stderr

echo "${KEYSTORE_PATH}"
