(ns status-im.chat.views.plain-message
  (:require-macros [status-im.utils.views :refer [defview]])
  (:require [re-frame.core :refer [subscribe dispatch]]
            [reagent.core :as r]
            [status-im.components.react :refer [view
                                                animated-view
                                                icon
                                                touchable-highlight
                                                dismiss-keyboard!]]
            [status-im.components.animation :as anim]
            [status-im.chat.styles.plain-message :as st]
            [status-im.constants :refer [response-input-hiding-duration]]))

(defn set-input-message [message]
  (dispatch [:set-chat-input-text message]))

(defn send [dismiss-keyboard]
  (when dismiss-keyboard
    (dismiss-keyboard!))
  (dispatch [:send-chat-msg]))

(defn message-valid? [staged-commands message]
  (or (and (pos? (count message))
           (not= "!" message))
      (pos? (count staged-commands))))

(defn try-send [staged-commands message dismiss-keyboard]
  (when (message-valid? staged-commands message)
    (send dismiss-keyboard)))

(defn commands-button-animation-logic [{:keys [to-value val]}]
  (fn [_]
    (let [to-scale @to-value
          minimum 0.1
          scale (cond (< 1 to-scale) 1
                      (< to-scale minimum) minimum
                      :else to-scale)]
      (anim/start (anim/timing val {:toValue  scale
                                    :duration response-input-hiding-duration})
                  (fn [arg]
                    (when (.-finished arg)
                      (dispatch [:set-in [:animations ::message-input-buttons-scale-current] scale])
                      (when (= to-scale minimum)
                        (dispatch [:finish-show-response]))))))))

(defn commands-button []
  (let [typing-command? (subscribe [:typing-command?])
        animation? (subscribe [:get-in [:animations :commands-input-is-switching?]])
        to-scale (subscribe [:get-in [:animations :message-input-buttons-scale]])
        cur-scale (subscribe [:get-in [:animations ::message-input-buttons-scale-current]])
        buttons-scale (anim/create-value (or @cur-scale 1))
        context {:to-value to-scale
                 :val      buttons-scale}
        on-update (commands-button-animation-logic context)]
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
  (let [animation? (subscribe [:get-in [:animations :commands-input-is-switching?]])
        to-scale (subscribe [:get-in [:animations :message-input-buttons-scale]])
        cur-scale (subscribe [:get-in [:animations ::message-input-buttons-scale-current]])
        buttons-scale (anim/create-value (or @cur-scale 1))
        context {:to-value to-scale
                 :val      buttons-scale}
        on-update (commands-button-animation-logic context)]
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
