#!/usr/bin/env bash

# If this is not run on some machines react-native builds fail.

GIT_ROOT=$(cd "${BASH_SOURCE%/*}" && git rev-parse --show-toplevel)
source "$GIT_ROOT/scripts/lib/setup/path-support.sh"

source_lib "output.sh"
source_lib "platform.sh"

if ! is_linux; then
  echo "inotify fix not applicable on non-linux OS"
  return
fi

watches=$(cat /proc/sys/fs/inotify/max_user_watches)
required_watches=524288

if [ $watches -lt $required_watches ]; then
  cecho "@b@cyan[[fs.inotify.max_user_watches limit is too low ($watches), increasing it]]"
  echo fs.inotify.max_user_watches=$required_watches | sudo tee -a /etc/sysctl.conf
  sudo sysctl -p
fi
