#!/usr/bin/env bash

set -Eeu

# This script computes the required Maven dependencies required by cljsbuild for this project
#  and outputs them as a text file, where each line represents a URL to a Maven dependency
#  (minus the extension).
# For this, we start with a clean cache (in a temporary directory) and call cljsbuild
#  to cause it to download all the artifacts.

GIT_ROOT=$(cd "${BASH_SOURCE%/*}" && git rev-parse --show-toplevel)
_current_dir=$(cd "${BASH_SOURCE%/*}" && pwd)
_project_file_name='project.clj'
_lein_cmd='lein with-profile prod cljsbuild once'
_repo_path=$(mktemp -d)

function filter() {
  sed -E "s;Retrieving ([^ ]+)\.(pom|jar) from $1.*;$2\1;"
}

echo "Computing maven dependencies with \`$_lein_cmd\`..." > /dev/stderr
trap "rm -rf ${_repo_path}; [ -f ${_project_file_name}.bak ] && mv -f ${_project_file_name}.bak ${_project_file_name}" HUP ERR EXIT INT

cd $GIT_ROOT

# Add a :local-repo entry to project.clj so that we always start with a clean repo
sed -i'.bak' -E "s|(:license \{)|:local-repo \"$_repo_path\" \1|" ${_project_file_name}
rm -rf ./${_repo_path}
$_lein_cmd 2>&1 \
  | grep Retrieving \
  | filter clojars https://repo.clojars.org/ \
  | filter central https://repo1.maven.org/maven2/ # NOTE: We could use `lein pom` to figure out the repository names and URLs so they're not hardcoded
