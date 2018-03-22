(ns ^:figwheel-no-load env.desktop.main
  (:require [reagent.core :as r]
            [re-frisk-remote.core :as rr]
            [status-im.desktop.core :as core]
            [status-im.utils.handlers :as utils.handlers]
            [figwheel.client :as figwheel]
            [env.config :as conf]
            [env.utils]))

(enable-console-print!)

(assert (exists? core/init) "Fatal Error - Your core.cljs file doesn't define an 'init' function!!! - Perhaps there was a compilation failure?")
(assert (exists? core/app-root) "Fatal Error - Your core.cljs file doesn't define an 'app-root' function!!! - Perhaps there was a compilation failure?")

(def cnt (r/atom 0))
(defn reloader [] @cnt [core/app-root])

;; Do not delete, root-el is used by the figwheel-bridge.js
(def root-el (r/as-element [reloader]))

(figwheel/start {:websocket-url    (:ios conf/figwheel-urls)
                 :heads-up-display false
                 :jsload-callback  #(swap! cnt inc)})

(utils.handlers/add-pre-event-callback rr/pre-event-callback)

(rr/enable-re-frisk-remote! {:host (env.utils/re-frisk-url (:ios conf/figwheel-urls))
                             :on-init core/init})
