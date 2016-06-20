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
            [status-im.chat.styles.message-input :as st]
            [status-im.chat.styles.plain-message :as st-message]
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

(defview message-input [input-options]
  [command? [:animations :command?]
   input-message [:get-chat-input-text]
   input-command [:get-chat-command-content]]
  [text-input (merge {:style           (if command?
                                         st-response/command-input
                                         st-message/message-input)
                      :ref             #(dispatch [:set-message-input %])
                      :autoFocus       false
                      :blurOnSubmit    false
                      :onChangeText    (if command?
                                         command/set-input-message
                                         plain-message/set-input-message)
                      :onSubmitEditing (if command?
                                         command/send-command
                                         plain-message/send)}
                     (when command?
                       {:accessibility-label :command-input})
                     input-options)
   (if command? input-command input-message)])

(defview plain-message-input-view [{:keys [input-options validator]}]
  [command? [:animations :command?]
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
