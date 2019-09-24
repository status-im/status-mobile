# [DEPRECATED] Undefined is not an object evaluating `register_handler_fx`

## Deprecation note

This type of error should not occur anymore now that we require the namespace in the `fx.cljs` file. 

It can however happen with other macros requiring a cljs namespace. 

The general fix for that type of issue is to have two files for the namespace where your macros are defined, let's say for `my-project.my-macro` namespace you would have: 
- my_macro.cljs in which you need `(:require-macros my-project.my-macro)` and `(:require my-project.the-namespace-used-in-the-macro)`
- my_macro.clj in which you define the macro

That way you don't need to use any magical call like `find-ns` or inline `require` with some kind of call only once switch (which was the root cause of another bug in ``defstyle`` macro because the compilation phase at which the evaluation of the switch is done was not properly considered).

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
