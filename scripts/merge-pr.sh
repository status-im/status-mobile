#!/bin/bash

set -eof pipefail

BRANCH=$1
if [[ $# -eq 0 ]] ; then
    echo 'Branch required as first argument'
    exit 0
fi

confirm() {
  read -p "$1 (type 'yes' to continue) " r
  if [[ $r != yes ]]; then
     exit 3
  fi
}

echo "[Merge PR from ${BRANCH}]"

echo "[Update remote and checkout branch]"
git remote update origin
git checkout -B $BRANCH origin/$BRANCH && git pull

echo "[Rebase and squash to one commit (manual)]"
git rebase -i origin/develop
if [[ $(git rev-list $BRANCH..$PR_LOCAL_BRACNCH | wc -l) -ne 1 ]] ;then
    confirm "There are multiple commits in this PR. Are you sure you wish to continue without squashing?"
fi

echo "[Verify signature and commit (manual), update PR]"
git show --show-signature
git push -f

echo "[Checkout develop and merge with same SHA]"
git checkout develop && git pull
git merge --ff-only $BRANCH

echo "[Push to protected develop branch]"
git push

echo "[Clean up remote branch]"
git push origin --delete $BRANCH

echo "[Done]"
