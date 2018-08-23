#!/usr/bin/env sh

set -e

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

REGEX='^build-[0-9]\+$'

getNumber () {
    echo "$1" | sed 's/[^0-9]*//g'
}

findNumber () (
    # check if current commit has a build tag
    # since we are building in separate jobs we have to check for a tag
    BUILD_TAG=$(git tag --points-at HEAD | grep -e "$REGEX" | tail -n1)
    
    # use already existing build number if applicable
    if [ -n "$BUILD_TAG" ]; then
        echo "Current commit already tagged: $BUILD_TAG" >&2
        getNumber $BUILD_TAG
    fi
)

tagBuild () {
    echo "Tagging HEAD: build-$1" >&2
    git tag "build-$1" HEAD
    if [ -n "$GIT_USER" ] && [ -n "$GIT_PASS" ]; then
        git push --tags \
          https://${GIT_USER}:${GIT_PASS}@github.com/status-im/status-react
    else
        git push --tags git@github.com:status-im/status-react
    fi
}

increment () {
    # find the last used build number
    BUILD=$(git tag -l --sort=-v:refname | grep -e "$REGEX" | head -n 1)
    # extract the number
    BUILD_NO=$(getNumber "$BUILD")
    
    # increment
    BUILD_NO="$((BUILD_NO+1))"
    # finally print build number
    echo "$BUILD_NO"
}

#####################################################################

# make sure we have all the tags
git fetch --tags --quiet >/dev/null || \
    >&2 echo "Could not fetch tags from remote"

# check if this commit already has a build number
NUMBER=$(findNumber)

# if it doesn't, or we are forcing via cli option, increment
if [ -z "$NUMBER" ] || [ "$1" = "--increment" ]; then
    NUMBER=$(increment)
    tagBuild $NUMBER
fi

# print build number
echo $NUMBER
