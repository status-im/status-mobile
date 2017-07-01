(defproject status-im "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url  "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.9.0-alpha13"]
                 [org.clojure/clojurescript "1.9.671"]
                 [reagent "0.6.0" :exclusions [cljsjs/react cljsjs/react-dom cljsjs/react-dom-server]]
                 [re-frame "0.9.4"]
                 [natal-shell "0.3.0"]
                 [com.andrewmcveigh/cljs-time "0.4.0"]
                 [tailrecursion/cljs-priority-map "1.2.0"]
                 [com.taoensso/timbre "4.7.4"]
                 [com.google.guava/guava "21.0"]]
  :plugins [[lein-cljsbuild "1.1.4"]
            [lein-figwheel "0.5.8"]
            [lein-re-frisk "0.4.7"]
            [rasom/lein-externs "0.1.7"]]
  :clean-targets ["target/" "index.ios.js" "index.android.js"]
  :aliases {"prod-build" ^{:doc "Recompile code with prod profile."}
                         ["do" "clean"
                          ["with-profile" "prod" "cljsbuild" "once" "ios"]
                          ["with-profile" "prod" "cljsbuild" "once" "android"]]
            "generate-externs" ["with-profile" "prod" "externs" "android" "externs/externs.js"]
            "test" ["doo" "phantom" "test" "once"]}
  :test-paths ["test/clj"]
  :figwheel {:nrepl-port 7888}
  :profiles {:dev  {:dependencies [[figwheel-sidecar "0.5.8"]
                                   [re-frisk-remote "0.4.2"]
                                   [re-frisk-sidecar "0.4.5"]
                                   [com.cemerick/piggieback "0.2.1"]
                                   [io.appium/java-client "3.4.1"]
                                   [hawk "0.2.10"]]
                    :plugins      [[lein-doo "0.1.7"]]
                    :source-paths ["src" "env/dev"]
                    :cljsbuild    {:builds [{:id           :ios
                                             :source-paths ["src" "env/dev"]
                                             :figwheel     true
                                             :compiler     {:output-to     "target/ios/app.js"
                                                            :main          "env.ios.main"
                                                            :output-dir    "target/ios"
                                                            :optimizations :none}}
                                            {:id           :android
                                             :source-paths ["src" "env/dev"]
                                             :figwheel     true
                                             :compiler     {:output-to     "target/android/app.js"
                                                            :main          "env.android.main"
                                                            :output-dir    "target/android"
                                                            :optimizations :none}}
                                            {:id           "test"
                                             :source-paths ["src" "test/cljs"]
                                             :compiler
                                                           {:main          status-im.test.runner
                                                            :output-to     "target/test/test.js"
                                                            :output-dir    "target"
                                                            :optimizations :none}}]}
                    :repl-options {:nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]
                                   :timeout          240000}}
             :prod {:cljsbuild {:builds [{:id           "ios"
                                          :source-paths ["src" "env/prod"]
                                          :compiler     {:output-to          "index.ios.js"
                                                         :main               "env.ios.main"
                                                         :output-dir         "target/ios-prod"
                                                         :static-fns         true
                                                         :optimize-constants true
                                                         :optimizations      :advanced
                                                         :externs            ["externs/externs.js"]
                                                         :closure-defines    {"goog.DEBUG" false}
                                                         :parallel-build true}}
                                         {:id           "android"
                                          :source-paths ["src" "env/prod"]
                                          :compiler     {:output-to          "index.android.js"
                                                         :main               "env.android.main"
                                                         :output-dir         "target/android-prod"
                                                         :static-fns         true
                                                         :optimize-constants true
                                                         :optimizations      :advanced
                                                         :externs            ["externs/externs.js"]
                                                         :closure-defines    {"goog.DEBUG" false}
                                                         :parallel-build true}}]}}})
