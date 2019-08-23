#!/usr/bin/env bash

#####################################################################
#
# Save the timestamp-based build number for builds
# that get uploaded to places like:
# Apple Store, Play Store, or TestFlight
#
# The result of this script is used by scripts/version/build_no.sh
# when being run in Jenkins build context.
#
#####################################################################

# Fail on first error
set -e

GIT_ROOT=$(cd "${BASH_SOURCE%/*}" && git rev-parse --show-toplevel)
BUILD_NUMBER_FILE="${GIT_ROOT}/BUILD_NUMBER"

if [[ -f "${BUILD_NUMBER_FILE}" ]]; then
    cat "${BUILD_NUMBER_FILE}"
else
    # Format: Year(4 digit) + Month + Day + Hour
    # Example: 2018120118
    # We limited precision to hours to avoid of mismatched numbers.
    date '+%Y%m%d%H' | tee "${BUILD_NUMBER_FILE}"
fi
