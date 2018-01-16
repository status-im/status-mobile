#!/usr/bin/env bash

set -eof pipefail

if [ $# -eq 0 ]
then
    echo "Need to supply a release tag"
    exit 0
fi

TAG=$1

if $(git tag | grep -q $TAG); then
    echo "Tag $TAG exists, replacing"
    git tag --delete $TAG
    git push --delete origin $TAG
else
    echo "New tag $TAG"
fi

git tag -s -a $TAG -m "Release $TAG"

# NOTE(oskarth): Alt. that requires two pushes: git push origin $TAG
git push --follow-tags

echo "Done"
