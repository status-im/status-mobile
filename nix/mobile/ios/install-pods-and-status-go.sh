#!/usr/bin/env bash

set -e

if [ -z "$RCTSTATUS_FILEPATH" ]; then
  echo "RCTSTATUS_FILEPATH is not defined! Aborting."
  exit 1
fi

RCTSTATUS_DIR="$STATUS_REACT_HOME/modules/react-native-status/ios/RCTStatus"
targetBasename='Statusgo.framework'

# Compare target folder with source to see if copying is required
if [ -d "$RCTSTATUS_DIR/$targetBasename" ] && \
  diff -q --no-dereference --recursive $RCTSTATUS_DIR/$targetBasename/ $RCTSTATUS_FILEPATH/ > /dev/null; then
  echo "$RCTSTATUS_DIR/$targetBasename already in place"
else
  sourceBasename="$(basename $RCTSTATUS_FILEPATH)"
  echo "Copying $sourceBasename from Nix store to $RCTSTATUS_DIR"
  rm -rf "$RCTSTATUS_DIR/$targetBasename/"
  cp -a $RCTSTATUS_FILEPATH $RCTSTATUS_DIR && chmod -R 755 "$RCTSTATUS_DIR/$targetBasename"
  if [ "$sourceBasename" != "$targetBasename" ]; then
    mv "$RCTSTATUS_DIR/$sourceBasename" "$RCTSTATUS_DIR/$targetBasename"
  fi
  if [ "$(uname)" == 'Darwin' ]; then
    # CocoaPods are trash and can't handle other pod instances running at the same time
    $STATUS_REACT_HOME/scripts/wait-for.sh pod 240
    pushd $STATUS_REACT_HOME/ios && pod install; popd
  fi
fi
