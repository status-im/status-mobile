(ns ^:figwheel-no-load env.android.main
  (:require [reagent.core :as r]
            [re-frame.core :as re-frame]
            [status-im.android.core :as core]
            [figwheel.client :as figwheel :include-macros true]
            [re-frisk-remote.core :as rr]
            [env.config :as conf]
            [status-im.utils.handlers :as utils.handlers]))

(enable-console-print!)

(assert (exists? core/init) "Fatal Error - Your core.cljs file doesn't define an 'init' function!!! - Perhaps there was a compilation failure?")
(assert (exists? core/app-root) "Fatal Error - Your core.cljs file doesn't define an 'app-root' function!!! - Perhaps there was a compilation failure?")

(def cnt (r/atom 0))
(defn reloader [] @cnt [core/app-root])
(def root-el (r/as-element [reloader]))

(figwheel/watch-and-reload
  :websocket-url "ws://localhost:3449/figwheel-ws"
  :heads-up-display false
  :jsload-callback #(swap! cnt inc))

(utils.handlers/add-pre-event-callback rr/pre-event-callback)

(rr/enable-re-frisk-remote! {:host "10.0.3.2:4567" :on-init core/init})
