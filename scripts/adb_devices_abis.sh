#!/usr/bin/env bash

set -e
typeset -a abis
SCRIPT_NAME=$(basename $0)

function log() {
  echo "${SCRIPT_NAME} ${1}: ${2}" >&2
}

# make sure server is running, otherwise sdb will show devices offline
adb start-server

while IFS= read line; do
  read -a arr <<< "${line}"

  device_name="${arr[0]}"
  device_status=${arr[1]}

  log "DEBUG" "device_name=${device_name}"
  log "DEBUG" "device_status=${device_status}"

  if [[ "${device_status}" != "device" ]]; then
    continue
  fi

  device_product_abi=$(adb -s ${device_name} shell -n getprop ro.product.cpu.abi)
  log "DEBUG" "device_product_abi=${device_product_abi}"

  if [[ ! ${abis[*]} =~ ${device_product_abi} ]]; then
    abis+=("${device_product_abi}")
  fi
done <<< "$(adb devices | tail -n+2)"

if [[ ! "${abis[*]}" ]]; then
  log "ERROR" "no devices found. Check 'adb devices -l' output and share with Infra team if needed."
  exit 3
fi

log "DEBUG" "resulting abis:"
IFS=\;; echo "${abis[*]}"
