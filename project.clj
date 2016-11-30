(defproject status-im "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url  "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.9.0-alpha13"]
                 [org.clojure/clojurescript "1.9.229"]
                 [reagent "0.6.0" :exclusions [cljsjs/react cljsjs/react-dom cljsjs/react-dom-server]]
                 [re-frame "0.7.0"]
                 [natal-shell "0.3.0"]
                 [com.andrewmcveigh/cljs-time "0.4.0"]
                 [tailrecursion/cljs-priority-map "1.2.0"]
                 [com.taoensso/timbre "4.7.4"]]
  :plugins [[lein-cljsbuild "1.1.4"]
            [lein-figwheel "0.5.8"]]
  :clean-targets ["target/" "index.ios.js" "index.android.js"]
  :aliases {"prod-build" ^{:doc "Recompile code with prod profile."}
                         ["do" "clean"
                          ["with-profile" "prod" "cljsbuild" "once" "ios"]
                          ["with-profile" "prod" "cljsbuild" "once" "android"]]}
  :test-paths ["test/clj"]
  :figwheel {:nrepl-port 7888}
  :profiles {:dev  {:dependencies [[figwheel-sidecar "0.5.8"]
                                   [com.cemerick/piggieback "0.2.1"]
                                   [io.appium/java-client "3.4.1"]
                                   [hawk "0.2.10"]]
                    :plugins      [[lein-doo "0.1.6"]]
                    :source-paths ["src" "env/dev"]
                    :cljsbuild    {:builds [{:id           :ios
                                             :source-paths ["src" "env/dev"]
                                             :figwheel     true
                                             :compiler     {:output-to     "target/ios/not-used.js"
                                                            :main          "env.ios.main"
                                                            :output-dir    "target/ios"
                                                            :optimizations :none}}
                                            {:id           :android
                                             :source-paths ["src" "env/dev"]
                                             :figwheel     true
                                             :compiler     {:output-to     "target/android/not-used.js"
                                                            :main          "env.android.main"
                                                            :output-dir    "target/android"
                                                            :optimizations :none}}
                                            {:id           :android-test
                                             :source-paths ["src" "env/dev"]
                                             :compiler     {:output-to     "target/android/not-used.js"
                                                            :main          "env.android-test.main"
                                                            :output-dir    "target/android-test"
                                                            :optimizations :none}}
                                            {:id           :test
                                             :source-paths ["src" "test/cljs"]
                                             :compiler
                                                           {:main          status-im.test.runner
                                                            :output-to     "target/test/test.js"
                                                            :optimizations :none
                                                            :target        :nodejs}}]}
                    :repl-options {:nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]
                                   :timeout          240000}}
             :prod {:cljsbuild {:builds [{:id           "ios"
                                          :source-paths ["src" "env/prod"]
                                          :compiler     {:output-to          "index.ios.js"
                                                         :main               "env.ios.main"
                                                         :output-dir         "target/ios-prod"
                                                         :static-fns         true
                                                         :optimize-constants true
                                                         :optimizations      :simple
                                                         :closure-defines    {"goog.DEBUG" false}}}
                                         {:id           "android"
                                          :source-paths ["src" "env/prod"]
                                          :compiler     {:output-to          "index.android.js"
                                                         :main               "env.android.main"
                                                         :output-dir         "target/android-prod"
                                                         :static-fns         true
                                                         :optimize-constants true
                                                         :optimizations      :simple
                                                         :closure-defines    {"goog.DEBUG" false}}}]}}})
