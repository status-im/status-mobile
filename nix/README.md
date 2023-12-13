# Description

This folder contains configuration for [Nix](https://nixos.org/), a purely functional package manager used by the Status app for its build process.

## Configuration

The main config file is [`nix/nix.conf`](/nix/nix.conf) and its main purpose is defining the [binary caches](https://nixos.org/nix/manual/#ch-basic-package-mgmt) which allow download of packages to avoid having to compile them yourself locally.

## Build arguments

We leverage the environment variables for parameterization of the builds (e.g. to pass a build number or build type).

Here is the list of variables you can use to override defaults:
```shell
# Build type (influences which .env file gets used for feature flags)
export BUILD_TYPE="pr"
# Used for versionCode and CFBundleVersion in Android and iOS respectively
export BUILD_NUMBER=9999
# Gradle options passed for Android builds
export ANDROID_GRADLE_OPTS=""
# If APKs should be split based on architectures
export ANDROID_ABI_SPLIT=false
# Android architectures to build for
export ANDROID_ABI_INCLUDE="x86"
# Local source override, see below
export STATUS_GO_SRC_OVERRIDE="$HOME/my/source/status-go"
export STATUS_GO_IPFS_GATEWAY_URL="https://ipfs.status.im/"
```
You can see the defaults in code:
- [`nix/status-go/default.nix`](./status-go/default.nix)
- [`nix/mobile/android/release.nix`](./mobile/android/release.nix)

## Shell

In order to access an interactive Nix shell a user should run `make shell`.

The Nix shell is started in this repo via the [`nix/scripts/shell.sh`](/nix/scripts/shell.sh) script, which is a wrapper around the `nix-shell` command and is intended for use with our main [`Makefile`](/Makefile). This allows for an implicit use of `nix-shell` as the default shell in the `Makefile`.

Normally the shell starts without any specific target platform, if you want to change that you should export the `TARGET` env variable with appropriate value:

```bash
make shell TARGET=android
```

This way your shell and all other nix commands should run in a setup that is tailored towards Android development.

For valid values you can check the [`nix/shells.nix`](/nix/shells.nix) file.

:warning: __WARNING__: To have Nix pick up all changes a new `nix-shell` needs to be spawned.

## Using a local status-go repository

If you need to use a locally checked-out status-go repository, you can achieve that by defining the `STATUS_GO_SRC_OVERRIDE`
environment variable:

```sh
export STATUS_GO_SRC_OVERRIDE=$GOPATH/src/github.com/status-im/status-go
make release-android
```

## Resources

You can learn more about Nix by watching these presentations:

* [Nix Fundamentals](https://www.youtube.com/watch?v=m4sv2M9jRLg) ([PDF](https://drive.google.com/file/d/1Tt5R7QOubudGiSuZIGxuFWB1OYgcThcL/view?usp=sharing), [src](https://github.com/status-im/infra-docs/tree/master/presentations/nix_basics))
* [Nix in Status](https://www.youtube.com/watch?v=rEQ1EvRG8Wc) ([PDF](https://drive.google.com/file/d/1Ti0wppMoj40icCPdHy7mJcQj__DeaYBE/view?usp=sharing), [src](https://github.com/status-im/infra-docs/tree/master/presentations/nix_in_status))

And you can read [`nix/DETAILS.md`](./DETAILS.md) for more information.

## Known Issues

See [`KNOWN_ISSUES.md`](./KNOWN_ISSUES.md).
