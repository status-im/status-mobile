(ns status-im.ui.components.drag-drop
  (:require [status-im.ui.components.react :as react]))

(defn pan-handlers [pan-responder]
  (js->clj (.-panHandlers pan-responder)))

(defn create-pan-responder [{:keys [on-move on-release]}]
  (.create react/pan-responder
           (clj->js {:onStartShouldSetPanResponder (fn [] true)
                     :onPanResponderMove           on-move
                     :onPanResponderRelease        on-release})))
