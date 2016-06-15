(ns status-im.chat.views.message-input
  (:require-macros [status-im.utils.views :refer [defview]])
  (:require [re-frame.core :refer [subscribe dispatch]]
            [reagent.core :as r]
            [status-im.components.react :refer [view
                                                animated-view
                                                icon
                                                touchable-highlight
                                                text-input]]
            [status-im.components.animation :as anim]
            [status-im.chat.views.plain-message :as plain-message]
            [status-im.chat.views.command :as command]
            [status-im.chat.views.response :as response]
            [status-im.chat.styles.message-input :as st]
            [status-im.chat.styles.plain-message :as st-message]
            [status-im.chat.styles.input :as st-command]
            [status-im.chat.styles.response :as st-response]
            [status-im.constants :refer [response-input-hiding-duration]]))

(defview send-button [{:keys [on-press accessibility-label]}]
  [commands-input-is-switching? [:animations :commands-input-is-switching?]]
  [touchable-highlight {:disabled            commands-input-is-switching?
                        :on-press            on-press
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
  (let [to-message-input-offset (subscribe [:animations :message-input-offset])
        cur-message-input-offset (subscribe [:animations ::message-input-offset-current])
        message-input-offset (anim/create-value (or @cur-message-input-offset 0))
        context {:to-value to-message-input-offset
                 :val message-input-offset}
        on-update (animation-logic context)]
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

(defview message-input [input-options validator]
   [input-message [:get-chat-input-text]
    command [:get-chat-command]
    to-msg-id [:get-chat-command-to-msg-id]
    input-command [:get-chat-command-content]
    staged-commands [:get-chat-staged-commands]
    typing-command? [:typing-command?]
    commands-input-is-switching? [:animations :commands-input-is-switching?]]
   (let [response? (and command to-msg-id)
         message-input? (or (not command) commands-input-is-switching?)
         animation? commands-input-is-switching?]
     [text-input (merge {:style           (cond
                                            message-input? st-message/message-input
                                            response? st-response/command-input
                                            command st-command/command-input)
                         :ref             (fn [input]
                                            (dispatch [:set-message-input input]))
                         :autoFocus       false
                         :blurOnSubmit    false
                         :onChangeText    (fn [text]
                                            (when-not animation?
                                              ((if message-input?
                                                 plain-message/set-input-message
                                                 command/set-input-message)
                                                text)))
                         :onSubmitEditing #(when-not animation?
                                            (if message-input?
                                              (plain-message/try-send staged-commands
                                                                      input-message)
                                              (command/try-send input-command validator)))}
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
   typing-command? [:typing-command?]
   commands-input-is-switching? [:animations :commands-input-is-switching?]]
  (let [response? (and command to-msg-id)
        message-input? (or (not command) commands-input-is-switching?)]
    [view st/input-container
     [view st/input-view
      (if message-input?
        [plain-message/commands-button]
        (when (and command (not response?))
          [command/command-icon command response?]))
      [message-input-container
       [message-input input-options validator]]
      ;; TODO emoticons: not implemented
      (when message-input?
        [plain-message/smile-button])
      (if message-input?
        (when (plain-message/message-valid? staged-commands input-message)
          [send-button {:on-press            #(plain-message/try-send staged-commands
                                                                      input-message)
                        :accessibility-label :send-message}])
        (if (command/valid? input-command validator)
          [send-button {:on-press            command/send-command
                        :accessibility-label :stage-command}]
          (when-not response?
            [command/cancel-button])))]]))
