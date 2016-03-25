 (ns ^:figwheel-no-load env.android.main
   (:require [reagent.core :as r]
             [syng-im.android.core :refer [app-root]]
             [figwheel.client :as figwheel :include-macros true]
             [syng-im.android.core :as core]))

 (enable-console-print!)

(def cnt (r/atom 0))
(defn reloader [] @cnt [app-root])
(def root-el (r/as-element [reloader]))

(figwheel/watch-and-reload
 :websocket-url "ws://10.0.3.2:3449/figwheel-ws"
 :heads-up-display true
 :jsload-callback #(swap! cnt inc))

(core/init)