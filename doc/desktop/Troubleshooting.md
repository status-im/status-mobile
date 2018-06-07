These are some common issues you may run into while setting up React Native Qt.

## Initial setup issues

### `npm install` hangs
Downgrade to version 5.5.1: `npm install -g npm@5.5.1`.

### `re-natal` missing
Create a link:
`ln -sf node_modules/re-natal/index.js re-natal`


### `react-native run desktop` complaining about missing `qmldir`:
```Command failed: ./build.sh -e "node_modules/react-native-i18n/desktop;node_modules/react-native-config/desktop;node_modules/react-native-fs/desktop;node_modules/react-native-http-bridge/desktop;node_modules/react-native-webview-bridge/desktop;modules/react-native-status/desktop"
Error copying directory from "/path-to-status-react/node_modules/react-native/ReactQt/runtime/src/qmldir" to "/path-to-status-react/desktop/lib/React".
make[2]: *** [lib/CMakeFiles/copy-qmldir] Error 1
make[1]: *** [lib/CMakeFiles/copy-qmldir.dir/all] Error 2
make: *** [all] Error 2
```
Can be solved by re-running `npm install react-native` which put the `ReactQt/runtime/src/qmldir` file back.

### Missing web3 package issue

After last upgrade of react-native-desktop to the v.0.53.3 of original react-native appeared some incompatibility between `react-native` and `web3` packages on npm install. Initially it installed usually fine, but after `react-native desktop` command execution `web3` package is get removed from `node_modules`. Manual install of web3 by `npm install web3` installs `web3` package, but removes `react-native` package. Workaround or solution?

### Go problem
```
panic: runloop has just unexpectedly stopped

goroutine 50 [running]:
github.com/status-im/status-go/vendor/github.com/rjeczalik/notify.init.0.func1()
        /path-to-status-react/desktop/modules/react-native-status/desktop/StatusGo/src/github.com/status-im/status-go/vendor/github.com/rjeczalik/notify/watcher_fsevents_cgo.go:69 +0x79
created by github.com/status-im/status-go/vendor/github.com/rjeczalik/notify.init.0
        /path-to-status-react/desktop/modules/react-native-status/desktop/StatusGo/src/github.com/status-im/status-go/vendor/github.com/rjeczalik/notify/watcher_fsevents_cgo.go:65 +0x4e
events.js:183
      throw er; // Unhandled 'error' event
```
Related to https://github.com/rjeczalik/notify/issues/139. Solution: re-run.

## App issues

### Node server crashing
`node ./ubuntu_server.js` log:
```
DEBUG [status-im.utils.handlers:36] - Handling re-frame event:  :signal-event {"type":"node.crashed","event":{"error":"node is already running"}}
DEBUG [status-im.ui.screens.events:350] - :event-str {"type":"node.crashed","event":{"error":"node is already running"}}
DEBUG [status-im.utils.instabug:8] - Signal event: {"type":"node.crashed","event":{"error":"node is already running"}}
DEBUG [status-im.ui.screens.events:362] - Event  node.crashed  not handled
```
Solution: prevent starting Node when there is an instance already running.

### Reload JS - blank screen
Console log for `react-native run-desktop` shows error 533.
Solution: reload again. Still, might hang at `Signing you in...` step (due to node attempted to be restarted). Re-run Figwheel and `react-native run-desktop`

### ReactButton.qml non-existent property "elide" error upon startup
```
qrc:/qml/ReactButton.qml:33: Error: Cannot assign to non-existent property "elide"
"Component for qrc:/qml/ReactWebView.qml is not loaded"
QQmlComponent: Component is not ready
"Unable to construct item from component qrc:/qml/ReactWebView.qml"
"Can't create QML item for componenet qrc:/qml/ReactWebView.qml"
"RCTWebViewView" has no view for inspecting!
```
Reload JS does not help, restarting Figwheel/react-native might not as well. Restarting Metro bundler solved it for me.

### After login when several contacts are available: realm errors
1. `attempting to create an object of type 'chat'...`
2. `attempting to create an object of type 'transport'...`
3. Error text containing only the public key.
The realm stack trace follows.

### Error: spawn gnome-terminal ENOENT
In node server log:
```
ignoring exception: Error: read ECONNRESET
```
In react-native log:
```
./run-app.sh: line 72: 56660 Segmentation fault: 11  /path-to-status-react/desktop/bin/StatusIm $args
events.js:183
      throw er; // Unhandled 'error' event
      ^

Error: spawn gnome-terminal ENOENT
    at _errnoException (util.js:992:11)
    at Process.ChildProcess._handle.onexit (internal/child_process.js:190:19)
    at onErrorNT (internal/child_process.js:372:16)
    at _combinedTickCallback (internal/process/next_tick.js:138:11)
    at process._tickCallback (internal/process/next_tick.js:180:9)
```
or
```
StatusIm(7924,0x70000c1cd000) malloc: *** error for object 0x7f8b1539bd10: incorrect checksum for freed object - object was probably modified after being freed.
*** set a breakpoint in malloc_error_break to debug
./run-app.sh: line 72:  7924 Abort trap: 6           /path-to-status-react/desktop/bin/StatusIm $args
events.js:183
      throw er; // Unhandled 'error' event
      ^

Error: spawn gnome-terminal ENOENT
    at _errnoException (util.js:992:11)
    at Process.ChildProcess._handle.onexit (internal/child_process.js:190:19)
    at onErrorNT (internal/child_process.js:372:16)
    at _combinedTickCallback (internal/process/next_tick.js:138:11)
    at process._tickCallback (internal/process/next_tick.js:180:9)
```

### statusgo error during `react-native run-desktop`

```
Command failed: build(.)sh -e "node_modules/react-native-i18n/desktop;node_modules/react-native-config/desktop;node_modules/react-native-fs/desktop;node_modules/react-native-http-bridge/desktop;node_modules/react-native-webview-bridge/desktop;modules/react-native-status/desktop"
# github.com/status-im/status-go/vendor/github.com/ethereum/go-ethereum/crypto/bn256
../vendor/github.com/ethereum/go-ethereum/crypto/bn256/bn256_fast.go:26: syntax error: unexpected = in type declaration
../vendor/github.com/ethereum/go-ethereum/crypto/bn256/bn256_fast.go:30: syntax error: unexpected = in type declaration
# github.com/status-im/status-go/vendor/github.com/ethereum/go-ethereum/crypto/bn256
vendor/github.com/ethereum/go-ethereum/crypto/bn256/bn256_fast.go:26: syntax error: unexpected = in type declaration
vendor/github.com/ethereum/go-ethereum/crypto/bn256/bn256_fast.go:30: syntax error: unexpected = in type declaration
make[3]: *** [statusgo-library] Error 2
make[2]: *** [modules/react-native-status/desktop/StatusGo/src/github.com/status-im/src/StatusGo_ep-stamp/StatusGo_ep-configure] Error 2
make[1]: *** [modules/react-native-status/desktop/CMakeFiles/StatusGo_ep(.)dir/all] Error 2
make: *** [all] Error 2
```

### inotify errors

upon running `npm start` on linux, watchman may indicate: "The user limit on the total number of inotify watches was reached"

This can be fixed by running the below command. Note, changes will only be as valid as the current terminal session.

```
echo 999999 | sudo tee -a /proc/sys/fs/inotify/max_user_watches && echo 999999 | sudo tee -a
/proc/sys/fs/inotify/max_queued_events && echo 999999 | sudo tee -a /proc/sys/fs/inotify/max_user_instances &&
watchman shutdown-server && sudo sysctl -p
```

