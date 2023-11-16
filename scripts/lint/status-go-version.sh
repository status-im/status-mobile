#!/usr/bin/env sh

if grep -qE '"version":\s"v\d+\.\d+\.\d+"' status-go-version.json && \
  grep -q '"owner": "status-im"' status-go-version.json && \
  grep -q '"repo": "status-go"' status-go-version.json; then
  # If all patterns are found, exit with status code 0 (success).
  exit 0
elif [ $? -eq 1 ]; then
  # Display error messages explaining the expected contents of the JSON file.
  echo "The status-go-version.json file is not valid."
  echo "Please ensure the repository is specified as 'status-im/status-go'."
  echo "The version should be formatted like 'vX.Y.Z' where X, Y, and Z are numbers."
  exit 1
else
  # If the exit status was not 1, exit with the actual status code (e.g., file not found).
  exit $?
fi
