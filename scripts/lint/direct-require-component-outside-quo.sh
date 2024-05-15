#!/usr/bin/env sh

# quo components namespace ends with style|utils|types|reaction-resource
# are not the component view, they are usually utils fns or styles
#
# We also ignore preview namespaces from this linter because they may require
# ?schema vars to auto-generate preview descriptors.

if rg --pcre2 --glob '!src/quo/**/*' --glob '!src/status_im/contexts/preview/quo/**/*' 'quo\.components(\.[\w-]+)*\.[\w-]*(?<!style|utils|types|reaction-resource)(\s|\]|\n)' src; then
  echo "Found above direct require of quo component outside src/quo/"
  echo "For more info, check the style guide https://github.com/status-im/status-mobile/blob/develop/doc/new-guidelines.md#requiring-quo-components"
  exit 1
elif [ $? -eq 1 ]; then
  exit 0
else
  exit $?
fi
