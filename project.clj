(defproject status-im "0.1.0-SNAPSHOT"
  :url "https://github.com/status-im/status-react/"
  :license {:name "Eclipse Public License"
            :url  "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.9.0-alpha17"]
                 [org.clojure/clojurescript "1.9.908"]
                 [org.clojure/core.async "0.3.443"]
                 [reagent "0.6.0" :exclusions [cljsjs/react cljsjs/react-dom cljsjs/react-dom-server]]
                 [re-frame "0.10.1"]
                 [com.andrewmcveigh/cljs-time "0.5.0"]
                 [com.taoensso/timbre "4.10.0"]
                 [hickory "0.7.1"]]
  :plugins [[lein-cljsbuild "1.1.7"]
            [lein-figwheel "0.5.14"]]
  :clean-targets ["index.ios.js" "index.desktop.js"]
  :aliases {"prod-build"       ^{:doc "Recompile code with prod profile."}
                               ["do" "clean"
                                ["with-profile" "prod" "cljsbuild" "once" "ios"]
                                ["with-profile" "prod" "cljsbuild" "once" "desktop"]]
            "test-cljs"        ["with-profile" "test" "doo" "node" "test" "once"]
            "test-protocol"    ["with-profile" "test" "doo" "node" "protocol" "once"]}
  :figwheel {:nrepl-port 7888}
  :profiles {:dev  {:dependencies [[figwheel-sidecar "0.5.11"]
                                   [com.cemerick/piggieback "0.2.2"]
                                   [hawk "0.2.11"]]
                    :source-paths ["src" "env/dev"]
                    :cljsbuild    {:builds
                                   {:ios
                                    {:source-paths ["react-native/src" "src" "env/dev"]
                                     :figwheel     true
                                     :compiler     {:output-to     "target/ios/app.js"
                                                    :main          "env.ios.main"
                                                    :output-dir    "target/ios"
                                                    :optimizations :none}}
                                    :desktop
                                    {:source-paths ["react-native/src" "src" "env/dev"]
                                     :figwheel     true
                                     :compiler     {:output-to     "target/desktop/app.js"
                                                    :main          "env.desktop.main"
                                                    :output-dir    "target/desktop"
                                                    :optimizations :none}
                                     :warning-handlers [status-im.utils.build/warning-handler]}}}
                    :repl-options {:nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]
                                   :timeout          240000}}
             :test {:dependencies [[day8.re-frame/test "0.1.5"]]
                    :plugins   [[lein-doo "0.1.7"]]
                    :cljsbuild {:builds
                                [{:id           "test"
                                  :source-paths ["src" "test/cljs"]
                                  :compiler     {:main          status-im.test.runner
                                                 :output-to     "target/test/test.js"
                                                 :output-dir    "target/test"
                                                 :optimizations :none
                                                 :target        :nodejs}}
                                 {:id           "protocol"
                                  :source-paths ["src" "test/cljs"]
                                  :compiler     {:main          status-im.test.protocol.runner
                                                 :output-to     "target/test/test.js"
                                                 :output-dir    "target/test"
                                                 :optimizations :none
                                                 :target        :nodejs}}]}}
             :prod {:cljsbuild {:builds
                                {:ios
                                 {:source-paths ["react-native/src" "src" "env/prod"]
                                  :compiler     {:output-to          "index.ios.js"
                                                 :main               "env.ios.main"
                                                 :output-dir         "target/ios-prod"
                                                 :static-fns         true
                                                 :optimize-constants true
                                                 :optimizations      :simple
                                                 :closure-defines    {"goog.DEBUG" false}
                                                 :parallel-build     false
                                                 :language-in        :ecmascript5}
                                  :warning-handlers [status-im.utils.build/warning-handler]}
                                 :desktop
                                 {:source-paths ["react-native/src" "src" "env/prod"]
                                  :compiler     {:output-to          "index.desktop.js"
                                                 :main               "env.desktop.main"
                                                 :output-dir         "target/desktop-prod"
                                                 :static-fns         true
                                                 :optimize-constants true
                                                 :optimizations      :simple
                                                 :closure-defines    {"goog.DEBUG" false}
                                                 :parallel-build     false
                                                 :language-in        :ecmascript5}
                                  :warning-handlers [status-im.utils.build/warning-handler]}}}}})
