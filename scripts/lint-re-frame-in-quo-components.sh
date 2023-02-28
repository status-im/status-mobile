#!/usr/bin/env sh

INVALID_CHANGES=$(grep -E -r '(re-frame/dispatch|rf/dispatch|re-frame/subscribe|rf/subscribe|rf/sub|<sub|>evt|status-im|i18n)' './src/quo2')

if test -n "$INVALID_CHANGES"; then
    echo "WARNING: re-frame, status-im and i18n are not allowed in quo2 components"
    echo ''
    echo "$INVALID_CHANGES"
    exit 1
fi
