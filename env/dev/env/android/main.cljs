 (ns ^:figwheel-no-load env.android.main
  (:require [reagent.core :as r]
            [status-im.android.core :as core]
            [figwheel.client :as figwheel :include-macros true]))

(enable-console-print!)

(def cnt (r/atom 0))
(defn reloader [] @cnt [core/app-root])
(def root-el (r/as-element [reloader]))

(figwheel/watch-and-reload
 :websocket-url "ws://localhost:3449/figwheel-ws"
 :heads-up-display false
 :jsload-callback #(swap! cnt inc))

(core/init)
