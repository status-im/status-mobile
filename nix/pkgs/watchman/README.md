# Issue

`watchman` was upgraded significantly during the last nixpkgs upgrade (`4.9.0` (Aug 16, 2017) to `2023.01.30.00` - 6 years between):
- https://github.com/status-im/status-mobile/pull/14944
- https://github.com/status-im/nixpkgs/commit/4e9c02bcc709fe1737a746add0e8e0109133d808

Probably causing developers to have "too many files open" issue:
https://github.com/status-im/status-mobile/issues/16341

```
Error: A non-recoverable condition has triggered.  Watchman needs your help!
The triggering condition was at timestamp=1687286390: opendir(/Users/javid/Projects/status-mobile/node_modules/metro-core/node_modules/jest-regex-util/build) -> Too many open files
All requests will continue to fail with this message until you resolve
the underlying problem.  You will find more information on fixing this at
https://facebook.github.io/watchman/docs/troubleshooting.html#poison-opendir
```

# Fix
This is an attempt to fix the issue by downgrading the watchman

# Upgrade
When upgrading in the future, please read the comments:
https://github.com/status-im/status-mobile/issues/16341
