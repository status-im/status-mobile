#!/bin/bash

set -eof pipefail

trap cleanup EXIT

fatal() {
  echo "FATAL: $@" >&2
  exit 1
}

warn() {
  echo "$@"
}

confirm() {
  read -p "$1 (type 'yes' to continue) " r
  if [[ $r != yes ]]; then
     exit 3
  fi
}

load_config() {
  [[ -f merge-pr.conf ]] && . merge-pr.conf
  : ${OWNER:=status-im}
  : ${REPO:=status-react}
  : ${REMOTE:=origin}
  : ${BRANCH:=develop}
}

check_pr_prereq() {
  if ! command -v jq >/dev/null; then
    fatal "jq(1) is not found, PR cannot be queried."
  fi
  if ! command -v curl >/dev/null; then
    fatal "curl(1) is not found, PR cannot be queried."
  fi
}

GH_URL_BASE="https://api.github.com"

get_pr_info() {
  echo '[ Reading PR info ]'
  local pr=$1
  local pr_info_url="$GH_URL_BASE/repos/${OWNER}/${REPO}/pulls/$pr"
  set +e
  local pr_info
  pr_info=$(curl -fsS "$pr_info_url")
  if [ $? -ne 0 ]; then
    fatal "Unable to get PR info from $pr_info_url"
  fi
  set -e
  if [[ $(echo "$pr_info" | jq -r .state) == closed ]]; then
    fatal "PR $pr is closed, will not merge"
  fi
  if [[ ($(echo "$pr_info" | jq -r .maintainer_can_modify) == true) ||\
        ($(echo "$pr_info" | jq -r .author_association) ==  MEMBER) ||\
        ($(echo "$pr_info" | jq -r .author_association) ==  OWNER)]]; then
    RW_PR_REPO=1
  else
    warn "PR does not allow 'edits from maintainers', so it will be kept open"
  fi
  PR_URL=$(echo "$pr_info" | jq -r .head.repo.ssh_url)
  PR_REMOTE_NAME=pr-$pr
  PR_BRANCH=$(echo "$pr_info" | jq -r .head.ref)
  PR_LOCAL_BRANCH=pr-$pr
}

fetch_pr() {
  echo '[ Fetching PR ]'
  git remote add $PR_REMOTE_NAME $PR_URL
  git fetch $PR_REMOTE_NAME $PR_BRANCH
}

refresh_base_branch() {
  git fetch $REMOTE $BRANCH
}

rebase_pr() {
  git checkout -B $PR_LOCAL_BRANCH $PR_REMOTE_NAME/$PR_BRANCH
  git rebase $BRANCH
}

check_is_pr_single_commit() {
  if [[ $(git rev-list $BRANCH..$PR_LOCAL_BRANCH | wc -l) -ne 1 ]] ;then
    confirm "PR has multiple commits, continue merging without squashing them?"
  fi
}

confirm_pr() {
  git log -p $BRANCH..$PR_LOCAL_BRANCH
  confirm "Do you like this PR?"
}

sign_pr() {
  git commit --amend --gpg-sign --signoff
}

verify_pr() {
  git show --show-signature $PR_LOCAL_BRANCH
  confirm "Is the signature on the commit correct?"
}

merge_pr() {
  # If PR is specified and can be pushed into, do it to mark PR as closed
  if [[ -n $RW_PR_REPO ]]; then
      git push -f $PR_REMOTE_NAME $PR_LOCAL_BRANCH:$PR_BRANCH
  fi
  git checkout $BRANCH
  git merge --ff-only $PR_LOCAL_BRANCH
  git push $REMOTE $BRANCH
}

cleanup() {
  if [[ -z $DEBUG ]]; then
      git checkout -q $BRANCH
      git branch -q -D $PR_LOCAL_BRANCH 2>/dev/null || :
      git remote remove $PR_REMOTE_NAME 2>/dev/null || :
  fi
}

run() {
  if [[ $# -ne 1 ]] ; then
    cat <<EOF >&2
Requirements:
  jq
  curl
Usage:
  ./merge-pr.sh <PR-ID>

EOF
    exit 2
  fi
  load_config
  check_pr_prereq
  get_pr_info "$@"
  cleanup
  fetch_pr
  refresh_base_branch
  rebase_pr
  check_is_pr_single_commit
  confirm_pr
  sign_pr
  verify_pr
  merge_pr
}

run "$@"
