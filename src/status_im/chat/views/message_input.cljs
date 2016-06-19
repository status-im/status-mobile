(ns status-im.chat.views.message-input
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
            [status-im.chat.views.plain-message :as plain-message]
            [status-im.chat.views.command :as command]
            [status-im.chat.views.response :as response]
            [status-im.chat.styles.message-input :as st]
            [status-im.chat.styles.plain-message :as st-message]
            [status-im.chat.styles.input :as st-command]
            [status-im.chat.styles.response :as st-response]
            [status-im.constants :refer [response-input-hiding-duration]]))

(defn send-button [{:keys [on-press accessibility-label]}]
  [touchable-highlight {:on-press            on-press
                        :accessibility-label accessibility-label}
   [view st/send-container
    [icon :send st/send-icon]]])

(defn animation-logic [{:keys [to-value val]}]
  (fn [_]
    (let [to-value @to-value]
      (anim/start (anim/timing val {:toValue  to-value
                                    :duration response-input-hiding-duration})
                  (fn [arg]
                    (when (.-finished arg)
                      (dispatch [:set-animation ::message-input-offset-current to-value])))))))

(defn message-input-container [input]
  [view st/message-input-container input])

(defview message-input [input-options validator]
  [input-message [:get-chat-input-text]
   command [:get-chat-command]
   to-msg-id [:get-chat-command-to-msg-id]
   input-command [:get-chat-command-content]
   staged-commands [:get-chat-staged-commands]
   typing-command? [:typing-command?]]
  (let [dismiss-keyboard (not (or command typing-command?))
        response?        (and command to-msg-id)
        message-input?   (not command)]
    [text-input (merge {:style           (cond
                                           message-input? st-message/message-input
                                           response? st-response/command-input
                                           command st-command/command-input)
                        :ref             (fn [input]
                                           (dispatch [:set-message-input input]))
                        :autoFocus       false
                        :blurOnSubmit    dismiss-keyboard
                        :onChangeText    (fn [text]
                                           ((if message-input?
                                              plain-message/set-input-message
                                              command/set-input-message)
                                             text))
                        :onSubmitEditing #(if message-input?
                                           (plain-message/try-send staged-commands
                                                                   input-message
                                                                   dismiss-keyboard)
                                           (command/try-send input-command validator))}
                       (when command
                         {:accessibility-label :command-input})
                       input-options)
     (if message-input?
       input-message
       input-command)]))

(defview plain-message-input-view [{:keys [input-options validator]}]
  [input-message [:get-chat-input-text]
   command [:get-chat-command]
   to-msg-id [:get-chat-command-to-msg-id]
   input-command [:get-chat-command-content]
   staged-commands [:get-chat-staged-commands]
   typing-command? [:typing-command?]]
  (let [dismiss-keyboard (not (or command typing-command?))
        response?        (and command to-msg-id)
        message-input?   (not command)]
    [view st/input-container
     [view st/input-view
      [plain-message/commands-button]
      [message-input-container
       [message-input input-options validator]]
      ;; TODO emoticons: not implemented
      [plain-message/smile-button]
      (if message-input?
        (when (plain-message/message-valid? staged-commands input-message)
          [send-button {:on-press            #(plain-message/try-send staged-commands
                                                                      input-message
                                                                      dismiss-keyboard)
                        :accessibility-label :send-message}])
        (if (command/valid? input-command validator)
          [send-button {:on-press            command/send-command
                        :accessibility-label :stage-command}]
          (when-not response?
            [command/cancel-button])))]]))
