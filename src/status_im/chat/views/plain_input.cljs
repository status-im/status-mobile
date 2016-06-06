(ns status-im.chat.views.plain-input
  (:require-macros [status-im.utils.views :refer [defview]])
  (:require [re-frame.core :refer [subscribe dispatch]]
            [reagent.core :as r]
            [status-im.components.react :refer [view
                                                animated-view
                                                icon
                                                touchable-highlight
                                                text-input
                                                dismiss-keyboard!]]
            [status-im.components.animation :as anim]
            [status-im.chat.views.command :as command]
            [status-im.chat.views.response :as response]
            [status-im.chat.styles.plain-input :as st]
            [status-im.chat.styles.input :as st-command]
            [status-im.chat.styles.response :as st-response]
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

(defn commands-button [animation?]
  (let [typing-command? (subscribe [:typing-command?])
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
       (fn [animation?]
         (let [typing-command? @typing-command?]
           @to-scale
           [touchable-highlight {:disabled animation?
                                 :on-press #(dispatch [:switch-command-suggestions])
                                 :style    st/message-input-button-touchable}
            [animated-view {:style (st/message-input-button buttons-scale)}
             (if typing-command?
               [icon :close-gray st/close-icon]
               [icon :list st/list-icon])]]))})))

(defn smile-button [animation?]
  (let [to-scale (subscribe [:get-in [:animations :message-input-buttons-scale]])
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
       (fn [animation?]
         @to-scale
         [touchable-highlight {:disabled animation?
                               :on-press (fn []
                                           ;; TODO emoticons: not implemented
                                           )
                               :style    st/message-input-button-touchable}
          [animated-view {:style (st/message-input-button buttons-scale)}
           [icon :smile st/smile-icon]]])})))

(defn message-input-container-animation-logic [{:keys [to-value val]}]
  (fn [_]
    (let [to-value @to-value]
      (anim/start (anim/timing val {:toValue  to-value
                                    :duration response-input-hiding-duration})
                  (fn [arg]
                    (when (.-finished arg)
                      (dispatch [:set-in [:animations ::message-input-offset-current] to-value])))))))

(defn message-input-container [input]
  (let [to-message-input-offset (subscribe [:get-in [:animations :message-input-offset]])
        cur-message-input-offset (subscribe [:get-in [:animations ::message-input-offset-current]])
        message-input-offset (anim/create-value (or @cur-message-input-offset 0))
        context {:to-value to-message-input-offset
                 :val message-input-offset}
        on-update (message-input-container-animation-logic context)]
    (r/create-class
      {:component-did-mount
       on-update
       :component-did-update
       on-update
       :reagent-render
       (fn [input]
         @to-message-input-offset
         [animated-view {:style (st/message-input-container message-input-offset)}
          input])})))

(defview plain-message-input-view [{:keys [input-options validator]}]
  [input-message [:get-chat-input-text]
   command [:get-chat-command]
   to-msg-id [:get-chat-command-to-msg-id]
   input-command [:get-chat-command-content]
   staged-commands [:get-chat-staged-commands]
   typing-command? [:typing-command?]
   commands-button-is-switching? [:get-in [:animations :commands-input-is-switching?]]]
  (let [dismiss-keyboard (not (or command typing-command?))
        response? (and command to-msg-id)
        message-input? (or (not command) commands-button-is-switching?)
        animation? commands-button-is-switching?]
    [view st/input-container
     [view st/input-view
      (if message-input?
        [commands-button animation?]
        (when (and command (not response?))
          [command/command-icon command response?]))
      [message-input-container
       [text-input (merge {:style           (cond
                                              message-input? st/message-input
                                              response? st-response/command-input
                                              command st-command/command-input)
                           :ref             (fn [input]
                                              (dispatch [:set-message-input input]))
                           :autoFocus       false
                           :blurOnSubmit    dismiss-keyboard
                           :onChangeText    (fn [text]
                                              (when-not animation?
                                                ((if message-input?
                                                   set-input-message
                                                   command/set-input-message)
                                                  text)))
                           :onSubmitEditing #(when-not animation?
                                              (if message-input?
                                                (try-send staged-commands
                                                          input-message
                                                          dismiss-keyboard)
                                                (command/try-send input-command validator)))}
                          (when command
                            {:accessibility-label :command-input})
                          input-options)
        (if message-input?
          input-message
          input-command)]]
      ;; TODO emoticons: not implemented
      (when message-input?
        [smile-button animation?])
      (if message-input?
        (when (message-valid? staged-commands input-message)
          [touchable-highlight {:disabled animation?
                                :on-press #(try-send staged-commands
                                                     input-message
                                                     dismiss-keyboard)
                                :accessibility-label :send-message}
           [view st/send-container
            [icon :send st/send-icon]]])
        (if (command/valid? input-command validator)
          [touchable-highlight {:disabled animation?
                                :on-press command/send-command
                                :accessibility-label :stage-command}
           [view st/send-container [icon :send st/send-icon]]]
          (when-not response?
            [touchable-highlight {:disabled animation?
                                  :on-press command/cancel-command-input}
             [view st-command/cancel-container
              [icon :close-gray st-command/cancel-icon]]])))]]))
