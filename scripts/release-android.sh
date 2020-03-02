#!/usr/bin/env bash

# Needed to fail on must_get_env()
set -e

GIT_ROOT=$(cd "${BASH_SOURCE%/*}" && git rev-parse --show-toplevel)
source "${GIT_ROOT}/scripts/colors.sh"

function must_get_env() {
  declare -n VAR_NAME="$1"
  if [[ -n "${VAR_NAME}" ]]; then
      echo "${VAR_NAME}"
      return
  fi
  echo -e "${RED}No required env variable:${RST} ${BLD}${!VAR_NAME}${RST}" 1>&2
  exit 1
}

config=''
if [ -n "${STATUS_GO_SRC_OVERRIDE}" ]; then
  config+="status-im.status-go.src-override=\"${STATUS_GO_SRC_OVERRIDE}\";"
fi
if [ -n "${NIMBUS_SRC_OVERRIDE}" ]; then
  config+="status-im.nimbus.src-override=\"${NIMBUS_SRC_OVERRIDE}\";"
fi
config+="status-im.build-type=\"$(must_get_env BUILD_TYPE)\";"
config+="status-im.status-react.build-number=\"$(must_get_env BUILD_NUMBER)\";"
config+="status-im.status-react.keystore-file=\"$(must_get_env KEYSTORE_PATH)\";"
nixOpts=(
  "--arg config {${config}}"
  "--arg env {BUILD_ENV=\"${BUILD_ENV}\";ANDROID_ABI_SPLIT=\"${ANDROID_ABI_SPLIT}\";ANDROID_ABI_INCLUDE=\"${ANDROID_ABI_INCLUDE}\";}"
)

if [[ "$OS" =~ Darwin ]]; then
  # Start a watchman instance if not started already and store its socket path.
  # In order to get access to the right versions of watchman and jq,
  # we start an ad-hoc nix-shell that imports the packages from nix/nixpkgs-bootstrap.
  WATCHMAN_SOCKFILE=$(watchman get-sockname --no-pretty | jq -r .sockname)
  nixOpts+=(
    "--argstr watchmanSockPath ${WATCHMAN_SOCKFILE}"
    "--option extra-sandbox-paths ${KEYSTORE_PATH};${WATCHMAN_SOCKFILE}"
  )
else
  nixOpts+=(
    "--option extra-sandbox-paths ${KEYSTORE_PATH}"
  )
fi

${GIT_ROOT}/nix/scripts/build.sh targets.mobile.android.release "${nixOpts[@]}"
