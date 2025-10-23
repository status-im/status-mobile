#!/usr/bin/env sh

DO_NOT_MERGE_KEYWORD="donotmerge"
DEFAULT_BASE_BRANCH="develop"
BASE_BRANCH="${CHANGE_TARGET:-$DEFAULT_BASE_BRANCH}"
FEATURE_BRANCH="${CHANGE_BRANCH:-$(git branch --show-current)}"

if [[ -n "$CI" && "$CI" == "true" ]]; then
    # The default Jenkins refspec does not accept branches other than the one from PR.
    git config remote.origin.fetch '+refs/heads/*:refs/remotes/origin/*'
    git fetch origin "${BASE_BRANCH}:${BASE_BRANCH}"
    COMMITS=$(git log "${BASE_BRANCH}..HEAD" --oneline)
else
    COMMITS=$(git log "${BASE_BRANCH}..${FEATURE_BRANCH}" --oneline)
fi

echo "checking for ${DO_NOT_MERGE_KEYWORD} commits"
if echo "${COMMITS}" | grep $DO_NOT_MERGE_KEYWORD; then
    echo "found ${DO_NOT_MERGE_KEYWORD} commits"
    exit 1
fi
