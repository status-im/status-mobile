# Local testing

## Unit & integration tests

To run tests:

```
   make test
```



Also test watcher can be launched. It will re-run the entire test suite when any file is modified

```
   make test-watch
```

Developers can also manually change the shadow-cljs option `:ns-regex` to control which namespaces the test runner should pick. 

## Testing with REPL

The most convenient way to develop and run test locally is using REPL:

1. Run command `make test-watch-for-repl`.
3. Once you see the message `[repl] shadow-cljs - #3 ready!` you can connect a REPL to the `:test` target from VS Code, Emacs, etc.
4. In any test namespace, run [cljs.test/run-tests](https://cljs.github.io/api/cljs.test/#run-tests) or your preferred method to run tests in the current namespace.

You can run single test in REPL like this

```clojure
(require 'cljs.test)
(cljs.test/test-var #'status-im.data-store.chats-test/normalize-chat-test)
```



Tests will use the bindings in `modules/react-native-status/nodejs`, if you make any changes to these you will need to restart the watcher.



### Example in Emacs

In the video below, you can see two buffers side-by-side. On the left the source implementation, on the right the REPL buffer. Whenever a keybinding is pressed, **tests in the current namespace instantly run**. You can achieve this exact flow in VS Code, IntelliJ, Vim, etc.

[2022-12-19 12-46.webm](https://user-images.githubusercontent.com/46027/208465927-4ad9a935-5494-45e7-85b0-8134dc32d1a1.webm)

### Example in terminal emulator

Here I'm showing a terminal-only experience using Tmux (left pane Emacs, right pane the output coming from running the make target).

[2022-12-19 13-17.webm](https://user-images.githubusercontent.com/46027/208471199-1909c446-c82d-42a0-9350-0c15ca562713.webm)

## Component tests

To run tests:

```
   make component-test
```

Also test watcher can be launched. It will re-run the entire test suite when any file is modified

```
   make component-test-watch
```

Check [component tests doc](./component-tests-overview.md) for more.
