(ns status-im.chat.views.input.animations.expandable
  (:require-macros [status-im.utils.views :refer [defview letsubs]])
  (:require [reagent.core :as reagent]
            [status-im.ui.components.animation :as animation]
            [status-im.ui.components.react :as react]
            [status-im.chat.styles.animations :as style]
            [status-im.chat.styles.input.input :as input-style]
            [status-im.utils.platform :as platform]))

(def top-offset 100)

(defn expandable-view-on-update [anim-value animation-height]
  (when animation-height
    (animation/start
     (animation/spring anim-value {:toValue  animation-height
                                   :friction 10
                                   :tension  60}))))

(defview expandable-view [{:keys [key]} & elements]
  (letsubs [anim-value         (animation/create-value 0)
            input-height       [:get-current-chat-ui-prop :input-height]
            input-focused?     [:get-current-chat-ui-prop :input-focused?]
            messages-focused?  [:get-current-chat-ui-prop :messages-focused?]
            chat-input-margin  [:chat-input-margin]
            keyboard-height    [:get :keyboard-height]
            chat-layout-height [:get :layout-height]]
           (let [input-height (or input-height (+ input-style/padding-vertical
                                                  input-style/min-input-height
                                                  input-style/padding-vertical
                                                  input-style/border-height))
                 bottom       (+ input-height chat-input-margin)
                 max-height   (- chat-layout-height (when platform/ios? keyboard-height) input-height top-offset)]
             [react/view style/overlap-container
              [react/animated-view {:style (style/expandable-container anim-value bottom max-height)}
               (into [react/scroll-view {:keyboard-should-persist-taps :always
                                         :on-content-size-change       #(expandable-view-on-update anim-value %2)
                                         :bounces                      false}]
                     (when (or input-focused? (not messages-focused?))
                       elements))]])))
