#!/usr/bin/env bash

set -Eeo pipefail

GIT_ROOT=$(cd "${BASH_SOURCE%/*}" && git rev-parse --show-toplevel)
CURRENT_DIR="$( cd "$( dirname "$0" )" && pwd )"
. "$CURRENT_DIR/lib/setup/path-support.sh"
source_lib "properties.sh"
source_lib "platform.sh"

STORE_FILE=$(property_gradle 'STATUS_RELEASE_STORE_FILE')
STORE_FILE="${STORE_FILE/#\~/$HOME}"

function cleanup() {
  trap - EXIT ERR INT QUIT

  echo "Deleting derivations from Nix store..."
  . ~/.nix-profile/etc/profile.d/nix.sh
  releaseDrv=$(nix-instantiate --quiet $nixOpts)
  if [ -n "$releaseDrv" ]; then
    local refs=( $(nix-store -q --references $releaseDrv) )
    local prodBuildDrv=$(printf -- '%s\n' "${refs[@]}" | grep -e "prod-build-android.drv")
    nix-store --delete $prodBuildDrv $releaseDrv
  fi
}

trap "cleanup" EXIT ERR INT QUIT

[ -z "$BUILD_TYPE" ] && BUILD_TYPE='nightly'

exportedEnv=()
if [ -n "$NDK_ABI_FILTERS" ]; then
  exportedEnv+=( "NDK_ABI_FILTERS=''${NDK_ABI_FILTERS}'';" ) # NOTE: Do not include spaces in the Nix attribute set, otherwise it'll create issues with automatic bash quoting
fi
exportedEnvFlag=''
if [ ${#exportedEnv[@]} -ne 0 ]; then
  exportedEnvFlag="--arg env {${exportedEnv[@]}}"
fi
nixOpts="--option extra-sandbox-paths ${STORE_FILE} \
         --argstr target-os ${TARGET_OS} \
         --argstr build-type ${BUILD_TYPE} \
         --argstr keystore-file ${STORE_FILE} \
         --show-trace \
         $exportedEnvFlag \
         -A targets.mobile.${TARGET_OS}.release"

# Run the build
outType='release'
if [ "$BUILD_TYPE" != "release" ] && [ "$BUILD_TYPE" != "nightly" ]; then
  outType="pr"
fi

nixResultPath=$(. ~/.nix-profile/etc/profile.d/nix.sh && nix-build --pure --no-out-link $nixOpts)
if [ -n "$${nixResultPath}" ]; then
  targetPath="${nixResultPath}/app.apk android/app/build/outputs/apk/${outType}/app-${outType}.apk"
  cpFlags='-v'
  is_linux && cpFlags='-fv --no-preserve=mode'
  mkdir -p android/app/build/outputs/apk/${outType} && \
  cp ${cpFlags} ${targetPath} && \
  chmod u+w ${targetPath}
fi
