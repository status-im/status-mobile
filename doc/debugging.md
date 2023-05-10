# Debugging

## Inspecting re-frame with re-frisk
`re-frisk` is a state visualization tool written by our very own Andrey (@flexsurfer). With its help you can inspect the current state of app-db, watch event, etc.

![re-frisk](images/debugging/re-frisk.png)

To start `re-frisk`, execute the following command:
```bash
$ yarn shadow-cljs run re-frisk-remote.core/start
```

or you can also use make:

```bash
$ make run-re-frisk
```

A server will be started at http://localhost:4567. It might show "not connected" at first. Don't worry and just start using the app. The events and state will populate.

More details about re-frisk are on the [project page](https://github.com/flexsurfer/re-frisk).

## Enabling debug logs
Calls to `log/debug` will not be printed to the console by default. It can be enabled under "Advanced settings" in the app:

![Enable Debug Logs](images/debugging/log-settings.png)


## Checking status-go logs
While status mobile works it saves logs from `status-go` to `geth.log` file.


### Checking logs from physical device
To obtain `geth.log` from physical device you need to shake it and in an opened menu select "Share logs". 

![Share logs](images/debugging/share-logs.jpeg)


### Checking logs from iOS Simulator
When developing with iOS simulator it is more convenient to see the `geth.log` updates in real-time.
To do this:
- open Activity Monitor
- find the "StatusIm" app and doubleclick it
- in the opened window select "Open files and ports" and find the full path to `geth.log` (note that it won't appear until you login to Status app)

![geth.log path](images/debugging/geth-path.png)


## Tips
### From @ilmotta:

Something I find extremely convenient for Android is to use `adb` to tail logs. I don't use macOS so I don't know if the iOS simulator offers a CLI interface with the same capabilities.

But here's what I use for example:

```
 adb shell tail -n 10 -f /storage/emulated/0/Android/data/im.status.ethereum.debug/files/Download/geth.log | grep 'waku.relay'
``` 

Also to inspect logs in a more flexible manner, instead of the strict output from `make run-metro`, I prefer `adb logcat`. Combined with enabling status-mobile logs in debug by default plus filtering the logs to only what I care during development, I find this helps me inspect the app without running re-frisk because with the debug log level I can already see which events are dispatched (one of the features I like the most from re-frisk).

```
adb logcat | grep 'ReactNativeJS\|StatusModule\|GoLog'
```
