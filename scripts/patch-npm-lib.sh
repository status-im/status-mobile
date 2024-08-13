#!/usr/bin/env bash

GIT_ROOT=$(cd "${BASH_SOURCE%/*}" && git rev-parse --show-toplevel)

read -p "Enter the path of the file to patch: " FILE_PATH

if [ ! -f "${FILE_PATH}" ]; then
  echo "File not found: ${FILE_PATH}" >&2
  exit 1
fi

if [[ "${FILE_PATH}" == *"node_modules"* ]]; then
  if [[ "${FILE_PATH}" != *"./node_modules"* ]]; then
    echo "Please prefix the file path like this './node_modules/'. The path you provided was: ${FILE_PATH}" >&2
    exit 1
  fi
fi

FILE_NAME=$(basename "${FILE_PATH}")
echo "File to patch: ${FILE_PATH}"

TEMP_DIR=$(mktemp -d)
trap 'rm -rf "${TEMP_DIR}"' EXIT
echo "Temporary directory created: ${TEMP_DIR}"

cp "${FILE_PATH}" "${TEMP_DIR}"
echo "Original file copied to temporary directory."

echo "Please make the necessary changes to the file: ${FILE_PATH}"
echo "Press any key when you are done with the changes..."

ORIGINAL_MTIME=$(stat -c %Y "${FILE_PATH}")
read -n 1 -s
CURRENT_MTIME=$(stat -c %Y "${FILE_PATH}")

if [[ "${ORIGINAL_MTIME}" -eq "${CURRENT_MTIME}" ]]; then
  echo "Warning: No changes were made to the file. Patch file will not be generated."
  exit 0
fi

echo "Generating patch file..."
diff -Naur "${TEMP_DIR}/${FILE_NAME}" "${FILE_PATH}" > "${GIT_ROOT}/patches/${FILE_NAME}.patch"

echo "Patch file created at ${GIT_ROOT}/patches/${FILE_NAME}.patch"
echo "Info: Please execute 'make run-clojure' to test if the patch file works as expected."
