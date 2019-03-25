#!/usr/bin/env bash

GIT_ROOT=$(git rev-parse --show-toplevel)
SCRIPTPATH="$( cd "$(dirname "$0")" ; pwd -P )"
toolversion="${GIT_ROOT}/scripts/toolversion"
dir="$SCRIPTPATH"
input="${dir}/output/node-packages.json"
output_dir="${dir}/output"

rm -rf $output_dir && mkdir -p $output_dir
react_native_cli_required_version=$($toolversion react_native_cli)
cat << EOF > $input
[
    { "react-native-cli": "${react_native_cli_required_version}" }
]
EOF

node_required_version=$($toolversion node)
node_major_version=$(echo $node_required_version | cut -d. -f1,1)
node2nix --nodejs-${node_major_version} --bypass-cache -i $input \
         -o $output_dir/node-packages.nix \
         -c $output_dir/default.nix \
         -e $output_dir/node-env.nix
rm $input
