(ns status-im.chat.views.plain-input
  (:require-macros [status-im.utils.views :refer [defview]])
  (:require [re-frame.core :refer [subscribe dispatch]]
            [status-im.components.react :refer [view
                                                icon
                                                touchable-highlight
                                                text-input
                                                dismiss-keyboard!]]
            [status-im.chat.views.suggestions :refer [suggestions-view]]
            [status-im.chat.views.content-suggestions :refer [content-suggestions-view]]
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

(defn plain-message-input-view [{:keys [input-options validator]}]
  (let [input-message (subscribe [:get-chat-input-text])
        command (subscribe [:get-chat-command])
        to-msg-id (subscribe [:get-chat-command-to-msg-id])
        input-command (subscribe [:get-chat-command-content])
        staged-commands (subscribe [:get-chat-staged-commands])
        typing-command? (subscribe [:typing-command?])]
    (fn [{:keys [input-options validator]}]
      (let [dismiss-keyboard (not (or command @typing-command?))
            command @command
            response? (and command @to-msg-id)]
        [view st/input-container
         (cond
           response? [response/request-view]
           command [content-suggestions-view]
           :else [suggestions-view])
         [view st/input-view
          (if command
            (when-not response?
              [command/command-icon command response?])
            [touchable-highlight {:on-press #(dispatch [:switch-command-suggestions])
                                  :style    st/switch-commands-touchable}
             [view nil
              (if @typing-command?
                [icon :close-gray st/close-icon]
                [icon :list st/list-icon])]])
          [text-input (merge {:style           (cond
                                                 response? st-response/command-input
                                                 command st-command/command-input
                                                 :else st/message-input)
                              :autoFocus       false
                              :blurOnSubmit    dismiss-keyboard
                              :onChangeText    (fn [text]
                                                 ((if command
                                                    command/set-input-message
                                                    set-input-message)
                                                   text))
                              :onSubmitEditing (fn []
                                                 (if command
                                                   (command/try-send @input-command validator)
                                                   (try-send @staged-commands
                                                             @input-message
                                                             dismiss-keyboard)))}
                             input-options)
           (if command
             @input-command
             @input-message)]
          ;; TODO emoticons: not implemented
          (when (not command)
            [icon :smile st/smile-icon])
          (if command
            (if (command/valid? @input-command validator)
              [touchable-highlight {:on-press command/send-command}
               [view st/send-container [icon :send st/send-icon]]]
              (when-not response?
                [touchable-highlight {:on-press command/cancel-command-input}
                 [view st-command/cancel-container
                  [icon :close-gray st-command/cancel-icon]]]))
            (when (message-valid? @staged-commands @input-message)
              [touchable-highlight {:on-press #(try-send @staged-commands
                                                         @input-message
                                                         dismiss-keyboard)}
               [view st/send-container
                [icon :send st/send-icon]]]))]]))))
