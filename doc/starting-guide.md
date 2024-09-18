# Description

This document provides information on how to start developing Status App.

# Getting Started

To start developing clone the status-mobile repo in the directory of your choice. 
```
git clone https://github.com/status-im/status-mobile.git
```

Then open a terminal and cd into this directory. 

```
cd status-mobile
```

Then build the clojure terminal 

```
make run-clojure
```

note: ⚠️ This might take a while if this is your first time.

This command installs `nix` and pulls in all the dependencies.
Do answer with "Y" to all the prompts and press "Enter" when `nix` setup asks you to Acknowledge.
This command builds the `jsbundle` and then compiles `Clojure` into `JavaScript`, watches for changes on `cljs` files, and hot-reloads code in the app.

wait till you see the following message :

```
[:mobile] Build completed. (1801 files, 52 compiled, 0 warnings, 9.52s)
```

Once the clojure terminal is running you need to run the appropriate command next for your platform in a separate terminal :

`make run-android` or `make run-ios`

These commands will build the app, start a metro bundler and deploy the app on your simulator OR connected device (android only). For building and deploying to connected iPhones use `make run-ios-device` instead of `make run-ios`
Also check [developing on a physical iOS Device](#Additional-requirements-for-developing-on-physical-ios-device).

## Simulators and Devices
### Android

You need to have an emulator like [AVD](https://developer.android.com/studio/run/emulator), or [Genymotion](#genymotion-virtualization), or a real device running and visible to [adb](https://developer.android.com/studio/command-line/adb), before you run `make run-android`.

### iOS

#### Running on a simulator

We highly recommend using either the `iPhone 11 Pro` or `iPhone 13` simulator as its screen dimensions match with our design.

If you have Xcode `v12.x` (and above) installed in your system, you need to follow the below steps to add `iPhone 13` simulator:

1. Open Xcode
2. Menu `>` Window `>` Devices and Simulators
3. Tap `+` button on bottom left
4. Select **Device Type** as `iPhone 13`
5. Leave the **Simulator Name** empty and tap on **Create**

##### NOTE ⚠️

Running `make run-ios` will target `iPhone 13` by default.

If you need to run on any other simulator, you can specify the simulator type by adding the `SIMULATOR` flag:
```sh
make run-ios SIMULATOR="iPhone 15"
```

# Build release

To build the app, you can simply run on of the following:
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

The optimal way of pushing to GitHub is using SSH instead of user/pass auth.

It's recommended that you [add your public SSH key to your GitHub account](https://help.github.com/en/github/authenticating-to-github/adding-a-new-ssh-key-to-your-github-account).

## Configure GPG Keys for signing commits

In order to increase security we require all commits in `status-mobile` repo to be signed with a GPG key.

Steps:
1. [Generate a new GPG key](https://help.github.com/en/github/authenticating-to-github/generating-a-new-gpg-key)
2. [Setup Git to use your GPG key](https://help.github.com/en/github/authenticating-to-github/telling-git-about-your-signing-key)
3. [Setup Git to sign commits](https://help.github.com/en/github/authenticating-to-github/signing-commits)
4. [Setup GitHub to validate commits](https://help.github.com/en/github/authenticating-to-github/adding-a-new-gpg-key-to-your-github-account)

## Additional requirements for developing on Physical iOS Device

To use a physical iPhone your device UDID must be added to provisioning profiles and your Apple account invited as Developer to Status team.

1. [Get your UDID of your iPhone.](https://www.extentia.com/post/finding-the-udid-of-an-ios-device)
2. Request from someone with access like @cammellos or @jakubgs to
- Add the UDID to development devices on Apple Developer Portal.
- Invite your Apple account to be Developer in Status team.
3. Open XCode using the project from `status-mobile/ios` directory.
- You might see error: `Select a development team in the Signing & Capabilities editor`
- Select `Status Research & Development GmbH` as the development team.
4. In a new terminal execute `make clean` and then `make xcode-clean` 
