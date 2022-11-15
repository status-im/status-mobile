#!/usr/bin/env sh

CHANGES=$(git diff --no-ext-diff --diff-filter=d --cached --unified=0 --no-prefix --minimal --exit-code src/quo src/quo2 | grep '^+' || echo '')

INVALID_CHANGES=$(echo "$CHANGES" | grep -E '(re-frame/dispatch|rf/dispatch|re-frame/subscribe|rf/subscribe|rf/sub|<sub|>evt|status-im)')

if test -n "$INVALID_CHANGES"; then
    echo "re-frame dispatch/subscribe ans status-im are not allowed in quo/quo2 components"
    echo ''
    echo "$INVALID_CHANGES"
    exit 1
fi
