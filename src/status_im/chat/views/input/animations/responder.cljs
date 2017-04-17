(ns status-im.chat.views.input.animations.responder
  (:require [status-im.components.drag-drop :as drag]
            [status-im.components.animation :as anim]
            [status-im.chat.views.input.utils :as input-utils]
            [re-frame.core :refer [dispatch]]
            [taoensso.timbre :as log]))

;; todo bad name. Ideas?
(defn enough-dy [gesture]
  (> (Math/abs (.-dy gesture)) 10))

(defn on-move [response-height layout-height]
  (fn [_ gesture]
    (when (enough-dy gesture)
      (let [to-value (- @layout-height (.-moveY gesture))]
        (when (> to-value input-utils/min-height)
          (anim/start
            (anim/spring response-height {:toValue to-value})))))))

(defn on-release [response-height handler-name key]
  (fn [_ gesture]
    (when (enough-dy gesture)
      (dispatch [handler-name (.-vy gesture) (.-_value response-height) key]))))

(defn pan-responder [response-height layout-height handler-name key]
  (drag/create-pan-responder
    {:on-move    (on-move response-height layout-height)
     :on-release (on-release response-height handler-name key)}))
