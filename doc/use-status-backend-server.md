## What is Status Backend Server
Status Backend Server is a http server that runs status-go, it exposed endpoints come from [mobile/status.go](https://github.com/status-im/status-go/blob/master/mobile/status.go) so that you can call them through http requests. It brings the following benefits:
- When verifying changes made in status-go, you only need to run these commands once (unless modifying Kotlin/Objective-C code):
  - `make clean && make run-clojure` 
  - `make run-android` or `make run-ios`
  This avoids repeatedly rebuilding and saves development time.
- Debug status-go while running status mobile app

## Solution to use Status Backend Server for mobile development
`StatusBackendClient` is the entry point to use Status Backend Server. We need always to call `status-im.setup.status-backend-client/init` whether `STATUS_BACKEND_SERVER_ENABLED` is `1` or not. If it's not enabled, the invocation to functions in `native-module.core` will be delegated to built-in status-go library, otherwise it will be delegated to status-go running in status-backend server. Currently, all functions has usages in `native-module.core` should be supported delegated to. 
NOTE: not all the native functions used `StatusBackendClient`, only the corresponding functions in `native-module.core` has usages should be supported delegated to ATM.

related [PR](https://github.com/status-im/status-mobile/pull/21550)

## Usage
### Add environment variables to your local machine:
```shell
# enable using status backend server or not, otherwise it will use built-in status-go library
export STATUS_BACKEND_SERVER_ENABLED=1

#The host should contain an IP address and a port separated by a colon. 
#The port comes from your running status backend server. 
#If you run it by PORT=60000 make run-status-backend , then host will likely be 127.0.0.1:60000
export STATUS_BACKEND_SERVER_HOST="127.0.0.1:60000" 

export STATUS_BACKEND_SERVER_ROOT_DATA_DIR="/path/to/your/root/data/dir" 
```
You need to change `STATUS_BACKEND_SERVER_ROOT_DATA_DIR` to your preferred directory and ensure it exists, it should be in absolute path.
All the db files and log files(requests.log/geth.log) and keystore files etc will be stored in this directory.

### Start the status backend server:
```shell
PORT=60000 make run-status-backend
```
MAKE SURE the status-backend is checked out to a revision that's at least compatible with the revision in status-mobile/status-go-version.json before starting the server.

For the Android simulator, you need to reverse the port:
```shell
adb reverse tcp:60000 tcp:60000
```
However, there is restriction when use adb reverse, we use random port for media server. So I'd suggest use "10.0.2.2:60000" as `STATUS_BACKEND_SERVER_HOST`.

### Debug status-go using IDEA
Assume you've already set up the development environment for status-go, open status-go project use IDEA, run `make generate` with terminal in the status-go project root directory to ensure all the generated files are up to date, then open `cmd/status-backend/main.go` file, navigate to function `main()`, click the green play button on the left of `main()`, choose `Modify Run Configuration`, in `Program arguments` section, add `--address=localhost:60000 --media-https=false`, then click `OK`, finally click the green play button on the left of `main()` again and choose `Debug ...`.
Basically, you don't have to run `make generate` again and again, just run it once at the beginning. So you can re-run and debug it faster!

## Known Android simulator issues
- Issue#1: Android simulator may not display images due to TLS certificate validation issues with the image server
  - solution: use http instead of https for media server with set env: `export STATUS_BACKEND_SERVER_MEDIA_SERVER_ENABLE_TLS=0`, you also need to set env variable `STATUS_BACKEND_SERVER_IMAGE_SERVER_URI_PREFIX` to "http://10.0.2.2:" so that `image_server.cljs` can work, and to make `/accountInitials` work, you need to copy `Inter-Medium.ttf` to your host machine from the android simulator, let's say you store it in `/Users/mac/Downloads/Inter-Medium.ttf`, then you need to update `get-font-file-ready` manually in `image_server.cljs` to return the correct path so that status backend server can access it.
- Issue#2: exportUnencryptedDatabaseV2/import-multiaccount does not work for android, probably cause of tech debt, I found it during creating the draft PR.
- Issue#3: unable to invoke `multiaccounts_storeIdentityImage` to change avatar image.
  - The reason is that we path the absolute path of the image to the backend server, but the image file is stored in the android simulator. the backend server cannot access it as it runs in the host machine.

If you're using ios simulator, you can skip above issues!

## Details for issue#1 if you're interested
- we use `react-native-fast-image` which use okhttpclient behind
- we were using custom cert for https
- we fetch the custom cert through endpoint `ImageServerTLSCert`
- we fetched it through built-in status-go before, now we need to fetch it through status backend server
- we expect `OkHttpClientProvider.setOkHttpClientFactory(StatusOkHttpClientFactory())` to be invoked early(will trigger fetching wrong custom cert via built-in status-go since StatusBackendClient is not initialised yet!) in `MainApplication.kt` before executing clojure code, otherwise we will get black screen after `make run-android` . After deep research, I found there's no way to update the cert okhttpclient used to the correct one return from status backend server
