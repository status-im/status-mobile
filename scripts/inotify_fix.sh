#!/usr/bin/env bash

# If this is not run on some machines react-native builds fail.

GIT_ROOT=$(cd "${BASH_SOURCE%/*}" && git rev-parse --show-toplevel)
source "${GIT_ROOT}/scripts/colors.sh"

if [[ "$(uname -s)" != Linux ]]; then
  echo "inotify fix not applicable on non-linux OS"
  exit
fi

watches=$(cat /proc/sys/fs/inotify/max_user_watches)
required_watches=524288

if [ $watches -lt $required_watches ]; then
  echo -e "${YLW}fs.inotify.max_user_watches limit is too low ($watches). Increasing it.${RST}"
  echo "fs.inotify.max_user_watches = $required_watches" | sudo tee -a /etc/sysctl.conf
  sudo sysctl -p
else
  echo -e "${GRN}fs.inotify.max_user_watches limit is high enough.${RST}"
fi
