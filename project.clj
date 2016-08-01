(defproject status-im "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url  "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.9.0-alpha7"]
                 [org.clojure/clojurescript "1.9.76"]
                 [reagent "0.5.1" :exclusions [cljsjs/react]]
                 [re-frame "0.7.0"]
                 [prismatic/schema "1.0.4"]
                 ^{:voom {:repo   "git@github.com:status-im/status-lib.git"
                          :branch "discover-rework"}}
                 [status-im/protocol "0.1.1-20160706_085008-ge61756a"]
                 [natal-shell "0.3.0"]
                 [com.andrewmcveigh/cljs-time "0.4.0"]]
  :plugins [[lein-cljsbuild "1.1.1"]
            [lein-figwheel "0.5.0-2"]]
  :clean-targets ["target/" "index.ios.js" "index.android.js"]
  :aliases {"prod-build" ^{:doc "Recompile code with prod profile."}
                         ["do" "clean"
                          ["with-profile" "prod" "cljsbuild" "once" "ios"]
                          ["with-profile" "prod" "cljsbuild" "once" "android"]]}
  :test-paths ["test/clj"]
  :figwheel {:nrepl-port 7888}
  :profiles {:dev  {:dependencies [[figwheel-sidecar "0.5.0-2"]
                                   [com.cemerick/piggieback "0.2.1"]
                                   [io.appium/java-client "3.4.1"]]
                    :plugins      [[lein-doo "0.1.6"]]
                    :source-paths ["src" "env/dev"]
                    :cljsbuild    {:builds {:ios          {:source-paths ["src" "env/dev"]
                                                           :figwheel     true
                                                           :compiler     {:output-to     "target/ios/not-used.js"
                                                                          :main          "env.ios.main"
                                                                          :output-dir    "target/ios"
                                                                          :optimizations :none}}
                                            :android      {:source-paths ["src" "env/dev"]
                                                           :figwheel     true
                                                           :compiler     {:output-to     "target/android/not-used.js"
                                                                          :main          "env.android.main"
                                                                          :output-dir    "target/android"
                                                                          :optimizations :none}}
                                            :android-test {:source-paths ["src" "env/dev"]
                                                           :figwheel     true
                                                           :compiler     {:output-to     "target/android/not-used.js"
                                                                          :main          "env.android-test.main"
                                                                          :output-dir    "target/android"
                                                                          :optimizations :none}}
                                            :test         {:source-paths ["src" "test/cljs"]
                                                           :compiler
                                                                         {:main          status-im.test.runner
                                                                          :output-to     "target/test/test.js"
                                                                          :optimizations :none
                                                                          :target        :nodejs}}}}
                    :repl-options {:nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]}}
             :prod {:cljsbuild {:builds {:ios     {:source-paths ["src" "env/prod"]
                                                   :compiler     {:output-to     "index.ios.js"
                                                                  :main          "env.ios.main"
                                                                  :output-dir    "target/ios"
                                                                  :optimizations :simple}}
                                         :android {:source-paths ["src" "env/prod"]
                                                   :compiler     {:output-to     "index.android.js"
                                                                  :main          "env.android.main"
                                                                  :output-dir    "target/android"
                                                                  :optimizations :simple}}}}}})
