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
    echo "$1" | sed 's/[^0-9]*//g'
}

REGEX='^build-[0-9]\+$'

# make sure we have all the tags
git fetch --tags --quiet >/dev/null || >&2 echo "Could not fetch tags from remote"

# check if current commit has a build tag
# since we are building in separate jobs we have to check for a tag
BUILD_TAG=$(git tag --points-at HEAD | grep -e "$REGEX")

# chech for multiple lines
if [ 1 -lt $(echo "$BUILD_TAG" | grep -c -) ]; then
    echo "Commit marked with more than one build tag!" >&2 
    echo "$BUILD_TAG" >&2
    exit 1
fi

# use already existing build number if applicable
if [ -n "$BUILD_TAG" ]; then
    echo "Current commit already tagged: $BUILD_TAG" >&2
    getNumber $BUILD_TAG
    exit 0
fi

# if no tag was found and --increment was not given stop
if [ "$1" != "--increment" ]; then
    exit 0
fi

# find the last used build number
BUILD=$(git tag -l --sort=-v:refname | grep -e "$REGEX" | head -n 1)
# extract the number
BUILD_NO=$(getNumber "$BUILD")

# These need to be provided by Jenkins
if [ -z "${GIT_USER}" ] || [ -z "${GIT_PASS}" ]; then
    echo "Git credentials not specified! (GIT_USER, GIT_PASS)" >&2
    exit 1
fi
# increment
BUILD_NO="$((BUILD_NO+1))"

echo "Tagging HEAD: build-$BUILD_NO" >&2
git tag "build-$BUILD_NO" HEAD
git push --tags https://${GIT_USER}:${GIT_PASS}@github.com/status-im/status-react

# finally print build number
echo "$BUILD_NO"
