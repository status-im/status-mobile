#!/usr/bin/env bash
set -e

GIT_ROOT=$(cd "${BASH_SOURCE%/*}" && git rev-parse --show-toplevel)
source "${GIT_ROOT}/scripts/colors.sh"

function property() {
    grep "${2}" "${1}" | cut -d'=' -f2
}

function gradle_property() {
    property ${GIT_ROOT}/android/gradle.properties ${1}
}

function env_var_or_gradle_prop() {
    VAR_NAME="${1}"
    if [[ -n "${!VAR_NAME}" ]]; then
        echo "${!VAR_NAME}"
    else
        gradle_property "${VAR_NAME}"
    fi
}

function must_get_env() {
  declare -n VAR_VALUE="$1"
  if [[ -n "${VAR_VALUE}" ]]; then
      echo "${VAR_VALUE}"
      return
  fi
  echo -e "${RED}No required env variable:${RST} ${BLD}${!VAR_VALUE}${RST}" 1>&2
  exit 1
}

# If filename contains string "unsigned" change that to signed.
# Otherwise sign in-place and overwrite the current unsigned file.
if [[ "${1}" =~ unsigned ]]; then
  OUTPUT_FLAGS="--out=${1/unsigned/signed}"
fi

echo -e "${GRN}Signing APK:${RST} ${1}" >&2

exec apksigner sign --verbose \
    --ks="$(env_var_or_gradle_prop KEYSTORE_PATH)" \
    --ks-pass="pass:$(env_var_or_gradle_prop KEYSTORE_PASSWORD)" \
    --ks-key-alias="$(env_var_or_gradle_prop KEYSTORE_ALIAS)" \
    --key-pass="pass:$(env_var_or_gradle_prop KEYSTORE_KEY_PASSWORD)" \
    "${OUTPUT_FLAGS}" \
    "${1}"
