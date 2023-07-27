#!/usr/bin/env bash

set -eof pipefail

FILES=$(comm -23 <(sort <(git ls-files --cached --others --exclude-standard)) <(sort <(git ls-files --deleted)) | grep --ignore-case -E '\.(java|cpp|nix|json|sh|md|js|clj|cljs|cljc|edn)$')
N_FILES=$(echo "$FILES" | wc -l)
LINT_SHOULD_FIX=0

if [[ -n $1 && $1 != '--fix' ]]; then
    echo "Unknown option '$1'" >&2
    exit 1
elif [[ $1 == '--fix' ]]; then
    LINT_SHOULD_FIX=1
fi

echo "Checking ${N_FILES} files for missing trailing newlines."

# Do not process the whole file and only check the last character. Ignore empty
# files. Taken from https://stackoverflow.com/a/10082466.
for file in $FILES; do
    if [ -s "$file" ] && [ "$(tail -c1 "$file"; echo x)" != $'\nx' ]; then
        if [[ $LINT_SHOULD_FIX -eq 1 ]]; then
            echo "" >>"$file"
        else
            LINT_ERROR=1
            echo "No trailing newline: $file" >&2
        fi
    fi
done

if [[ $LINT_ERROR -eq 1 ]]; then
    exit 1
fi
