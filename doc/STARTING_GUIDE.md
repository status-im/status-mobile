# Description

This document provides information on how to start developing Status App.

# Getting Started

To start developing start a shell for platform you are interested in.
```
make shell TARGET=android
```
This step will take a while the first time as it will download all dependencies.

# Development

There are three steps necessary to start development, in this case for Android:

1. `make run-clojure` - Compiles Clojure into JavaScript, watches for changes on cljs files, and hot-reloads code in the app
2. `make run-metro` - Starts metro bundler and watches JavaScript code
3. `make run-android` - Builds the Android app and starts it on the device

The first two will continue watching for changes and keep re-building the app. They need to be ready first.
The last one will exit once the app is up and ready.

You need to have your emulator or real devices running and visible to adb, before you run `make run-android`.

# Build release

To build the app, your can simply run on of the following:
```
make release-android
make release-ios
```

For more `make` targets run `make help`.

# Updating Dependencies

* `make nix-update-pods` - iOS CocoaPods dependencies (updates `ios/Podfile` and `ios/Podfile.loc`)
* `make nix-update-gradle` - Android Gradle/Maven dependencies (updates `nix/deps/gradle/deps.json`)
* `make nix-update-clojure` - Clojure Maven dependencies (updates `nix/deps/clojure/deps.json`)
* `make nix-update-gems` - Fastlane Ruby dependencies (updates `fastlane/Gemfile.lock` and `fastlane/gemset.nix`)

# Manual Steps

There are a few manual steps you might want to do in order to start contributing.

## Genymotion Virtualization

Optionally set up Genymotion if you don't want to use Android Virtual Device:

https://www.genymotion.com

## Android Development Environment

You can also setup Android Development Environment + Simulator:

https://facebook.github.io/react-native/docs/getting-started.html

## Configure GitHub Account

The optimal way of pushing to GitHubis using SSH instead of user/pass auth.

It's recommented that you [add your public SSH key to your GitHub account](https://help.github.com/en/github/authenticating-to-github/adding-a-new-ssh-key-to-your-github-account).

## Configure GPG Keys for signing commits

In order to increase security we require all commits in `status-react` repo to be signed with a GPG key.

Steps:
1. [Generate a new GPG key](https://help.github.com/en/github/authenticating-to-github/generating-a-new-gpg-key)
2. [Setup Git to use your GPG key](https://help.github.com/en/github/authenticating-to-github/telling-git-about-your-signing-key)
3. [Setup Git to sign commits](https://help.github.com/en/github/authenticating-to-github/signing-commits)
4. [Setup GitHub to validate commits](https://help.github.com/en/github/authenticating-to-github/adding-a-new-gpg-key-to-your-github-account)
