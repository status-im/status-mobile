# [DEPRECATED] Undefined is not an object evaluating `register_handler_fx`

## Deprecation note

This type of error should not occur anymore now that we require the namespace in the `fx.cljs` file. 

It can however happen with other macros requiring a cljs namespace. 

The general fix for that type of issue is to have two files for the namespace where your macros are defined, let's say for `my-project.my-macro` namespace you would have: 
- my_macro.cljs in which you need `(:require-macros my-project.my-macro)` and `(:require my-project.the-namespace-used-in-the-macro)`
- my_macro.clj in which you define the macro

That way you don't need to use any magical call like `find-ns` or inline `require` with some kind of call only once switch (which was the root cause of another bug in ``defstyle`` macro because the compilation phase at which the evaluation of the switch is done was not properly considered).

You also want to make sure users are using the macro by using a aliased namespace defined in require statement rather than require-macro and refer to the macro directly. Otherwise it won't require the cljs file and the require statement of the namespace in the macroexpension might not be there.

## Stacktrace

```
13:25:22, Requiring: hi-base32
13:25:23, Possible Unhandled Promise Rejection (id: 0):
TypeError: undefined is not an object (evaluating 'status_im.utils.handlers.register_handler_fx')
eval code
eval@[native code]
asyncImportScripts$@http://localhost:8081/index.bundle?platform=ios&dev=true&minify=false:200728:21
tryCatch@http://localhost:8081/index.bundle?platform=ios&dev=true&minify=false:26567:23
invoke@http://localhost:8081/index.bundle?platform=ios&dev=true&minify=false:26742:32
tryCatch@http://localhost:8081/index.bundle?platform=ios&dev=true&minify=false:26567:23
invoke@http://localhost:8081/index.bundle?platform=ios&dev=true&minify=false:26643:30
http://localhost:8081/index.bundle?platform=ios&dev=true&minify=false:26653:21
tryCallOne@http://localhost:8081/index.bundle?platform=ios&dev=true&minify=false:3725:16
http://localhost:8081/index.bundle?platform=ios&dev=true&minify=false:3826:27
_callTimer@http://localhost:8081/index.bundle?platform=ios&dev=true&minify=false:28405:17
_callImmediatesPass@http://localhost:8081/index.bundle?pla<…>
```

## Cause

- stacktrace mentions `register_handler_fx`, 
- common cause is when requires have been cleaned up and a require of `status-im.utils.handlers` namespace was removed because it looked like it was unused but was actually used through a fx/defn macro

## Solution

go through known faulty commit looking for deleted requires


# Git "unable to access" errors during `yarn install`

## Description
Developer updates `package.json` file with a new dependency using a GitHub URL. So it looks like this:
```
  "react-native-status-keycard": "git+https://github.com/status-im/react-native-status-keycard.git#feature/exportKeyWithPath",
```
Afterwards, when running e.g. `make react-native-android`, they might see the following confusing error:
```
# macOS
fatal: unable to access 'https://github.com/siphiuel/react-native-status-keycard.git/': SSL certificate problem: unable to get local issuer certificate
# Linux
fatal: unable to access 'https://github.com/status-im/react-native-status-keycard.git/': Could not resolve host: github.com
info Visit https://yarnpkg.com/en/docs/cli/install for documentation about this command.
```

## Cause
`yarn.lock` is not updated to be in sync with `package.json`.

## Solution
Update yarn.lock file. In order to do this, perform the following steps on a clean `status-react` repo:
```
cd status-react
ln -sf mobile/js_files/package.json .
ln -sf mobile/js_files/yarn.lock .
yarn install
```
and don't forget to commit updated `yarn.lock` together with `package.json`.


# adb server/client version mismatch errors

## Description
Running some adb commands, e.g. `adb devices` or `make android-ports` (in turn invokes `adb reverse`/`adb forward` commands) may display the following message:
```
adb server version (40) doesn't match this client (41); killing...
```
or the reverse
```
adb server version (41) doesn't match this client (40); killing...
```

This might cause all kinds of difficult-to-debug errors, e.g.:
  -  not being able to find the device through `adb devices`
  -  `make run-android` throwing `com.android.builder.testing.api.DeviceException: com.android.ddmlib.InstallException: device 'device-id' not found.`
  -  `make run-android` throwing `- Error: Command failed: ./gradlew app:installDebug -PreactNativeDevServerPort=8081 Unable to install /status-react/android/app/build/outputs/apk/debug/app-debug.apk com.android.ddmlib.InstallException: EOF`
  - dropped CLJS repl connections (that have been enabled previously with the help of `make android-ports`)

## Cause
System's local adb and Nix's adb differ. As adb include of server/client processes, this can cause subtle version errors that cause adb to kill mismatching server processes.

## Solution
Always use respective `make` commands, e.g. `make android-ports`, `make android-devices`, etc.

Alternatively, run adb commands only from `make shell TARGET=android` shell. Don't forget the `TARGET=android` env var setting - otherwise `adb` will still be selected from the system's default location. You can double-check this by running `which adb`.
