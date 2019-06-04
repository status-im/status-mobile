#!/usr/bin/env bash

################################################################################
# This tool fetches versions of build tools from the .TOOLVERSIONS
# file in project root and calculates a single hash that represents
# the combined versions of all the specified tools.
################################################################################

GIT_ROOT=$(cd "${BASH_SOURCE%/*}" && git rev-parse --show-toplevel)
toolversion="${GIT_ROOT}/scripts/toolversion"

usage () {
  echo "Usage: gen-deps-hash [-b <base_hash>] -d <dep-name-1> [-d <dep-name-n>]" >&2
  echo
  echo "This script calculates a hash representing the required versions of the specified tools"
  exit 0
}

# some options parsing
deps=()
while getopts "hb:d:" opt; do
  case $opt in
    b) base_hash="${base_hash}${OPTARG}";;
    d)
      version=$($toolversion "$OPTARG")
      if [ $? -ne 0 ]; then
        echo "ERROR: $OPTARG not found in .TOOLVERSIONS"
        exit 1
      fi
      deps+=("$OPTARG $version")
      ;;
    h)  usage;;
    \?) echo "Invalid option: -$OPTARG" >&2; exit 1;;
  esac
done

if [ ${#deps[@]} -eq 0 ]; then
  echo "ERROR: No dependencies specified"
  echo
  usage
fi

IFS=$'\n' sorted_deps=($(sort <<<"${deps[*]}"))
unset IFS

hash=$(echo "${base_hash}${sorted_deps[@]}" | md5sum | cut -f1 -d" ")
echo "${hash:0:8}"
