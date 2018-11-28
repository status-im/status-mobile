#!/usr/bin/env bash

# This script should be used instead of `npm install` in the project for security reasons.
# The script checks if `npm install` changed the package-lock.json file, this way
# it verifies that all the downloaded dependencies are of the versions we expect they have.
#
# Usage:
#
# From the root project directory call `scripts/locked-npm-install.sh`
#
# Return value:
# 
# Script returns 0 if all lock files are up to date, 1 otherwise.

npm install

NPM_EXIT_CODE=$?

if [ $NPM_EXIT_CODE -ne 0 ]; then
    echo "Error while running npm install"
    exit $NPM_EXIT_CODE
fi

EXIT_CODE=0

                                                  # TODO: Re-enable when Desktop's package-lock is fixed.
                                                  # 
for LOCK_FILE in "mobile_files/package-lock.json" #"desktop_files/package-lock.json"
do
    echo "VERIFYING LOCK FILE: $LOCK_FILE"

    git diff-index --quiet HEAD $LOCK_FILE
    DIFF_RESULT=$?

    if [ $DIFF_RESULT -ne 0 ]; then
        echo "!!! $LOCK_FILE is OUTDATED !!!"
        echo "if that is expected, please, commit your changes to the package-lock.json"
        echo "if that is unexpected, please, verify the new dependencies or report to security@status.im"
        echo ""
        echo "*************************"
        echo "DIFF of $LOCK_FILE"
        echo "if the only difference in the diff is https -> http, try doing"
        echo "+------------------------------+"
        echo "| > rm -rf node_modules/       |"
        echo "| > npm cache clean --force    |"
        echo "+------------------------------+"
        echo "and run this script again"
        echo "-> https://npm.community/t/npm-install-downgrading-resolved-packages-from-https-to-http-registry-in-package-lock-json/1818/7"
        echo "-> https://npm.community/t/some-packages-have-dist-tarball-as-http-and-not-https/285/9"
        git --no-pager diff $LOCK_FILE

        EXIT_CODE=1
    else
        echo "$LOCK_FILE is up-to-date"
    fi
    echo ""
    echo ""
done

exit $EXIT_CODE
