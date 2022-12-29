# Testing

### Unit & integration tests

To run tests:

```
   make test
```


To watch the tests:

```
   make test-watch
```
  
To run test in REPL

```
   make test
   yarn shadow-cljs cljs-repl test # or start the REPL in your editor 
``` 

Then start the test process with

```
   node --require ./test-resources/override.js target/test/test.js --repl
```

You can run single test in REPL like this

```clojure
(require 'cljs.test)
(cljs.test/test-var #'status-im.data-store.chats-test/normalize-chat-test)
```



Tests will use the bindings in `modules/react-native-status/nodejs`, if you make any changes to these you will need to restart the watcher.
