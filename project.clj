(defproject status-im "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url  "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.9.0-alpha13"]
                 [org.clojure/clojurescript "1.9.671"]
                 [org.clojure/core.async "0.3.443"]
                 [reagent "0.6.0" :exclusions [cljsjs/react cljsjs/react-dom cljsjs/react-dom-server]]
                 [re-frame "0.9.4"]
                 [com.andrewmcveigh/cljs-time "0.5.0"]
                 [tailrecursion/cljs-priority-map "1.2.0"]
                 [com.taoensso/timbre "4.7.4"]
                 [com.google.guava/guava "21.0"]]
  :plugins [[lein-cljsbuild "1.1.6"]
            [lein-figwheel "0.5.11"]
            [lein-re-frisk "0.4.7"]
            [rasom/lein-externs "0.1.7"]]
  :clean-targets ["target/" "index.ios.js" "index.android.js"]
  :aliases {"prod-build"       ^{:doc "Recompile code with prod profile."}
                               ["do" "clean"
                                ["with-profile" "prod" "cljsbuild" "once" "ios"]
                                ["with-profile" "prod" "cljsbuild" "once" "android"]]
            "generate-externs" ["with-profile" "prod" "externs" "android" "externs/externs.js"]
            "test-cljs"        ["with-profile" "test" "doo" "node" "test" "once"]
            "test-protocol"    ["with-profile" "test" "doo" "node" "protocol" "once"]}
  :test-paths ["test/clj"]
  :figwheel {:nrepl-port 7888}
  :profiles {:dev  {:dependencies [[figwheel-sidecar "0.5.11"]
                                   [re-frisk-remote "0.4.2"]
                                   [re-frisk-sidecar "0.4.5"]
                                   [com.cemerick/piggieback "0.2.1"]
                                   [io.appium/java-client "3.4.1"]
                                   [hawk "0.2.10"]]
                    :source-paths ["src" "env/dev"]
                    :cljsbuild    {:builds
                                   {:ios
                                    {:source-paths ["react-native/src" "src" "env/dev"]
                                     :figwheel     true
                                     :compiler     {:output-to     "target/ios/app.js"
                                                    :main          "env.ios.main"
                                                    :output-dir    "target/ios"
                                                    :optimizations :none}}
                                    :android
                                    {:source-paths ["react-native/src" "src" "env/dev"]
                                     :figwheel     true
                                     :compiler     {:output-to     "target/android/app.js"
                                                    :main          "env.android.main"
                                                    :output-dir    "target/android"
                                                    :optimizations :none}}}}
                    :repl-options {:nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]
                                   :timeout          240000}}
             :test {:dependencies [[day8.re-frame/test "0.1.5"]]
                    :plugins   [[lein-doo "0.1.7"]]
                    :cljsbuild {:builds
                                [{:id           "test"
                                  :source-paths ["src" "test/cljs"]
                                  :compiler
                                                {:main          status-im.test.runner
                                                 :output-to     "target/test/test.js"
                                                 :output-dir    "target/test"
                                                 :optimizations :none
                                                 :target        :nodejs}}
                                 {:id           "protocol"
                                  :source-paths ["src" "test/cljs"]
                                  :compiler
                                                {:main          status-im.test.protocol.runner
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
                                                 :externs            ["externs/externs.js"]
                                                 :closure-defines    {"goog.DEBUG" false}
                                                 :parallel-build     true}}
                                 :android
                                 {:source-paths ["react-native/src" "src" "env/prod"]
                                  :compiler     {:output-to          "index.android.js"
                                                 :main               "env.android.main"
                                                 :output-dir         "target/android-prod"
                                                 :static-fns         true
                                                 :optimize-constants true
                                                 :optimizations      :simple
                                                 :externs            ["externs/externs.js"]
                                                 :closure-defines    {"goog.DEBUG" false}
                                                 :parallel-build     true}}}}}})
