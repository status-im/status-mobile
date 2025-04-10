#!/usr/bin/env sh

INVALID_CHANGES=$(grep -E -r '(/atom|re-frame/dispatch|rf/dispatch|re-frame/subscribe|rf/subscribe|rf/sub|<sub|>evt|status-im\.)' --include '*.cljs' --include '*.clj' './src/quo')

if test -n "$INVALID_CHANGES"; then
    echo "WARNING: re-frame, status-im, reagent atoms are not allowed in quo components"
    echo ''
    echo "$INVALID_CHANGES"
    exit 1
fi

# Add exception for status-im.config in the utils package check
INVALID_CHANGES2=$(grep -E -r '(status-im\.)' --include '*.cljs' --include '*.clj' './src/utils' | grep -v 'status-im.config')

if test -n "$INVALID_CHANGES2"; then
    echo "WARNING: status-im are not allowed in utils package"
    echo ''
    echo "$INVALID_CHANGES2"
    exit 1
fi

INVALID_CHANGES3=$(grep -E -r '(status-im\.)' --include '*.cljs' --include '*.clj' './src/react_native')

if test -n "$INVALID_CHANGES3"; then
    echo "WARNING: status-im are not allowed in react-native package"
    echo ''
    echo "$INVALID_CHANGES3"
    exit 1
fi
