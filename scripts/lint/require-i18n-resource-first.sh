#!/usr/bin/env sh

if rg --quiet --multiline '^\(ns.*\n^\s*\(:require\n(^\s*(;|#_).*\n)*(^\s*\[status-im2\.setup\.i18n-resources\W)' src/status_im2/core.cljs; then
  exit 0
elif [ $? -eq 1 ]; then
  echo "status-im2.setup.i18n-resources must be loaded first (be the first one in ns :require form) in status-im2.core"
  echo "For more info, check the comment here https://github.com/status-im/status-mobile/pull/17618#discussion_r1361275489"
else
  exit $?
fi
