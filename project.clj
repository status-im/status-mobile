(defproject status-im "0.1.0-SNAPSHOT"
  :url "https://github.com/status-im/status-react/"
  :license {:name "Eclipse Public License"
            :url  "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.9.0-RC1"]
                 [org.clojure/clojurescript "1.9.946"]
                 [org.clojure/core.async "0.3.443"]
                 [reagent "0.7.0" :exclusions [cljsjs/react cljsjs/react-dom cljsjs/react-dom-server cljsjs/create-react-class]]
                 [re-frame "0.10.2"]
                 [com.andrewmcveigh/cljs-time "0.5.0"]
                 [com.taoensso/timbre "4.10.0"]
                 [hickory "0.7.1"]
                 [com.cognitect/transit-cljs "0.8.243"]]
  :plugins [[lein-cljsbuild "1.1.7"]
            [lein-figwheel "0.5.14"]
            [lein-re-frisk "0.5.5"]]
  :clean-targets ["target/" "index.ios.js" "index.android.js"]
  :aliases {"prod-build"       ^{:doc "Recompile code with prod profile."}
            ["do" "clean"
             ["with-profile" "prod" "cljsbuild" "once" "ios"]
             ["with-profile" "prod" "cljsbuild" "once" "android"]]
            "figwheel-repl"    ["run" "-m" "clojure.main" "env/dev/run.clj"]
            "test-cljs"        ["with-profile" "test" "doo" "node" "test" "once"]
            "test-protocol"    ["with-profile" "test" "doo" "node" "protocol" "once"]
            "test-env-dev-utils" ["with-profile" "test" "doo" "node" "env-dev-utils" "once"]}
  :figwheel {:nrepl-port 7888}
  :profiles {:dev  {:dependencies [[figwheel-sidecar "0.5.14"]
                                   [re-frisk-remote "0.5.3"]
                                   [re-frisk-sidecar "0.5.4"]
                                   [com.cemerick/piggieback "0.2.2"]
                                   [hawk "0.2.11"]]
                    :source-paths ["src" "env/dev"]
                    :cljsbuild    {:builds
                                   {:ios
                                    {:source-paths ["react-native/src" "src"]
                                     :figwheel     true
                                     :compiler     {:output-to     "target/ios/app.js"
                                                    :main          "env.ios.main"
                                                    :output-dir    "target/ios"
                                                    :optimizations :none}}
                                    :android
                                    {:source-paths ["react-native/src" "src"]
                                     :figwheel     true
                                     :compiler     {:output-to     "target/android/app.js"
                                                    :main          "env.android.main"
                                                    :output-dir    "target/android"
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
                                                 :preamble      ["js/hook-require.js"]
                                                 :target        :nodejs}}
                                 {:id           "protocol"
                                  :source-paths ["src" "test/cljs"]
                                  :compiler     {:main          status-im.test.protocol.runner
                                                 :output-to     "target/test/test.js"
                                                 :output-dir    "target/test"
                                                 :optimizations :none
                                                 :target        :nodejs}}
                                 {:id           "env-dev-utils"
                                  :source-paths ["env/dev/env/utils.cljs" "test/env/dev"]
                                  :compiler     {:main          env.test.runner
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
                                 :android
                                 {:source-paths ["react-native/src" "src" "env/prod"]
                                  :compiler     {:output-to          "index.android.js"
                                                 :main               "env.android.main"
                                                 :output-dir         "target/android-prod"
                                                 :static-fns         true
                                                 :optimize-constants true
                                                 :optimizations      :simple
                                                 :closure-defines    {"goog.DEBUG" false}
                                                 :parallel-build     false
                                                 :language-in        :ecmascript5}
                                  :warning-handlers [status-im.utils.build/warning-handler]}}}}})
