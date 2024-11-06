## Solution to use Status Backend Server
`StatusBackendClient` is the entry point to use Status Backend Server. We need always to call `status-im.setup.status-backend-client/init` whether `STATUS_BACKEND_SERVER_ENABLED` is `1` or not. If it's not enabled, the invocation to functions in `native-module.core` will be delegated to built-in status-go library, otherwise it will be delegated to status-go running in status-backend server. Currently, all functions has usages in `native-module.core` should be supported delegated to.

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

### Known issues
- Android devices may not display images due to TLS certificate validation issues with the image server
- exportUnencryptedDatabaseV2/import-multiaccount does not work for android, probably cause of tech debt, I found it during creating the draft PR.
