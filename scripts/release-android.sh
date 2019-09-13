#!/usr/bin/env bash

GIT_ROOT=$(cd "${BASH_SOURCE%/*}" && git rev-parse --show-toplevel)
_current_dir=$(cd "${BASH_SOURCE%/*}" && pwd)
source "$_current_dir/lib/setup/path-support.sh"

source_lib "platform.sh"

nixOpts=(
  "--arg env {BUILD_ENV=\"${BUILD_ENV}\";}"
  "--arg env {ANDROID_ABI_SPLIT=\"${ANDROID_ABI_SPLIT}\";}"
  "--arg env {ANDROID_ABI_INCLUDE=\"${ANDROID_ABI_INCLUDE}\";}"
  "--argstr build-type ${BUILD_TYPE}"
  "--argstr build-number ${BUILD_NUMBER}"
  "--argstr keystore-file ${STORE_FILE}"
)

if is_macos; then
  # Start a watchman instance if not started already and store its socket path.
  # In order to get access to the right versions of watchman and jq, we start an ad-hoc nix-shell that imports the packages from nix/nixpkgs-bootstrap.
  WATCHMAN_SOCKFILE=$(watchman get-sockname --no-pretty | jq -r .sockname)
  nixOpts+=(
    "--argstr watchmanSockPath ${WATCHMAN_SOCKFILE}"
    "--option extra-sandbox-paths ${STORE_FILE};${WATCHMAN_SOCKFILE}"
  )
else
  nixOpts+=(
    "--option extra-sandbox-paths ${STORE_FILE}"
  )
fi

${GIT_ROOT}/nix/build.sh targets.mobile.${TARGET_OS}.release "${nixOpts[@]}"
