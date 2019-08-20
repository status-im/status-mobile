#!/usr/bin/env bash

if [[ -z "${IN_NIX_SHELL}" ]]; then
    echo "Remember to call 'make shell'!"
    exit 1
fi

#
# This script takes care of generating/updating the maven-sources.nix file
# representing the offline Maven repo containing the dependencies
# required to build the project
#

set -Eeu

GIT_ROOT=$(cd "${BASH_SOURCE%/*}" && git rev-parse --show-toplevel)
current_dir=$(cd "${BASH_SOURCE%/*}" && pwd)
inputs_file_path="${current_dir}/maven-inputs.txt"
output_nix_file_path="${current_dir}/maven-sources.nix"
inputs2nix=$(realpath --relative-to="${current_dir}" "${GIT_ROOT}/nix/tools/maven/maven-inputs2nix.sh")

echo "Regenerating Nix files, this process should take about 90 minutes"
nix-shell --run "set -Eeuo pipefail; LC_ALL=C ${current_dir}/fetch-maven-deps.sh | sort -u -o ${inputs_file_path}" \
          --pure -A targets.mobile.android.generate-maven-and-npm-deps-shell --argstr target-os android --show-trace "${GIT_ROOT}/default.nix"

pushd ${current_dir}
${inputs2nix} ${inputs_file_path} > ${output_nix_file_path}
echo "Done"
popd
