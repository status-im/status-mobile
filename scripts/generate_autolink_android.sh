#!/usr/bin/env bash

function generate_android_autolink() {
  AUTOLINKING_DIR="android/build/generated/autolinking"

  if [ ! -d "$AUTOLINKING_DIR" ]; then
      mkdir -p "$AUTOLINKING_DIR"
      echo "Created directory: $AUTOLINKING_DIR"
  fi

  rm "$AUTOLINKING_DIR/autolinking.json" || true
  react-native config > "$AUTOLINKING_DIR/autolinking.json"
  echo "Generated autolinking.json"

  ROOT_VALUE=$(jq -r '.root' "$AUTOLINKING_DIR/autolinking.json")

  if [ -z "$ROOT_VALUE" ]; then
      echo "Error: 'root' key not found in autolinking.json"
      exit 1
  fi

  echo "Current 'root' value: $ROOT_VALUE"

  sed -i.bak "s|$ROOT_VALUE|..|g" "$AUTOLINKING_DIR/autolinking.json"

  echo "Updated autolinking.json: replaced all occurrences of '$ROOT_VALUE' with '..'"

  rm "$AUTOLINKING_DIR/autolinking.json.bak"
}

generate_android_autolink
