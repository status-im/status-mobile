(ns status-im.components.drag-drop
  (:require [status-im.components.react :refer [animated pan-responder]]))

(defn pan-handlers [pan-responder]
  (js->clj (.-panHandlers pan-responder)))

(defn create-pan-responder [{:keys [on-move on-release]}]
  (.create pan-responder
           (clj->js {:onStartShouldSetPanResponder (fn [] true)
                     :onPanResponderMove           on-move
                     :onPanResponderRelease        on-release})))
