(ns status-im.chat.suggestions-responder
  (:require [status-im.components.drag-drop :as drag]
            [status-im.components.animation :as anim]
            [status-im.components.react :as react]
            [re-frame.core :refer [dispatch]]))

;; todo bad name. Ideas?
(defn enough-dy [gesture]
  (> (Math/abs (.-dy gesture)) 10))

(defn on-move [response-height kb-height orientation]
  (fn [_ gesture]
    (when (enough-dy gesture)
      (let [w (react/get-dimensions "window")
            ;; depending on orientation use height or width of screen
            prop (if (= :portrait @orientation)
                   :height
                   :width)
            ;; subtract keyboard height to get "real height" of screen
            ;; then subtract gesture position to get suggestions height
            ;; todo maybe it is better to use margin-top instead height
            ;; it is not obvious
            to-value (- (prop w) @kb-height (.-moveY gesture))]
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

(defn pan-responder [response-height kb-height orientation handler-name]
  (drag/create-pan-responder
    {:on-move    (on-move response-height kb-height orientation)
     :on-release (on-release response-height handler-name)}))
