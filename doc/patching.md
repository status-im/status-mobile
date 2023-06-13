# Patching

## Libraries
If 3rd party library has an issue and fix is not yet released (or we can't switch to a new release), we use forks. Fix should be commited to the fork, tagged and referenced from package.json.

Example: [`react-native-hole-view`](https://github.com/status-im/react-native-hole-view#refs/tags/v2.1.1-status)

## React Native
When patch need to be applied to React Native itself Status does patching with Nix instead of doing it nodejs-way.

Patches should be added to [this file](https://github.com/status-im/status-mobile/blob/develop/nix/deps/nodejs-patched/default.nix).

Example: [patching `react-native/Yoga` to build app with XCode 14.3](https://github.com/status-im/status-mobile/pull/15589)
