#!/usr/bin/env bash

set -e

KEYCHAIN="$1"

if [ $# -ne 1 ]; then
  echo -e "encrypt-keychain.sh <path to keychain>"
  exit 10
elif [ ! -f "$KEYCHAIN" ]; then
  echo -e "Keychain file does not exist."
  exit 20
fi

INNER_CIPHER="CAMELLIA256"
OUTER_CIPHER="AES256"

echo -e "\n### Double-encrypting ${KEYCHAIN}. Enter first inner, then outer passphrase..."
gpg --symmetric --cipher-algo "$INNER_CIPHER" --s2k-digest-algo SHA256 --s2k-count 65011712 "${KEYCHAIN}"
gpg --symmetric --cipher-algo "$OUTER_CIPHER" --s2k-digest-algo SHA256 --s2k-count 65011712 "${KEYCHAIN}.gpg"
mv -f "${KEYCHAIN}.gpg.gpg" "${KEYCHAIN}.gpg"

