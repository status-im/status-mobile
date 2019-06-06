(ns status-im.ui.screens.chat.input.animations.expandable
  (:require-macros [status-im.utils.views :refer [defview letsubs]])
  (:require [reagent.core :as reagent]
            [status-im.ui.components.animation :as animation]
            [status-im.ui.components.react :as react]
            [status-im.ui.screens.chat.styles.animations :as style]
            [status-im.ui.screens.chat.styles.input.input :as input-style]
            [status-im.utils.platform :as platform]))

(def top-offset 100)

(defview expandable-view [{:keys [key]} & elements]
  (letsubs [input-height       [:chats/current-chat-ui-prop :input-height]
            input-focused?     [:chats/current-chat-ui-prop :input-focused?]
            messages-focused?  [:chats/current-chat-ui-prop :messages-focused?]
            chat-input-margin  [:chats/input-margin]
            keyboard-height    [:keyboard-height]
            chat-layout-height [:layout-height]
            height (reagent/atom 0)]
    (let [input-height (or input-height (+ input-style/padding-vertical
                                           input-style/min-input-height
                                           input-style/padding-vertical
                                           input-style/border-height))
          bottom       (+ input-height chat-input-margin)
          max-height   (- chat-layout-height (when platform/ios? keyboard-height) input-height top-offset)]
      [react/view {:style (style/expandable-container @height bottom max-height)}
       (into [react/scroll-view {:keyboard-should-persist-taps :always
                                 :on-content-size-change #(reset! height %2)
                                 :bounces                      false}]
             (when (or input-focused? (not messages-focused?))
               elements))])))
