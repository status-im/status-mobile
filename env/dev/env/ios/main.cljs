(ns ^:figwheel-no-load env.ios.main
  (:require [reagent.core :as r]
            [re-frisk-remote.core :as rr]
            [status-im.ios.core :as core]
            [status-im.utils.handlers :as utils.handlers]
            [figwheel.client :as figwheel :include-macros true]
            [cljs.pprint]))

(enable-console-print!)

(def cnt (r/atom 0))
(defn reloader [] @cnt [core/app-root])
(def root-el (r/as-element [reloader]))

(figwheel/watch-and-reload
 :websocket-url "ws://localhost:3449/figwheel-ws"
 :heads-up-display false
 :jsload-callback #(swap! cnt inc))

(utils.handlers/add-pre-event-callback rr/pre-event-callback)

(rr/enable-re-frisk-remote! {:host "localhost:4567" :on-init core/init})