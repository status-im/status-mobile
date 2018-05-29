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

# make sure we have all the tags
git fetch --tags --quiet


# even if the current commit has a tag already, it is normal that the same commit
# is built multiple times (with different build configurations, for instance),
# so we increment the build number every time.

# find the last used build number
BUILD=$(git tag -l --sort=-v:refname | grep -e "$REGEX" | head -n 1)
# extract the number
BUILD_NO=$(getNumber "$BUILD")
# increment
BUILD_NO="$((BUILD_NO+1))"

if [ "$1" = "--tag" ]; then
    echo "Tagging HEAD: build-$BUILD_NO" >&2
    echo "You will need to 'git push --tags' to make this tag take effect." >&2
    git tag "build-$BUILD_NO" HEAD
fi

# finally print build number
echo "$BUILD_NO"
