#!/usr/bin/env bash

set -euf pipefail

TARGET=${1:-debug}

CURRENT_DIR="$( cd "$( dirname "$0" )" && pwd )"
. "$CURRENT_DIR/lib/setup/path-support.sh"
source_lib "properties.sh"

STORE_FILE=$(property_gradle 'STATUS_RELEASE_STORE_FILE')
STORE_FILE="${STORE_FILE/#\~/$HOME}"
STATUS_RELEASE_STORE_PASSWORD=$(property_gradle 'STATUS_RELEASE_STORE_PASSWORD')
STATUS_RELEASE_KEY_ALIAS=$(property_gradle 'STATUS_RELEASE_KEY_ALIAS')
STATUS_RELEASE_KEY_PASSWORD=$(property_gradle 'STATUS_RELEASE_KEY_PASSWORD')

[[ -e "$STORE_FILE" ]] && echo "Keystore $STORE_FILE already exists, please manually remove it if you want to regenerate." && exit 0

echo "Generating keystore $STORE_FILE"
keydirname="$( dirname "$STORE_FILE" )"
[ -d $keydirname ] || mkdir -p $keydirname
keytool -genkey -v -keystore ${STORE_FILE}  -keyalg RSA -keysize 2048 -validity 10000 -alias ${STATUS_RELEASE_KEY_ALIAS} \
        -storepass ${STATUS_RELEASE_STORE_PASSWORD} -keypass ${STATUS_RELEASE_KEY_PASSWORD} -dname "CN=, OU=, O=, L=, S=, C="