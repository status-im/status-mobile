(defproject status-im "0.1.0-SNAPSHOT"
  :url "https://github.com/status-im/status-react/"
  :license {:name "Eclipse Public License"
            :url  "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.10.0"]
                 [org.clojure/clojurescript "1.10.520"
                  :exclusions
                  [com.google.javascript/closure-compiler-unshaded
                   org.clojure/google-closure-library]]
                 ;; [com.google.javascript/closure-compiler-unshaded "v20180319"]
                 ;;  v20180506
                 [com.google.javascript/closure-compiler-unshaded "v20190325"]
                 [org.clojure/google-closure-library "0.0-20190213-2033d5d9"]
                 [org.clojure/core.async "0.4.474"]
                 [reagent "0.7.0" :exclusions [cljsjs/react cljsjs/react-dom cljsjs/react-dom-server cljsjs/create-react-class]]
                 [status-im/re-frame "0.10.5"]
                 [com.andrewmcveigh/cljs-time "0.5.2"]
                 [status-im/timbre "4.10.0-2-status"]
                 [com.taoensso/encore "2.94.0"]
                 [hickory "0.7.1"]
                 [cljs-bean "1.3.0"]
                 [com.cognitect/transit-cljs "0.8.248"]
                 [mvxcvi/alphabase "1.0.0"]
                 [rasom/cljs-react-navigation "0.1.4"]]
  :plugins [[rasom/lein-githooks "0.1.5"]
            [lein-cljsbuild "1.1.7"]
            [lein-re-frisk "0.5.8"]
            [lein-cljfmt "0.5.7"]]
  :githooks {:auto-install true
             :pre-commit   ["lein cljfmt check src/status_im/core.cljs $(git diff --diff-filter=d --cached --name-only src test/cljs)"]}
  :cljfmt {:indents {letsubs [[:inner 0]]}}
  :clean-targets ["target/" "index.ios.js" "index.android.js" "status-modules/cljs"]
  :aliases {"jsbundle"         ^{:doc "Recompile code with prod profile."}
            ["do" "clean"
             ["with-profile" "prod" "cljsbuild" "once" "ios"]
             ["with-profile" "prod" "cljsbuild" "once" "android"]
             ["with-profile" "prod" "cljsbuild" "once" "desktop"]]
            "jsbundle-android" ^{:doc "Recompile code for Android with prod profile."}
            ["do" "clean"
             ["with-profile" "prod" "cljsbuild" "once" "android"]]
            "jsbundle-ios"     ^{:doc "Recompile code for iOS with prod profile."}
            ["do" "clean"
             ["with-profile" "prod" "cljsbuild" "once" "ios"]]
            "jsbundle-desktop" ^{:doc "Recompile code for desktop with prod profile."}
            ["do" "clean"
             ["with-profile" "prod" "cljsbuild" "once" "desktop"]]
            "figwheel-repl"      ["with-profile" "+figwheel" "run" "-m" "clojure.main" "env/dev/run.clj"]
            "test-cljs"          ["with-profile" "test" "doo" "node" "test" "once"]
            "test-protocol"      ["with-profile" "test" "doo" "node" "protocol" "once"]
            "test-env-dev-utils" ["with-profile" "test" "doo" "node" "env-dev-utils" "once"]}
  :profiles {:dev      {:dependencies [[cider/piggieback "0.4.0"]]
                        :cljsbuild    {:builds
                                       {:ios
                                        {:source-paths ["components/src" "react-native/src/cljsjs" "react-native/src/mobile" "src" "dev"]
                                         :compiler     {:output-to     "target/ios/app.js"
                                                        :main          "env.ios.main"
                                                        :output-dir    "target/ios"
                                                        :optimizations :none}}
                                        :android
                                        {:source-paths     ["components/src" "react-native/src/cljsjs" "react-native/src/mobile" "src" "dev"]
                                         :compiler         {:output-to     "target/android/app.js"
                                                            :main          "env.android.main"
                                                            :output-dir    "target/android"
                                                            :optimizations :none}
                                         :warning-handlers [status-im.utils.build/warning-handler]}
                                        :desktop
                                        {:source-paths ["components/src" "react-native/src/cljsjs" "react-native/src/desktop" "src" "dev"]
                                         :compiler     {:output-to     "target/desktop/app.js"
                                                        :main          "env.desktop.main"
                                                        :output-dir    "target/desktop"
                                                        :optimizations :none}}}}
                        :repl-options {:nrepl-middleware [cider.piggieback/wrap-cljs-repl]
                                       :timeout          240000}}
             :figwheel [:dev
                        {:dependencies [[figwheel-sidecar "0.5.18"]
                                        [re-frisk-remote "0.5.5"]
                                        [re-frisk-sidecar "0.5.7"]
                                        [day8.re-frame/tracing "0.5.0"]
                                        [hawk "0.2.11"]]
                         :source-paths ["src" "env/dev" "react-native/src/cljsjs" "components/src" "dev"]}]
             :test     {:dependencies [[day8.re-frame/test "0.1.5"]]
                        :plugins      [[lein-doo "0.1.9"]]
                        :cljsbuild    {:builds
                                       [{:id           "test"
                                         :source-paths ["components/src" "src" "test/cljs" "dev"]
                                         :compiler     {:main          status-im.test.runner
                                                        :output-to     "target/test/test.js"
                                                        :output-dir    "target/test"
                                                        :optimizations :none
                                                        :preamble      ["js/hook-require.js"]
                                                        :target        :nodejs}}
                                        {:id           "protocol"
                                         :source-paths ["components/src" "src" "test/cljs" "dev"]
                                         :compiler     {:main          status-im.test.protocol.runner
                                                        :output-to     "target/test/test.js"
                                                        :output-dir    "target/test"
                                                        :optimizations :none
                                                        :preamble      ["js/hook-require.js"]
                                                        :target        :nodejs}}
                                        {:id           "env-dev-utils"
                                         :source-paths ["env/dev/env/utils.cljs" "test/env/dev" "dev"]
                                         :compiler     {:main          env.test.runner
                                                        :output-to     "target/test/test.js"
                                                        :output-dir    "target/test"
                                                        :optimizations :none
                                                        :target        :nodejs}}]}}
             :prod     {:cljsbuild {:builds
                                    {:ios
                                     {:source-paths     ["components/src" "react-native/src/cljsjs" "react-native/src/mobile" "src" "env/prod" "prod"]
                                      :compiler         {:main               "env.ios.main"
                                                         :output-dir         "target/ios-prod"
                                                         :static-fns         true
                                                         :fn-invoke-direct   true
                                                         :optimize-constants true
                                                         :optimizations      :advanced
                                                         :pseudo-names       false
                                                         :pretty-print       false
                                                         :closure-defines    {"goog.DEBUG" false}
                                                         :parallel-build     false
                                                         :elide-asserts      true
                                                         :externs            ["externs.js"]
                                                         :language-in        :es-2015
                                                         :language-out       :es-2015
                                                         :modules            {:cljs-base  {:output-to "index.ios.js"}
                                                                              :i18n       {:entries   #{"status_im.goog.i18n"}
                                                                                           :output-to "status-modules/cljs/i18n-raw.js"}
                                                                              :network    {:entries   #{"status_im.network.ui.network_details.views"
                                                                                                        "status_im.network.ui.edit_network.views"
                                                                                                        "status_im.network.ui.edit_network.styles"
                                                                                                        "status_im.network.ui.views"
                                                                                                        "status_im.network.ui.styles"
                                                                                                        "status_im.network.core"}
                                                                                           :output-to "status-modules/cljs/network-raw.js"}}}
                                      :warning-handlers [status-im.utils.build/warning-handler]}
                                     :android
                                     {:source-paths     ["components/src" "react-native/src/cljsjs" "react-native/src/mobile" "src" "env/prod" "prod"]
                                      :compiler         {:main               "env.android.main"
                                                         :output-dir         "target/android-prod"
                                                         :static-fns         true
                                                         :fn-invoke-direct   true
                                                         :optimize-constants true
                                                         :optimizations      :advanced
                                                         :stable-names       true
                                                         :pseudo-names       false
                                                         :pretty-print       false
                                                         :closure-defines    {"goog.DEBUG" false}
                                                         :parallel-build     false
                                                         :elide-asserts      true
                                                         :externs            ["externs.js"]
                                                         :language-in        :es-2015
                                                         :language-out       :es-2015
                                                         :modules            {:cljs-base  {:output-to "index.android.js"}
                                                                              :i18n       {:entries   #{"status_im.goog.i18n"}
                                                                                           :output-to "status-modules/cljs/i18n-raw.js"}
                                                                              :network    {:entries   #{"status_im.network.ui.network_details.views"
                                                                                                        "status_im.network.ui.edit_network.views"
                                                                                                        "status_im.network.ui.edit_network.styles"
                                                                                                        "status_im.network.ui.views"
                                                                                                        "status_im.network.ui.styles"
                                                                                                        "status_im.network.core"}
                                                                                           :output-to "status-modules/cljs/network-raw.js"}}}
                                      :warning-handlers [status-im.utils.build/warning-handler]}
                                     :desktop
                                     {:source-paths     ["components/src" "react-native/src/cljsjs" "react-native/src/desktop" "src" "env/prod" "prod"]
                                      :compiler         {:main               "env.desktop.main"
                                                         :output-dir         "target/desktop-prod"
                                                         :static-fns         true
                                                         :fn-invoke-direct   true
                                                         :optimize-constants true
                                                         :optimizations      :simple
                                                         :closure-defines    {"goog.DEBUG" false}
                                                         :pseudo-names       false
                                                         :pretty-print       false
                                                         :parallel-build     false
                                                         :elide-asserts      true
                                                         :language-in        :es-2015
                                                         :language-out       :es-2015
                                                         :modules            {:cljs-base  {:output-to "index.desktop.js"}
                                                                              :i18n       {:entries   #{"status_im.goog.i18n"}
                                                                                           :output-to "status-modules/cljs/i18n-raw.js"}
                                                                              :network    {:entries   #{"status_im.network.ui.network_details.views"
                                                                                                        "status_im.network.ui.edit_network.views"
                                                                                                        "status_im.network.ui.edit_network.styles"
                                                                                                        "status_im.network.ui.views"
                                                                                                        "status_im.network.ui.styles"
                                                                                                        "status_im.network.core"}
                                                                                           :output-to "status-modules/cljs/network-raw.js"}}}
                                      :warning-handlers [status-im.utils.build/warning-handler]}}}}})
