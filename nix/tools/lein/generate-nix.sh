#!/usr/bin/env bash

if [[ -z "${IN_NIX_SHELL}" ]]; then
    echo "Remember to call 'make shell'!"
    exit 1
fi

# This script takes care of generating/updating the nix files in the directory passed as a single argument.
# For this, we start with a clean cache (in ./.m2~/repository/) and call cljsbuild
#  to cause it to download all the artifacts. At the same time, we note them
#  in $1/lein-project-deps-maven-inputs.txt so that we can use that as an input
#  to ../maven/maven-inputs2nix.sh

set -Eeuo pipefail

output_dir=$1
mkdir -p $output_dir

_current_dir=$(cd "${BASH_SOURCE%/*}" && pwd)
_inputs_file_path="$output_dir/lein-project-deps-maven-inputs.txt"
_deps_nix_file_path="$output_dir/lein-project-deps.nix"
_nix_shell_opts="-I nixpkgs=https://github.com/status-im/nixpkgs/archive/db492b61572251c2866f6b5e6e94e9d70e7d3021.tar.gz"

echo "Regenerating Nix files, this process should take 5-10 minutes"
nix-shell ${_nix_shell_opts} --run "set -Eeuo pipefail; $_current_dir/fetch-maven-deps.sh | sort -u > $_inputs_file_path" \
          --pure --packages leiningen git
echo "Generating $(basename $_deps_nix_file_path) from $(basename $_inputs_file_path)..."
nix-shell ${_nix_shell_opts} \
          --run "$_current_dir/../maven/maven-inputs2nix.sh $_inputs_file_path > $_deps_nix_file_path" \
          --packages maven
echo "Done"
