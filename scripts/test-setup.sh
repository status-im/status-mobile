#!/bin/bash
# This script tests the current source code against the dev environment which
# has all of the dependencies installed.
set -e
script_dir=$(dirname $0)
pushd $script_dir/..

docker-compose run --rm \
  --entrypoint bash \
  status-im <<EOF
set -e
./re-natal use-figwheel
lein test-cljs
lein prod-build
EOF

popd
