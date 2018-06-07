# Prerequisites:
lein, node.js v.8 , cmake, Qt 5.9.1 (with QtWebEngine components installed), Qt's qmake available in PATH

Note: add qmake to PATH via
`export PATH=<QT_PATH>/clang_64/bin:$PATH`

Caveats:
  - if npm hangs at some step, check the version. If it's 5.6.0, try downgrading to 5.5.1 via `npm install -g npm@5.5.1`

# To install react-native-cli with desktop commands support:
1. git clone https://github.com/status-im/react-native-desktop.git
2. cd react-native-desktop/react-native-cli
3. npm update
4. npm install -g

# To setup re-natal dev builds of status-react for Desktop:
1. git clone https://github.com/status-im/status-react.git
2. cd status-react
3. git checkout desktop
4. npm install
5. lein deps
6. ./re-natal use-figwheel
7. ./re-natal enable-source-maps
8. In separate terminal tab: `npm start` (note: it starts react-native packager )
9. In separate terminal tab: node ./ubuntu-server.js
10. In separate terminal tab: lein figwheel-repl desktop (note: wait until sources compiled)
11. In separate terminal tab: react-native run-desktop

# Editor setup
Running `lein figwheel-repl desktop` will run a REPL on port 7888 by default. Some additional steps might be needed to connect to it.

## emacs-cider
In order to get REPL working, use the below elisp code:
```
(defun custom-cider-jack-in ()
  (interactive)
  (let ((status-desktop-params "with-profile +figwheel repl"))
    (set-variable 'cider-lein-parameters status-desktop-params)
    (message "setting 'cider-lein-parameters")
    (cider-jack-in)))

(defun start-figwheel-cljs-repl ()
  (interactive)
  (set-buffer "*cider-repl status-react*")
  (goto-char (point-max))
  (insert "(do (use 'figwheel-api)
           (start [:desktop])
           (start-cljs-repl))")
  (cider-repl-return))
```

`custom-cider-jack-in` sets the correct profile for leiningen, and can be run as soon as emacs is open.
run `start-figwheel-cljs-repl` once you already have a cider repl session from the jack-in

## vim-fireplace
For some reason there is no `.nrepl-port` file in project root, so `vim-fireplace` will not be able to connect automatically. You can either:
  - run `:Connect` and answer a couple of interactive prompts
  - create `.nrepl-port` file manually and add a single line containing `7888` (or whatever port REPL is running on)

After Figwheel has connected to the app, run `:Piggieback (figwheel-sidecar.repl-api/repl-env)` inside Vim, and you should be all set.
