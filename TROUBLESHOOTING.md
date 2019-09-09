
Stacktrace:
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

Cause: 
- stacktrace mentions `register_handler_fx`, 
- common cause is when requires have been cleaned up and a require of `status-im.utils.handlers` namespace was removed because it looked like it was unused but was actually used through a fx/defn macro

Solution: 
go through known faulty commit looking for deleted requires
