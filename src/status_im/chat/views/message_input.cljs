(ns status-im.chat.views.message-input
  (:require-macros [status-im.utils.views :refer [defview]])
  (:require [re-frame.core :refer [subscribe]]
            [status-im.components.react :refer [view
                                                animated-view
                                                icon
                                                touchable-highlight
                                                text-input]]
            [status-im.components.animation :as anim]
            [status-im.chat.views.plain-message :as plain-message]
            [status-im.chat.views.command :as command]
            [status-im.chat.styles.message-input :as st]
            [status-im.chat.styles.plain-message :as st-message]
            [status-im.chat.styles.response :as st-response]))

(defn send-button [{:keys [on-press accessibility-label]}]
  [touchable-highlight {:on-press            on-press
                        :accessibility-label accessibility-label}
   [view st/send-container
    [icon :send st/send-icon]]])

(defn message-input-container [input]
  [view st/message-input-container input])

(def plain-input-options
  {:style           st-message/message-input
   :onChangeText    plain-message/set-input-message
   :onSubmitEditing plain-message/send})

(def command-input-options
  {:style           st-response/command-input
   :onChangeText    command/set-input-message
   :onSubmitEditing command/send-command})

(defview message-input [input-options]
  [command? [:command?]
   input-message [:get-chat-input-text]
   input-command [:get-chat-command-content]]
  [text-input (merge
                (if command?
                  command-input-options
                  plain-input-options)
                {:autoFocus           false
                 :blurOnSubmit        false
                 :accessibility-label :input}
                input-options)
   (if command? input-command input-message)])

(defview plain-message-input-view [{:keys [input-options validator]}]
  [command? [:command?]
   input-command [:get-chat-command-content]
   valid-plain-message? [:valid-plain-message?]
   valid-command? [:valid-command? validator]]
  [view st/input-container
   [view st/input-view
    [plain-message/commands-button]
    [message-input-container
     [message-input input-options validator]]
    ;; TODO emoticons: not implemented
    [plain-message/smile-button]
    (when (if command? valid-command? valid-plain-message?)
      (let [on-press (if command?
                       command/send-command
                       plain-message/send)]
        [send-button {:on-press            on-press
                      :accessibility-label :send-message}]))]])
