#!/usr/bin/env bash
set -euo pipefail

GIT_ROOT=$(cd "${BASH_SOURCE%/*}" && git rev-parse --show-toplevel)
FDROIDATA_REPO_URL="https://gitlab.com/fdroid/fdroiddata.git"
WORKING_DIR="${HOME}/fdroid-release"

source "${GIT_ROOT}/scripts/colors.sh"

function log_info()    { echo -e " ${GRN}>>>${RST} $@"; }
function log_data()    { echo -e " ${BLU}===${RST} $@"; }
function log_notice()  { echo -e " ${YLW}<<<${RST} $@"; }
function log_warning() { echo -e " ${RED}!!!${RST} $@"; }

if [[ $# -ne 1 ]]; then
    echo "No release URL provided!" >&2
    echo "Usage: fdroid-pr.sh https://.../release.apk" >&2
    exit 1
fi
# URL of APK to identify release version code
RELEASE_APK="${1}"

log_info "Creating working dir..."
mkdir -p "${WORKING_DIR}"

APK_FILE="${WORKING_DIR}/status.apk"
if [[ -f "${RELEASE_APK}" ]]; then
    log_info "Using: ${RELEASE_APK}"
    APK_FILE="${RELEASE_APK}"
else
    log_info "Fetching release..."
    curl -s "${RELEASE_APK}" -o "${APK_FILE}"
fi

log_info "Parsing APK..."

VERSION_CODE=$(apkanalyzer manifest version-code "${APK_FILE}")
if [[ -n "${VERSION_CODE}" ]]; then
    log_data "Version Code: ${VERSION_CODE}"
else
    log_warning "Failed to find version code." >&2; exit 1
fi

VERSION_NAME=$(apkanalyzer manifest print "${APK_FILE}" | awk -F'"' '/android:versionName/{print $2}')
if [[ -n "${VERSION_NAME}" ]]; then
    log_data "Version Code: ${VERSION_NAME}"
else
    log_warning "Failed to find version name." >&2; exit 1
fi

COMMIT_HASH=$(apkanalyzer manifest print "${APK_FILE}" | awk -F'"' '/commitHash/{getline; print $2}')
if [[ -n "${COMMIT_HASH}" ]]; then
    log_data "Commit Hash: ${COMMIT_HASH}"
else
    log_warning "Failed to find commit hash." >&2; exit 1
fi

CLONE_DIR="${WORKING_DIR}/fdroidata"
METADATA_FILE="${CLONE_DIR}/metadata/im.status.ethereum.yml"

PREVIOUS_BRANCH=""
if [[ -d "${CLONE_DIR}" ]]; then
    log_info "Fetching: ${FDROIDATA_REPO_URL}"
    cd "${CLONE_DIR}"
    PREVIOUS_BRANCH=$(git rev-parse --abbrev-ref HEAD)
    git checkout master
    git pull --force
else
    log_info "Cloning: ${FDROIDATA_REPO_URL}"
    git clone -q --depth=1 "${FDROIDATA_REPO_URL}" "${CLONE_DIR}"
    cd "${CLONE_DIR}"
fi

BRANCH_NAME="status-im/v${VERSION_NAME}"
log_info "Checkout out branch: ${BRANCH_NAME}"
if [[ "${PREVIOUS_BRANCH}" == "${BRANCH_NAME}" ]]; then
    log_warning "Removing previous branch: ${PREVIOUS_BRANCH}"
    git branch -D "${BRANCH_NAME}"
fi
git switch -C "${BRANCH_NAME}"

log_info "Updating metadata file..."

# find line number of last "versionName" line
START_LINE=$(awk '/ versionCode:/{n=NR} END{print n}' "${METADATA_FILE}")
# find line number of last "build" line
END_LINE=$(awk '/ build:/{n=NR} END{print n}' "${METADATA_FILE}")
# Get the latest entry, excluding version info
BUILD_PARAMS=$(awk "NR >= $((START_LINE+2)) && NR <= ${END_LINE}" "${METADATA_FILE}")

# Build new entry
NEW_ENTRY="
  - versionName: ${VERSION_NAME}
    versionCode: ${VERSION_CODE}
    commit: ${COMMIT_HASH}
${BUILD_PARAMS}"

# Insert new release build entry
sed -i "$((END_LINE+1))i\\${NEW_ENTRY//$'\n'/\\n}" "${METADATA_FILE}"
# Update current version values
sed -i "s/CurrentVersion: .*/CurrentVersion: ${VERSION_NAME}/" "${METADATA_FILE}"
sed -i "s/CurrentVersionCode: .*/CurrentVersionCode: ${VERSION_CODE}/" "${METADATA_FILE}"

log_info "Committing changes..."
# Add, commit and push
COMMIT_MESSAGE="Update Status to ${VERSION_NAME} (${VERSION_CODE})"
git add ${METADATA_FILE}
git commit -m "${COMMIT_MESSAGE}"

log_info "SUCCESS"
log_notice "Now add your fork of fdroidata as a remote to the repository and push."
log_notice "Then create a Merge Request from the branch in your fork."
log_notice "Repo path: ${BLD}${CLONE_DIR}${RST}"
