#!/usr/bin/env bash

DEV_ID="Developer ID Application: STATUS HOLDINGS PTE. LTD. (DTX7Z4U3YA)"

OBJECT="$1"
GPG_ENCRYPTED_KEYCHAIN="$2"

if [ `uname` != 'Darwin' ]; then
  echo "This only works on macOS."
  exit 1
elif [ $# -ne 2 ]; then
  echo "sign-macos-bundle.sh <path to .app bundle or .dmg image> <path to encrypted keychain>"
  exit 1
elif [ ! -e "$OBJECT" ]; then
  echo "Object does not exist."
  exit 1
elif [ ! -f "$GPG_ENCRYPTED_KEYCHAIN" ]; then
  echo "Encrypted keychain file does not exist."
  exit 1
fi

# Required env variables:

export GPG_PASS_OUTER
export GPG_PASS_INNER
export KEYCHAIN_PASS

[ -z "$GPG_PASS_OUTER" ] && echo 'Missing env var: GPG_PASS_OUTER' && exit 1
[ -z "$GPG_PASS_INNER" ] && echo 'Missing env var: GPG_PASS_INNER' && exit 1
[ -z "$KEYCHAIN_PASS" ] && echo 'Missing env var: KEYCHAIN_PASS' && exit 1

# If GPG hasn't been run on this host before, we run it once
# quietly to make sure it creates the directories it needs first
# and doesn't trip when trying to do the decryption further down
script -q /dev/null gpg < /dev/null > /dev/null

set -e

echo -e "\n### Storing original keychain search list..."
ORIG_KEYCHAIN_LIST="$(security list-keychains \
  | grep -v "/Library/Keychains/System.keychain" | xargs)"

echo -e "\n### Creating ramdisk..."
RAMDISK="$(hdiutil attach -nomount ram://20480 | tr -d '[:blank:]')"
MOUNTPOINT="$(mktemp -d)"
KEYCHAIN="${MOUNTPOINT}/macos-developer-id.keychain-db"

function clean_up {
  local STATUS=$?

  set +e

  if [ $STATUS -eq 0 ]; then
    echo -e "\n###### DONE."
  else
    echo -e "\n###### ERROR. See above for details."
  fi

  echo -e "\n###### Cleaning up..."

  echo -e "\n### Locking keychain..."
  security lock-keychain "$KEYCHAIN"

  echo -e "\n### Restoring original keychain search list..."
  security list-keychains -s $ORIG_KEYCHAIN_LIST
  security list-keychains

  echo -e "\n### Wiping keychain file..."
  rm -P "$KEYCHAIN"

  echo -e "\n### Destroying ramdisk..."
  diskutil umount force "$RAMDISK"
  diskutil eject "$RAMDISK"

  exit $STATUS
}

trap clean_up EXIT

echo -e "\n### Formatting and mounting ramdisk..."
newfs_hfs "$RAMDISK"
mount -t hfs "$RAMDISK" "$MOUNTPOINT"

echo -e "\n### Decrypting keychain to $KEYCHAIN ..."
gpg --batch --passphrase "$GPG_PASS_OUTER" --pinentry-mode loopback \
  --decrypt "$GPG_ENCRYPTED_KEYCHAIN" \
  | gpg --batch --passphrase "$GPG_PASS_INNER" --pinentry-mode loopback \
    --decrypt > "$KEYCHAIN"

echo -e "\n### Adding code-signing keychain to search list..."
security list-keychains -s $ORIG_KEYCHAIN_LIST "$KEYCHAIN"
security list-keychains

echo -e "\n### Unlocking keychain..."
security unlock-keychain -p "$KEYCHAIN_PASS" "$KEYCHAIN"

echo -e "\n### Signing object..."

# If `OBJECT` is a directory, we assume it's an app
# bundle, otherwise we consider it to be a dmg.
if [ -d "$OBJECT" ]; then
  codesign --sign "$DEV_ID" --deep --force --verbose=4 "$OBJECT"
else
  codesign --sign "$DEV_ID" --force --verbose=4 "$OBJECT"
fi

echo -e "\n### Verifying signature..."
codesign --verify --strict=all --deep --verbose "$OBJECT"

echo -e "\n### Assessing Gatekeeper validation..."
if [ -d "$OBJECT" ]; then
  spctl --assess --type execute --verbose=2 "$OBJECT"
else
  spctl --assess --type open --context context:primary-signature --verbose=2 "$OBJECT"
fi
