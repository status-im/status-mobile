#!/usr/bin/env bash

#####################################################################
#
# Save the timestamp-based build number for builds
# that get uploaded to places like:
# Apple Store, Play Store, or TestFlight
#
# The result of this script is used by scripts/build_no.sh
# when being run in Jenkins build context.
#
#####################################################################

# Fail on first error
set -e

GIT_ROOT=$(git rev-parse --show-toplevel)
BUILD_NUMBER_FILE="${GIT_ROOT}/BUILD_NUMBER"

if [[ -f "${BUILD_NUMBER_FILE}" ]]; then
    cat "${BUILD_NUMBER_FILE}"
else
    # Format: Year(2 digit) + Month + Day + Hour + Minutes
    # Example: 1812011805
    date '+%y%m%d%H%M' | tee "${BUILD_NUMBER_FILE}"
fi
