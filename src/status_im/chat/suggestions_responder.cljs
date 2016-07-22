(ns status-im.chat.suggestions-responder
  (:require [status-im.components.drag-drop :as drag]
            [status-im.components.animation :as anim]
            [status-im.components.react :as react]
            [re-frame.core :refer [dispatch]]))

;; todo bad name. Ideas?
(defn enough-dy [gesture]
  (> (Math/abs (.-dy gesture)) 10))

(defn on-move [response-height layout-height]
  (fn [_ gesture]
    (when (enough-dy gesture)
      (let [to-value (- @layout-height (.-moveY gesture))]
        (anim/start
          (anim/spring response-height {:toValue to-value}))))))

(defn on-release [response-height handler-name]
  (fn [_ gesture]
    (when (enough-dy gesture)
      (dispatch [handler-name
                 (.-vy gesture)
                 ;; todo access to "private" property
                 ;; better to find another way...
                 (.-_value response-height)]))))

(defn pan-responder [response-height layout-height handler-name]
  (drag/create-pan-responder
    {:on-move    (on-move response-height layout-height)
     :on-release (on-release response-height handler-name)}))
