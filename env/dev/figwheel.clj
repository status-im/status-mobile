(ns figwheel
  (:require [clojure.string :as s]))

(defn system-options [builds-to-start]
      {:nrepl-port      7888
       :builds          [{:id           :desktop
                          :source-paths ["react-native/src" "src" "env/dev"]
                          :compiler     {:output-to     "target/ios/desktop.js"
                                         :main          "env.desktop.main"
                                         :output-dir    "target/desktop"
                                         :npm-deps      false
                                         :optimizations :none}
                          :figwheel     true}
                         {:id           :ios
                          :source-paths ["react-native/src" "src" "env/dev"]
                          :compiler     {:output-to     "target/ios/app.js"
                                         :main          "env.ios.main"
                                         :output-dir    "target/ios"
                                         :npm-deps      false
                                         :optimizations :none}
                          :figwheel     true}
                         {:id               :android
                          :source-paths     ["react-native/src" "src" "env/dev"]
                          :compiler         {:output-to       "target/android/app.js"
                                             :main            "env.android.main"
                                             :output-dir      "target/android"
                                             :npm-deps        false
                                             :optimizations   :none}
                          :warning-handlers '[status-im.utils.build/warning-handler]
                          :figwheel         true}

                         {:id           :worker-android
                          :source-paths ["react-native/src" "src" "env/dev"]
                          :figwheel     true
                          :compiler     {:output-to     "target/worker_android/app.js"
                                         :main          "env.android.worker"
                                         :output-dir    "target/worker_android"
                                         :optimizations :none
                                         :npm-deps      false
                                         :target        :nodejs
                                         :closure-defines {"status-im.thread/platform" "android"}}}]
       :builds-to-start builds-to-start})
