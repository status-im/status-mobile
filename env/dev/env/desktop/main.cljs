(ns ^:figwheel-no-load env.desktop.main
  (:require [reagent.core :as r]
            [re-frisk-remote.core :as re-frisk]
            [status-im.desktop.core :as core]
            [figwheel.client :as figwheel]
            [env.config :as conf]
            [env.utils]))

(enable-console-print!)

(assert (exists? core/init) "Fatal Error - Your core.cljs file doesn't define an 'init' function!!! - Perhaps there was a compilation failure?")
(assert (exists? core/app-root) "Fatal Error - Your core.cljs file doesn't define an 'app-root' function!!! - Perhaps there was a compilation failure?")

(def cnt (r/atom 0))
(defn reloader [props] @cnt [core/app-root props])

;; Do not delete, root-el is used by the figwheel-bridge.js
(def root-el (r/reactify-component reloader))

(figwheel/start {:websocket-url    (:desktop conf/figwheel-urls)
                 :heads-up-display false
                 :jsload-callback  #(swap! cnt inc)})

(re-frisk/enable {:host (env.utils/re-frisk-url (:desktop conf/figwheel-urls))})

(core/init)