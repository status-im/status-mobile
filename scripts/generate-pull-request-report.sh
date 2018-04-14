#!/usr/bin/env bash

# Github Client Credentials
# Required to increase rate limits to a useable level.

GITHUB_CLIENT_ID=cae8611e191547e163bb
GITHUB_CLIENT_SECRET=ebcfe4e78a6bb96933a3e2476a6d1e8efa8d8b07

# Generates a markdown report for the pull requests given in STDIN
# The input format should match the output of list-pull-requests.sh:
# [pr #] [tab] [github pr api url] [tab] [comma separated list of commits]

usage() {
  echo "DESCRIPTION"
  echo ""
  # Generates a markdown report for the pull requests given in STDIN
  # The input format should match the output of list-pull-requests.sh:
  # [pr #] [tab] [github pr api url] [tab] [comma separated list of commits]
  echo ""
}

if [ "$1" = "-h" ] || [ "$1" = "--help" ]; then
  usage
  exit
fi

fatal() {
  echo "FATAL: $@" >&2
  exit 1
}

echo_json() {
	local json=$1
	local path=$2
	echo -n "$json" | jq -r "$path"
}

check_pr_prereq() {
  if ! command -v jq >/dev/null; then
    fatal "jq(1) is not found, PR cannot be queried."
  fi
  if ! command -v curl >/dev/null; then
    fatal "curl(1) is not found, PR cannot be queried."
  fi
}

fetch() {
	local url="$1?client_id=$GITHUB_CLIENT_ID&client_secret=$GITHUB_CLIENT_SECRET"
	fetch_result=$(curl --fail -fsS "$url")
	if [ $? -ne 0 ]; then fatal "Unable to get fetch from $1"; fi
}

process_stdin() {
	while IFS= read -r line; do
		local pr_url=`echo "$line" | cut -f 2`
  		echo_pull_request "$pr_url"
	done
}

echo_pull_request() {
	local pr_url=$1
	fetch "$pr_url"
	local pr=$fetch_result

	echo "## $(echo_json "$pr" .title) (#$(echo_json "$pr" .number))

URL:     $(echo_json "$pr" .html_url)
Creator: $(echo_json "$pr" .user.login)
Labels:  $(echo_json "$pr" ' [.labels[].name] | join(", ") ' )

$(echo_json "$pr" .body)

"
}

check_pr_prereq

echo "# Pull Request Report

$( process_stdin )

"
