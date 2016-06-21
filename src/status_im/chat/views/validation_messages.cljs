(ns status-im.chat.views.validation-messages
  (:require-macros [reagent.ratom :refer [reaction]]
                   [status-im.utils.views :refer [defview]])
  (:require [re-frame.core :refer [subscribe dispatch]]
            [reagent.core :as r]
            [status-im.components.react :refer [view
                                                animated-view
                                                icon
                                                image
                                                text
                                                text-input
                                                touchable-highlight]]
            [status-im.chat.styles.validation-messages :as st]
            [status-im.components.animation :as anim]))

(def min-height 1)
(def max-height st/container-height)

(defn info-container [message]
  [view st/info-container
   [text {:style st/command-name}
    (:parameter message)]
   [text {:style st/message-info}
    (:message message)]])

(defview validation-messages-inner []
  [command [:get-chat-command]
   validation-messages [:command-validation-messages]]
  [view {:style    st/validation-messages
         :onLayout (fn [event]
                     (let [height (.. event -nativeEvent -layout -height)]
                       (dispatch [:set-animation :validation-messages-max-height height])))}
   [view st/inner-container
    (for [message validation-messages]
      ^{:key message} [info-container message])]])

(defn container-animation-logic [{:keys [to-value val]}]
  (fn [_]
    (let [to-value @to-value]
      (when (< min-height to-value)
        (dispatch [:set-animation ::visible? true]))
      (anim/start (anim/timing val {:toValue  to-value
                                    :duration 200})
                  (fn [arg]
                    (when (<= to-value min-height)
                      (dispatch [:set-animation ::visible? false])))))))

(defn container [& children]
  (let [show? (subscribe [:show-command-validation-messages?])
        commands-input-is-switching? (subscribe [:animations :commands-input-is-switching?])
        validation-messages (subscribe [:command-validation-messages])
        to-height (reaction (if (and @show? (not @commands-input-is-switching?))
                              (* max-height (count @validation-messages))
                              min-height))
        visible? (subscribe [:animations ::visible?])
        anim-height (anim/create-value min-height)
        context {:to-value to-height
                 :val      anim-height}
        on-update (container-animation-logic context)]
    (r/create-class
      {:component-did-mount
       on-update
       :component-did-update
       on-update
       :reagent-render
       (fn [& children]
         @to-height
         (when @visible?
           (into [animated-view {:style (st/animated-container anim-height)}]
                 children)))})))

(defview validation-messages []
  [validation-messages [:command-validation-messages]]
  (when validation-messages
    [container
     [validation-messages-inner]]))
