#!/usr/bin/env bash

# Needed to fail on must_get_env()
set -e

GIT_ROOT=$(cd "${BASH_SOURCE%/*}" && git rev-parse --show-toplevel)
source "${GIT_ROOT}/scripts/colors.sh"

function must_get_env() {
  declare -n VAR_VALUE="$1"
  if [[ -n "${VAR_VALUE}" ]]; then
      echo "${VAR_VALUE}"
      return
  fi
  echo -e "${RED}No required env variable:${RST} ${BLD}${!VAR_VALUE}${RST}" 1>&2
  exit 1
}

function append_env_export() {
  VAR_NAME=${1}
  VAR_VALUE=$(must_get_env "${VAR_NAME}")
  echo "export ${VAR_NAME}=\"${VAR_VALUE}\";" >> "${SECRETS_FILE_PATH}"
}

export COMMIT_HASH="$(git rev-parse --verify HEAD)"
nixOpts=()

# We create if now so the trap knows its location
export SECRETS_FILE_PATH=$(mktemp)
chmod 644 ${SECRETS_FILE_PATH}
# If secrets file was created we want to remove it.
trap "rm -vf ${SECRETS_FILE_PATH}" EXIT ERR INT QUIT

# Names of variables containing secrets to save in env file.
SECRETS_ENV_VARS=(
    'ALCHEMY_ARBITRUM_GOERLI_TOKEN'
    'ALCHEMY_ARBITRUM_MAINNET_TOKEN'
    'ALCHEMY_OPTIMISM_GOERLI_TOKEN'
    'ALCHEMY_OPTIMISM_MAINNET_TOKEN'
    'INFURA_TOKEN'
    'INFURA_TOKEN_SECRET'
    'OPENSEA_API_KEY'
    'POKT_TOKEN'
)

# Secrets like this can't be passed via args or they end up in derivation.
for SECRET_VAR_NAME in "${SECRETS_ENV_VARS[@]}"; do
    [[ -n "${!SECRET_VAR_NAME}" ]] && append_env_export "${SECRET_VAR_NAME}"
done

# If no secrets were passed there's no need to pass the 'secretsFile'.
if [[ -s "${SECRETS_FILE_PATH}" ]]; then
  nixOpts+=("--option" "extra-sandbox-paths" "${SECRETS_FILE_PATH}")
  nixOpts+=("--argstr" "secretsFile" "${SECRETS_FILE_PATH}")
fi

# Used by Clojure at compile time to include JS modules
nixOpts+=("--argstr" "buildEnv" "$(must_get_env BUILD_ENV)")

# On Darwin we hit a sandbox serialization limit of 65535.
# https://github.com/NixOS/nix/issues/4119
if [[ "$(uname -s)" =~ Darwin ]]; then
  nixOpts+=("--option" "build-use-sandbox" "false")
else
  nixOpts+=("--option" "build-use-sandbox" "true")
fi


"${GIT_ROOT}/nix/scripts/build.sh" targets.mobile.android.release "${nixOpts[@]}"
