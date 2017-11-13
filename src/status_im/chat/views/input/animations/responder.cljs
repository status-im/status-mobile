(ns status-im.chat.views.input.animations.responder
  (:require [status-im.ui.components.drag-drop :as drag]
            [status-im.ui.components.animation :as anim]
            [status-im.chat.views.input.utils :as input-utils]
            [re-frame.core :refer [dispatch]]
            [taoensso.timbre :as log]
            [status-im.utils.platform :as p]
            [status-im.ui.components.toolbar.styles :as toolbar-st]))

;; todo bad name. Ideas?
(defn enough-dy [gesture]
  (> (Math/abs (.-dy gesture)) 10))

(defn on-move [response-height layout-height]
  (let [margin-top (+ (get-in p/platform-specific [:component-styles :status-bar :main :height])
                      (/ (:height toolbar-st/toolbar) 2))]
    (fn [_ gesture]
      (when (enough-dy gesture)
        (let [to-value (+ (- @layout-height (.-moveY gesture)) margin-top)]
          (when (> to-value input-utils/min-height)
            (dispatch [:set :expandable-view-height-to-value to-value])
            (anim/start
              (anim/spring response-height {:toValue to-value}))))))))

(defn on-release [response-height handler-name key]
  (fn [_ gesture]
    (when (enough-dy gesture)
      (dispatch [handler-name (.-vy gesture) (.-_value response-height) key]))))

(defn pan-responder [response-height layout-height handler-name key]
  (drag/create-pan-responder
    {:on-move    (on-move response-height layout-height)
     :on-release (on-release response-height handler-name key)}))
