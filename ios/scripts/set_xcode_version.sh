#!/bin/bash

# This script automatically sets the version and short version string of
# an Xcode project from the Git repository containing the project.
#
# To use this script in Xcode, add the script's path to a "Run Script" build
# phase for your application target.

set -o errexit
set -o nounset

# First, check for git in $PATH
hash git 2>/dev/null || { echo >&2 "Git required, not installed.  Aborting build number update script."; exit 0; }

# Alternatively, we could use Xcode's copy of the Git binary,
# but old Xcodes don't have this.
#GIT=$(xcrun -find git)

# Run Script build phases that operate on product files of the target that defines them should use the value of this build setting [TARGET_BUILD_DIR]. But Run Script build phases that operate on product files of other targets should use ?BUILT_PRODUCTS_DIR? instead.
INFO_PLIST="${TARGET_BUILD_DIR}/${INFOPLIST_PATH}"

if [[ $(git ls-files -m "StatusIm/Info.plist") = *"Info.plist"* ]]; then
    echo "version was set in Info.plist"
else
    RELEASE_VERSION=$(cat ../VERSION)
    BUILD_NO=$(bash ../scripts/build_no.sh)

    # For debugging:
    echo "SHORT VERSION: $RELEASE_VERSION"
    echo "BUILD NO: $BUILD_NO"

    /usr/libexec/PlistBuddy -c "Add :CFBundleVersion string $BUILD_NO" "$INFO_PLIST" 2>/dev/null || /usr/libexec/PlistBuddy -c "Set :CFBundleVersion $BUILD_NO" "$INFO_PLIST"
    /usr/libexec/PlistBuddy -c "Set :CFBundleShortVersionString $RELEASE_VERSION" "$INFO_PLIST"
fi
