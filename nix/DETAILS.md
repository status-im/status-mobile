# Description

This document descripts the layout of our Nix setup.

# Folders

There are four main folders in the `nix` directory:

* `nix/scripts` - Bash scripts for easier usage of Nix
* `nix/pkgs` - Packages we add to or modify in `nixpkgs`
* `nix/tools` - Various tools used by our derivations and shells
* `nix/status-go` - Derivations for building [`status-go`](https://github.com/status-im/status-go) repo

# Files

There are a few main files that define the whole build environment:

* `nix/default.nix` - Entrypoint for both shells and targets
* `nix/shells.nix` - Definition of Nix shells used in builds
* `nix/targets.nix` - Hierarchy of main build targets
* `nix/pkgs.nix` - Definition of a custom `nixpkgs` repo
* `nix/overlay.nix` - Overrides for `nixpkgs`, custom packages

# Start

The starting point for using our Nix shells and targets is the [`default.nix`](/default.nix) file.

It pulls in all the `pkgs`, `targets` and `shells` defined in [`nix/default.nix`](/nix/default.nix). The point is easy access to them via commands like `nix-build` or `nix-shell`, which you'll see next.

# Usage

We will use the `make jsbundle-android` target as an example of a derivation you can build using Nix:

1. `make jsbundle-android` is called by developer
2. `make` calls `nix/scripts/build.sh targets.mobile.android.jsbundle`
3. [`build.sh`](/nix/scripts/build.sh) calls `nix-build --attr targets.mobile.android.jsbundle` with extra arguments
4. `nix-build` builds the derivation from [`nix/mobile/android/jsbundle/default.nix`](/nix/mobile/android/jsbundle/default.nix)

The same can be done for other targets like `targets.mobile.android.release`.
Except in that case extra arguments are required which is why the [`scripts/release-android.sh`](/scripts/release-android.sh) is used in the `make release-android` target.

If you run `make release-android` you'll see the `nix-build` command used:
```
nix-build \
  --pure \
  --fallback \
  --no-out-link \
  --show-trace \
  --attr targets.mobile.android.release \
  --argstr secrets-file '/tmp/tmp-status-react-559a3a441/tmp.xAnrPuNtAP' \
  --option extra-sandbox-paths '/home/sochan/.gradle/status-im.keystore /tmp/tmp-status-react-559a3a441/tmp.xAnrPuNtAP' \
  --arg config '{ \
    status-im.build-type="nightly";
    status-im.build-number="2020022418";
    status-im.android.keystore-file="/home/sochan/.gradle/status-im.keystore";
    status-im.android.abi-split="false";
    status-im.android.abi-include="armeabi-v7a;arm64-v8a;x86";
  }' \
  default.nix
```
Some of those are required which is why just calling:
```
nix-build --attr targets.mobile.android.release
```
Would fail.
