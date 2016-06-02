(ns status-im.chat.views.plain-input
  (:require-macros [status-im.utils.views :refer [defview]])
  (:require [re-frame.core :refer [subscribe dispatch]]
            [status-im.components.react :refer [view
                                                animated-view
                                                icon
                                                touchable-highlight
                                                text-input
                                                dismiss-keyboard!]]
            [status-im.chat.views.command :as command]
            [status-im.chat.views.response :as response]
            [status-im.chat.styles.plain-input :as st]
            [status-im.chat.styles.input :as st-command]
            [status-im.chat.styles.response :as st-response]))

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

(defview commands-button [animation?]
  [typing-command? [:typing-command?]
   buttons-scale [:get-in [:animations :message-input-buttons-scale]]]
  [touchable-highlight {:disabled animation?
                        :on-press #(dispatch [:switch-command-suggestions])
                        :style    st/message-input-button-touchable}
   [animated-view {:style (st/message-input-button buttons-scale)}
    (if typing-command?
      [icon :close-gray st/close-icon]
      [icon :list st/list-icon])]])

(defview smile-button [animation?]
  [buttons-scale [:get-in [:animations :message-input-buttons-scale]]]
  [touchable-highlight {:disabled animation?
                        :on-press #(dispatch [:switch-command-suggestions])
                        :style    st/message-input-button-touchable}
   [animated-view {:style (st/message-input-button buttons-scale)}
    [icon :smile st/smile-icon]]])

(defview message-input-container [input]
  [message-input-offset [:get-in [:animations :message-input-offset]]]
  [animated-view {:style (st/message-input-container message-input-offset)}
   input])

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
                           :autoFocus       false
                           :blurOnSubmit    dismiss-keyboard
                           :onChangeText    (fn [text]
                                              ((if message-input?
                                                 set-input-message
                                                 command/set-input-message)
                                                text))
                           :editable        (not animation?)
                           :onSubmitEditing #(if message-input?
                                              (try-send staged-commands
                                                        input-message
                                                        dismiss-keyboard)
                                              (command/try-send input-command validator))}
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
                                                     dismiss-keyboard)}
           [view st/send-container
            [icon :send st/send-icon]]])
        (if (command/valid? input-command validator)
          [touchable-highlight {:disabled animation?
                                :on-press command/send-command}
           [view st/send-container [icon :send st/send-icon]]]
          (when-not response?
            [touchable-highlight {:disabled animation?
                                  :on-press command/cancel-command-input}
             [view st-command/cancel-container
              [icon :close-gray st-command/cancel-icon]]])))]]))
