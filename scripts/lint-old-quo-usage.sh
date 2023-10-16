#!/usr/bin/env sh

set -euo pipefail

QUO_USAGES=$(grep -r -E '\[quo\.[^ ]* :(?:as|refer)|\[quo\.[^ ]*\]' --include '*.cljs' --include '*.clj' './src/status_im2' --exclude='./src/status_im2/common/theme/core.cljs' || true)

echo -e "\nChecking 'status_im2' namespace for 'quo' namespace usage."

if [ -n "$QUO_USAGES" ]; then
    echo -e "\033[0;31mERROR: Usage of the old 'quo' namespace detected in 'status_im2' code. Please update to 'quo2'. \n"
    echo -e "$QUO_USAGES \033[0m"
    exit 1
fi
