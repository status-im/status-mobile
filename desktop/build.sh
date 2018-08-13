#!/bin/bash

# Copyright (C) 2016, Canonical Ltd.
# All rights reserved.

# This source code is licensed under the BSD-style license found in the
# LICENSE file in the root directory of this source tree. An additional grant
# of patent rights can be found in the PATENTS file in the same directory.

# XXX: Don't move this script
cd $(dirname $0)

while (( "$#" )); do
if [[ $1 == "-e" ]]; then
  shift
	ExternalModulesPaths="$1"
fi
if [[ $1 == "-j" ]]; then
  shift
	JsBundlePath="$1"
fi
if [[ $1 == "-f" ]]; then
  shift
	desktopFonts="$1"
fi
shift
done

echo "build.sh external modules paths: "$ExternalModulesPaths
echo "build.sh JS bundle path: "$JsBundlePath
echo "build.sh desktop fonts: "$desktopFonts

# Workaround
rm -rf CMakeFiles CMakeCache.txt cmake_install.cmake Makefile

# Build project
cmake -DCMAKE_BUILD_TYPE=Debug -DEXTERNAL_MODULES_DIR="$ExternalModulesPaths" -DJS_BUNDLE_PATH="$JsBundlePath" -DDESKTOP_FONTS="$desktopFonts" . && make
