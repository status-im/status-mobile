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

(defn container-animation-logic [{:keys [animation? to-value current-value val]}]
  (fn [_]
    (if @animation?
      (let [to-value @to-value]
        (anim/start (anim/spring val {:toValue to-value})
                    (fn [arg]
                      (when (.-finished arg)
                        (dispatch [:set-animation :response-height-current to-value])
                        (dispatch [:finish-animate-response-resize])))))
      (anim/set-value val @current-value))))

(defn container [& children]
  (let [commands-input-is-switching? (subscribe [:animations :commands-input-is-switching?])
        response-resize? (subscribe [:animations :response-resize?])
        to-response-height (subscribe [:animations :to-response-height])
        cur-response-height (subscribe [:animations :response-height-current])
        response-height (anim/create-value (or @cur-response-height 0))
        context {:animation?    (reaction (or @commands-input-is-switching? @response-resize?))
                 :to-value      to-response-height
                 :current-value cur-response-height
                 :val           response-height}
        on-update (container-animation-logic context)]
    (r/create-class
      {
       ;:component-did-mount
       ;on-update
       ;:component-did-update
       ;on-update
       :reagent-render
       (fn [& children]
         @to-response-height
         (into [animated-view {:style st/container
                               ;(if (or @commands-input-is-switching? @response-resize?)
                               ;  response-height
                               ;  (or @cur-response-height 0))
                               }]
               children))})))

(defview validation-messages []
  [validation-messages [:command-validation-messages]]
  (when validation-messages
    [container
     [validation-messages-inner]]))
