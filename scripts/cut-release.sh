#!/bin/bash

# Increment a version string using Semantic Versioning (SemVer) terminology.

# Parse command line options.

set -e

if ! git diff-index --quiet HEAD; then
  echo "Your Git working directory is not clean!" >&2
  exit 1
fi

while getopts ":Mmp" Option
do
  case $Option in
    M ) major=true;;
    m ) minor=true;;
  esac
done

shift $(($OPTIND - 1))

echo "Checking out develop..."
git checkout develop

echo "Pulling latest develop..."
git pull

version=$(cat VERSION)

if [[ -z $major ]] && [[ -z $minor ]]; then
  minor=true
fi

# Build array from version string.

a=( ${version//./ } )

# If version string is missing or has the wrong number of members, show usage message.

if [ ${#a[@]} -ne 3 ]
then
  echo "usage: $(basename $0) [-Mmp]"
  exit 1
fi

# Increment version numbers as requested.

if [ ! -z $major ]
then
  ((a[0]++))
  a[1]=0
  a[2]=0
fi

if [ ! -z $minor ]
then
  ((a[1]++))
  a[2]=0
fi

newversion=${a[0]}.${a[1]}.${a[2]}
releasebranch="${a[0]}.${a[1]}.x"
echo "Cutting release branch $newversion"

prbranch="chore/update-release-to-$newversion"


git checkout -b $prbranch
echo $newversion > VERSION
git add VERSION
git commit -m "Bump release to $newversion"
git push --set-upstream origin $prbranch
echo "Creating PR..."
if ! command -v gh &> /dev/null
then
  echo "Github command line not present, don't forget to create one"
else
  gh pr create --title "Bump release to $newversion" --body "status:ready"
fi

echo "Creating release branch..."
git checkout -b release/$releasebranch
echo "Release branch created, push to origin to start building"
