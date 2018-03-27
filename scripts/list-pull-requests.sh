#!/usr/bin/env bash

# Fetches pull request refs and prints commits
# for the given range grouped by pull request

REPOSITORY="status-im/status-react"
REPOSITORY_URL="https://github.com/$REPOSITORY.git"
PULL_REQUEST_REMOTE="pullRequestRemote"
PULL_REQUEST_URL="https://api.github.com/repos/$REPOSITORY/pulls/"

usage() {
  echo "DESCRIPTION"
  echo ""
  echo "	Fetches pull request refs and prints commits"
  echo "	for the given range grouped by pull request"
  echo ""
  echo "USAGE"
  echo ""
  echo "	$0			# list all commits"``
  echo "	$0 [ref1(..ref1)]	# lists commits within given range"
  echo "	$0 -h		# show help"
}

if [ "$1" = "-h" ] || [ "$1" = "--help" ]; then
  usage
  exit
fi

git fetch $REPOSITORY_URL +refs/pull/*/head:refs/remotes/$PULL_REQUEST_REMOTE/pull/*

git log $1 --pretty="%D, %H" |
  awk -v pull_request_url="$PULL_REQUEST_URL" -v pull_request_remote="$PULL_REQUEST_REMOTE" -F ", " '{
	  for (i = 1; i <= NF; ++i) {
		  if ($i ~ pull_request_remote "/pull/") {
			  split($i, temp, "/")
			  pr = temp[3]
			  pull_requests[pr] = pull_requests[pr] ", " $NF
			  sub(/^, /, "", pull_requests[pr])
		  }
	  }
  }

  END {
    for (pr in pull_requests) {
	  print(pr "\t" pull_request_url pr "\t" pull_requests[pr])
    }
  }'
exit
