(ns status-im.chat.views.plain-message
  (:require-macros [status-im.utils.views :refer [defview]])
  (:require [re-frame.core :refer [subscribe dispatch]]
            [reagent.core :as r]
            [status-im.components.react :refer [view
                                                animated-view
                                                icon
                                                touchable-highlight]]
            [status-im.components.animation :as anim]
            [status-im.chat.styles.plain-message :as st]
            [status-im.constants :refer [response-input-hiding-duration]]))

(defn set-input-message [message]
  (dispatch [:set-chat-input-text message]))

(defn message-valid? [staged-commands message]
  (or (and (pos? (count message))
           (not= "!" message))
      (pos? (count staged-commands))))

(defn try-send [staged-commands message]
  (when (message-valid? staged-commands message)
    (dispatch [:send-chat-msg])))

(defn prepare-message-input [message-input]
  (when message-input
    (.clear message-input)
    (.focus message-input)))

(defn commands-button-animation-callback [message-input]
  (fn [arg to-value]
    (when (.-finished arg)
      (dispatch [:set-animation ::message-input-buttons-scale-current to-value])
      (when (<= to-value 0.1)
        (dispatch [:finish-show-response])
        (prepare-message-input @message-input)))))

(defn button-animation-logic [{:keys [to-value val callback]}]
  (fn [_]
    (let [to-scale @to-value
          minimum 0.1
          scale (cond (< 1 to-scale) 1
                      (< to-scale minimum) minimum
                      :else to-scale)]
      (anim/start (anim/timing val {:toValue  scale
                                    :duration response-input-hiding-duration})
                  (when callback
                    (fn [arg]
                      (callback arg to-scale)))))))

(defn commands-button []
  (let [typing-command? (subscribe [:typing-command?])
        message-input (subscribe [:get :message-input])
        animation? (subscribe [:animations :commands-input-is-switching?])
        to-scale (subscribe [:animations :message-input-buttons-scale])
        cur-scale (subscribe [:animations ::message-input-buttons-scale-current])
        buttons-scale (anim/create-value (or @cur-scale 1))
        anim-callback (commands-button-animation-callback message-input)
        context {:to-value to-scale
                 :val      buttons-scale
                 :callback anim-callback}
        on-update (button-animation-logic context)]
    (r/create-class
      {:component-did-mount
       on-update
       :component-did-update
       on-update
       :reagent-render
       (fn []
         (let [typing-command? @typing-command?]
           @to-scale
           [touchable-highlight {:disabled @animation?
                                 :on-press #(dispatch [:switch-command-suggestions])
                                 :style    st/message-input-button-touchable}
            [animated-view {:style (st/message-input-button buttons-scale)}
             (if typing-command?
               [icon :close-gray st/close-icon]
               [icon :list st/list-icon])]]))})))

(defn smile-button []
  (let [animation? (subscribe [:animations :commands-input-is-switching?])
        to-scale (subscribe [:animations :message-input-buttons-scale])
        cur-scale (subscribe [:animations ::message-input-buttons-scale-current])
        buttons-scale (anim/create-value (or @cur-scale 1))
        context {:to-value to-scale
                 :val      buttons-scale}
        on-update (button-animation-logic context)]
    (r/create-class
      {:component-did-mount
       on-update
       :component-did-update
       on-update
       :reagent-render
       (fn []
         @to-scale
         [touchable-highlight {:disabled @animation?
                               :on-press (fn []
                                           ;; TODO emoticons: not implemented
                                           )
                               :style    st/message-input-button-touchable}
          [animated-view {:style (st/message-input-button buttons-scale)}
           [icon :smile st/smile-icon]]])})))
