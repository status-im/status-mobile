(ns status-im.ui.screens.chat.input.animations.expandable
  (:require-macros [status-im.utils.views :refer [defview letsubs]])
  (:require [reagent.core :as reagent]
            [status-im.ui.components.animation :as animation]
            [status-im.ui.components.react :as react]
            [status-im.ui.screens.chat.styles.animations :as style]
            [status-im.ui.screens.chat.styles.input.input :as input-style]
            [status-im.utils.platform :as platform]))

(def top-offset 100)

(defn expandable-view-on-update [anim-value]
  (animation/start
   (animation/spring anim-value {:toValue         -53
                                 :friction        10
                                 :tension         60
                                 :useNativeDriver true})))

(defview expandable-view [_ & elements]
  (letsubs [;; Default value of translateY is 104, it is sufficient to
            ;; hide two commands below an input view.
            ;; With bigger view like assets parameter animation looks good
            ;; enough too, even if initially the view isn't fully covered by
            ;; input. It might be inferred for each case but it would require
            ;; more efforts and will not change too much the way how animation
            ;; looks atm.
            anim-value         (animation/create-value 104)
            input-focused?     [:chats/current-chat-ui-prop :input-focused?]
            messages-focused?  [:chats/current-chat-ui-prop :messages-focused?]
            input-height       [:chats/current-chat-ui-prop :input-height]
            chat-input-margin  [:chats/input-margin]
            chat-layout-height [:layout-height]
            keyboard-height    [:keyboard-height]]
    {:component-did-mount
     (fn []
       (expandable-view-on-update anim-value))}
    (let [input-height (or input-height (+ input-style/padding-vertical
                                           input-style/min-input-height
                                           input-style/padding-vertical
                                           input-style/border-height))
          max-height   (- chat-layout-height (when platform/ios? keyboard-height) input-height top-offset)]
      [react/view style/overlap-container
       [react/animated-view {:style (style/expandable-container anim-value chat-input-margin max-height)}
        (into [react/scroll-view {:keyboard-should-persist-taps :always
                                  :bounces                      false}]
              (when (or input-focused? (not messages-focused?))
                elements))]])))
