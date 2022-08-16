#!/usr/bin/env nix-shell
#! nix-shell -i bash -p bash ruby bundler bundix
set -x
set -e

SCRIPTPATH="$( cd "$(dirname "$0")" ; pwd -P )"
GIT_ROOT=$(cd "${BASH_SOURCE%/*}" && git rev-parse --show-toplevel)

rm -f "${GIT_ROOT}/fastlane/Gemfile.lock"

bundler install \
  --gemfile="${GIT_ROOT}/fastlane/Gemfile" \
  --path "${GIT_ROOT}/fastlane/.bundle/vendor"
bundix \
  --gemfile="${GIT_ROOT}/fastlane/Gemfile" \
  --lockfile="${GIT_ROOT}/fastlane/Gemfile.lock" \
  --gemset="${GIT_ROOT}/fastlane/gemset.nix"

rm -rf "${GIT_ROOT}/fastlane/.bundle/vendor"

if [ "clean" == "$1" ]; then
  rm -r ~/.gem
fi
