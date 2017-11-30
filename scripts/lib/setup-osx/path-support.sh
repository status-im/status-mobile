#!/usr/bin/env bash

_current_dir=$(cd "${BASH_SOURCE%/*}" && pwd)
_scripts_dir=$(cd "$_current_dir/../.." && pwd)
_repo_dir=$(cd "$_scripts_dir/.." && pwd)

function scripts_path() {
  echo $_scripts_dir
}

function repo_path() {
  echo $_repo_dir
}

function source_lib() {
  local library_path=$1

  source "$_current_dir/$library_path"
}
