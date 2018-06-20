(ns status-im.ui.screens.home.animations.responder
  (:require [status-im.ui.components.react :as react]
            [re-frame.core :as re-frame]
            [status-im.ui.components.animation :as animation]))

(defn get-updated-value [gesture end-offset-x swiped?]
  (let [base-value (if swiped? end-offset-x 0)]
    (- base-value (.-dx gesture))))

(defn on-start [_ gesture]
  (> (js/Math.abs (.-dx gesture)) 10))

(defn on-move [animated-offset-x end-offset-x swiped?]
  (fn [_ gesture]
    (let [to-value (get-updated-value gesture end-offset-x swiped?)]
      (animation/start (animation/spring animated-offset-x {:toValue to-value})))))

(defn on-release [animated-offset-x end-offset-x chat-id swiped?]
  (fn [_ gesture]
    (let [updated-value (get-updated-value gesture end-offset-x swiped?)
          should-open?  (> updated-value (/ end-offset-x 2))
          to-value      (if should-open? end-offset-x 0)]
      (re-frame/dispatch [:set-swipe-position chat-id should-open?])
      (animation/start (animation/spring animated-offset-x {:toValue to-value})))))

(defn swipe-pan-responder [animated-offset-x end-offset-x chat-id swiped?]
  (.create react/pan-responder
           (clj->js {:onMoveShouldSetPanResponder on-start
                     :onPanResponderMove          (on-move animated-offset-x end-offset-x swiped?)
                     :onPanResponderRelease       (on-release animated-offset-x end-offset-x chat-id swiped?)
                     :onPanResponderTerminate     (on-release animated-offset-x end-offset-x chat-id swiped?)})))

(defn pan-handlers [pan-responder]
  (js->clj (.-panHandlers pan-responder)))