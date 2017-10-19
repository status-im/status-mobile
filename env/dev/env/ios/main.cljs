(ns ^:figwheel-no-load env.ios.main
  (:require [reagent.core :as r]
            [re-frame.core :as re-frame]
            [status-im.ios.core :as core]
            [figwheel.client :as figwheel]
            [re-frisk-remote.core :as rr]
            [env.config :as conf]))

(enable-console-print!)

(assert (exists? core/init) "Fatal Error - Your core.cljs file doesn't define an 'init' function!!! - Perhaps there was a compilation failure?")
(assert (exists? core/app-root) "Fatal Error - Your core.cljs file doesn't define an 'app-root' function!!! - Perhaps there was a compilation failure?")

(def cnt (r/atom 0))
(defn reloader [] @cnt [core/app-root])

;; Do not delete, root-el is used by the figwheel-bridge.js
(def root-el (r/as-element [reloader]))

(defn callback []
  (re-frame/clear-subscription-cache!)
  (swap! cnt inc)
  (status-im.native-module.core/init-jail)
  (re-frame/dispatch [:load-commands!]))

(figwheel/watch-and-reload
  :websocket-url "ws://10.0.1.15:3449/figwheel-ws"
  :heads-up-display false
  :jsload-callback callback)

(rr/enable-re-frisk-remote! {:host "localhost:4567" :on-init core/init :pre-send (fn [db] (update db :chats #(into {} %)))})
