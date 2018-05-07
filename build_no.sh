#!/usr/bin/env sh

#
# This script manages app build numbers.
# It returns the next build number to be used.
# If ran with --tag it will mark current HEAD with new build number.
#
# These numbers are used to mark app artifacts for:
# * Play Store - versionCode attribute (gradle)
# * Apple Store - CFBundleVersion attribute (plutil)
#
# The numbers need to be incremeneted and are maintained via
# git tags matching the '^build-[0-9]+$' regex.
# Builds from an already tagged commit should use the same number.
#
# For more details see:
# * https://developer.android.com/studio/publish/versioning
# * https://developer.apple.com/library/content/documentation/General/Reference/InfoPlistKeyReference/Articles/CoreFoundationKeys.html
# 

getNumber () {
    echo "$BUILD" | sed 's/[^0-9]*//g'
}

REGEX='^build-[0-9]\+$' 

# check if current commit has a build tag
BUILD=$(git tag --points-at refs/tags/HEAD | grep -e "$REGEX")

# chech for multiple lines
if [ 1 -lt $(echo "$BUILD" | grep -c -) ]; then
    echo "Commit marked with one than one build tag!" >&2 
    echo "$BUILD" >&2
    exit 1
fi

# use already existing build number if applicable
if [ -n "$BUILD" ]; then
    echo "Current commit already tagged: $BUILD" >&2
    getNumber $BUILD
    exit 0
fi

# otherwise find the last used build number
BUILD=$(git tag -l --sort=-v:refname | grep -e "$REGEX" | head -n 1)
# extract the number
BUILD_NO=$(getNumber "$BUILD")
# increment
BUILD_NO="$((BUILD_NO+1))"

if [ "$1" = "--tag" ]; then
    echo "Tagging refs/tags/HEAD: $BUILD" >&2
    echo "You will need to 'git push --tags' to make this tag take effect." >&2
    git tag "build-$BUILD_NO" refs/tags/HEAD
fi

# finally print build number
echo "$BUILD_NO"
