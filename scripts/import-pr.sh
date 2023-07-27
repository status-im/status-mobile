#!/bin/bash

if [ -z "$1" ]; then
  echo "Please provide the full branch URL as an argument."
  exit 1
fi

if [ -z "$2" ]; then
    echo "Please provide the full pull request URL as second argument."
    exit 1
fi

# Extract PR number from the URL
PR_URL="$2"
PR_NUMBER=$(echo "$PR_URL" | awk -F '/' '{print $NF}')

# Fetch PR Info
PR_TITLE=$(gh pr view "$PR_NUMBER" --json title -q '.title')
PR_DESCRIPTION=$(gh pr view "$PR_NUMBER" --json body -q '.body')
BRANCH_NAME=$(gh pr view "$PR_NUMBER" --json headRefName -q '.headRefName')

# Extracting the repo URL and branch name
FULL_URL="$1"
REPO_URL=$(echo "$FULL_URL" | awk -F "/tree/" '{print $1}')
REPO_NAME=$(basename "$REPO_URL")
NEW_REMOTE_URL="https://github.com/status-im/status-mobile"

# Clone the repo if not already cloned
if [ ! -d "$REPO_NAME" ]; then
    git clone "$REPO_URL"
    cd "$REPO_NAME" || exit
    gh repo set-default "$REPO_URL"
else
    cd "$REPO_NAME" || exit
    gh repo set-default "$REPO_URL"
    git fetch
fi

# Check if branch already exists
BRANCH_EXISTS=$(git show-ref refs/heads/"$BRANCH_NAME" 2>/dev/null)

if [ -z "$BRANCH_EXISTS" ]; then
    # New Import
    git checkout -b "$BRANCH_NAME" "origin/$BRANCH_NAME"
    git remote add source "$REPO_URL"
    git remote set-url origin $NEW_REMOTE_URL
    git push -u origin "$BRANCH_NAME"
    gh pr create --base develop --head "$BRANCH_NAME" --title "[IMPORTED] $PR_TITLE" --body "$PR_DESCRIPTION"
else
    # Sync Existing Import
    git checkout "$BRANCH_NAME"
    git fetch source "$BRANCH_NAME"
    git rebase source/"$BRANCH_NAME" # Rebase instead of merge
    git push origin "$BRANCH_NAME" # Push updates to your imported branch
fi
