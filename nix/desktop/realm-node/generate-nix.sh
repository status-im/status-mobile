#!/usr/bin/env bash

#
# Run this file to regenerate the Nix files in ./output.
# Prerequisites: Node, npm, and node2nix (installed with npm i -g https://github.com/svanderburg/node2nix)
#

GIT_ROOT=$(cd "${BASH_SOURCE%/*}" && git rev-parse --show-toplevel)
SCRIPTPATH="$( cd "$(dirname "$0")" ; pwd -P )"
toolversion="${GIT_ROOT}/scripts/toolversion"
dir="$SCRIPTPATH"
input="${dir}/output/node-packages.json"
output_dir="${dir}/output"
supplement_input="${dir}/output/supplement.json"

rm -rf $output_dir && mkdir -p $output_dir
# Specify the package.json file containing the dependencies to install
cat << EOF > $input
[
  { "realm": "https://github.com/status-im/realm-js/archive/v2.20.1.tar.gz" }
]
EOF

# Specify the package.json file containing the build dependencies to install
cat << EOF > $supplement_input
[
  "node-pre-gyp"
]
EOF

# Specify the package.json file containing the build dependencies to install
cat << EOF > $supplement_input
[
  "node-pre-gyp"
]
EOF

node_required_version=$($toolversion node)
node_major_version=$(echo $node_required_version | cut -d. -f1,1)

node2nix --nodejs-${node_major_version} --bypass-cache \
         --input             $input \
         --output            $output_dir/node-packages.nix \
         --supplement-input  $supplement_input \
         --supplement-output $output_dir/supplement.nix \
         --composition       $output_dir/default.nix \
         --node-env          $output_dir/node-env.nix
rm $input $supplement_input
