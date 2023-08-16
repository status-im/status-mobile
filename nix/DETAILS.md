# Description

This document describes the layout of our Nix setup.

# Folders

There are four main folders in the `nix` directory:

* [`nix/scripts`](./scripts) - Bash scripts for easier usage of Nix
* [`nix/pkgs`](./pkgs) - Packages we add to or modify in `nixpkgs`
* [`nix/deps`](./deps) - Project dependencies managed by Nix
* [`nix/lib`](./lib) - Our tools we merge into `pkgs.lib`
* [`nix/tools`](./tools) - Various tools used by our derivations and shells
* [`nix/status-go`](./status-go) - Derivations for building [`status-go`](https://github.com/status-im/status-go) repo

# Files

There are a few main files that define the whole build environment:

* [`nix/nix.conf`](./nix.conf) - Binary cache configuration
* [`nix/default.nix`](./default.nix) - Entry point for both shells and targets
* [`nix/shell.nix`](./shell.nix) - Definition of the default Nix shell
* [`nix/shells.nix`](./shells.nix) - Definitions of other Nix shells used in builds
* [`nix/targets.nix`](./targets.nix) - Hierarchy of main build targets
* [`nix/pkgs.nix`](./pkgs.nix) - Definition of a custom `nixpkgs` repo
* [`nix/overlay.nix`](./overlay.nix) - Overrides for `nixpkgs`, custom packages

The [`default.nix`](../default.nix) and [`shell.nix`](../shell.nix) files at th repo root are just a gateway into the `nix` sub folder.

# Scripts

There's a few scripts in [`nix/scripts`](./scripts) that make use of Nix simpler:

* [`nix/scripts/setup.sh`](./scripts/setup.sh) - Installs Nix Package manager
* [`nix/scripts/source.sh`](./scripts/source.sh) - Sources the Nix profile or installs Nix
* [`nix/scripts/build.sh`](./scripts/build.sh) - A wrapper around `nix-build` with sane defaults
* [`nix/scripts/shell.sh`](./scripts/shell.sh) - A wrapper around `nix-shell` for `Makefile`
* [`nix/scripts/clean.sh`](./scripts/clean.sh) - For cleaning Nix store after builds
* [`nix/scripts/purge.sh`](./scripts/purge.sh) - For purging everything Nix related from system

# Start

The starting point for using our Nix shells and targets is the [`default.nix`](/default.nix) file.

It pulls in all the `pkgs`, `targets` and `shells` defined in [`nix/default.nix`](/nix/default.nix). The point is easy access to them via commands like `nix-build` or `nix-shell`, which you'll see next.

# Shells

Normally shells are started using `make shell TARGET=default`, but that is essentially the same as calling:
```bash
nix-shell -A shells.default default.nix
```
The [`nix/scripts/shell.sh`](./scripts/shell.sh) script is essentially a wrapper around that command to make it usable as shell for the `Makefile`.

# Building

We will use the `make jsbundle` target as an example of a derivation you can build using Nix:

1. `make jsbundle` is called by developer
2. `make` calls `nix/scripts/build.sh targets.mobile.jsbundle`
3. [`build.sh`](/nix/scripts/build.sh) calls `nix-build --attr targets.mobile.jsbundle` with extra arguments
4. `nix-build` builds the derivation from [`nix/mobile/jsbundle/default.nix`](/nix/mobile/jsbundle/default.nix)

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
  --argstr secrets-file '/tmp/tmp-status-mobile-559a3a441/tmp.xAnrPuNtAP' \
  --option extra-sandbox-paths '/home/joe/.gradle/status-im.keystore /tmp/tmp-status-mobile-559a3a441/tmp.xAnrPuNtAP' \
  default.nix
```
Some of those are required which is why just calling:
```
nix-build --attr targets.mobile.android.release
```
Would fail.

# Garbage Collection

The `make nix-gc` target calls `nix-store --gc` and normally would remove almost everything, but to prevent that we place symlinks to protected derivations in `/nix/var/nix/gcroots` subfolder. Specifically:
```sh
_NIX_GCROOTS="${_NIX_GCROOTS:-/nix/var/nix/gcroots/per-user/${USER}/status-mobile}"
```
Whenever `nix/scripts/build.sh` or `nix/scripts/shell.sh` are called they update symlinks named after given targets in that folder. This in combination with `keep-outputs = true` set in `nix/nix.conf` prevents garbage collection from removing too much.
