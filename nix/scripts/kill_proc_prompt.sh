#!/usr/bin/env bash
set -Eeuo pipefail
# script to prompt user for terminating a process

pid="${1}"

read -p "Do you want to terminate this process? (y/n): " choice
if [[ "${choice}" == "y" ]]; then
    sudo kill "${pid}"
    echo "Process ${pid} terminated."
else
    echo "Process not terminated. Please close it manually and retry."
    exit 1
fi
