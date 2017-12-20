(ns ^:figwheel-no-load env.android.main
  (:require [reagent.core :as r]
            [status-im.android.core :as core]
            [figwheel.client :as figwheel :include-macros true]
            [status-im.test.handlers-stubs :refer [init-stubs]]))

(set! js/console.disableYellowBox true)

(enable-console-print!)

(def cnt (r/atom 0))
(defn reloader [] @cnt [core/app-root])
(def root-el (r/as-element [reloader]))
(defn callback []
  (swap! cnt inc)
  (status-im.ui.components.status/init-jail)
  (re-frame.core/dispatch [:load-commands!]))

(figwheel/watch-and-reload
  :websocket-url "ws://10.0.3.2:3449/figwheel-ws"
  :heads-up-display false
  :jsload-callback callback)

(core/init)
(init-stubs)
