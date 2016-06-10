(ns status-im.chat.views.command
  (:require [re-frame.core :refer [subscribe dispatch]]
            [status-im.components.react :refer [view
                                              icon
                                              text
                                              text-input
                                              touchable-highlight]]
            [status-im.chat.views.content-suggestions :refer
             [content-suggestions-view]]
            [status-im.chat.styles.input :as st]))

(defn cancel-command-input []
  (dispatch [:cancel-command]))

(defn set-input-message [message]
  (dispatch [:set-chat-command-content message]))

(defn send-command []
  (dispatch [:stage-command])
  (cancel-command-input))

(defn valid? [message validator]
  (if validator
    (validator message)
    (pos? (count message))))

(defn simple-command-input-view [command input-options & {:keys [validator]}]
  (let [message-atom (subscribe [:get-chat-command-content])]
    (fn [command input-options & {:keys [validator]}]
      (let [message @message-atom]
        [view st/command-input-and-suggestions-container
         [content-suggestions-view]
         [view st/command-input-container
          [view (st/command-text-container command)
           [text {:style st/command-text} (:text command)]]
          [text-input (merge {:style           st/command-input
                              :autoFocus       true
                              :onChangeText    set-input-message
                              :onSubmitEditing (fn []
                                                 (when (valid? message validator)
                                                   (send-command)))
                              :accessibility-label :command-input}
                             input-options)
           message]
          (if (valid? message validator)
            [touchable-highlight {:on-press send-command
                                  :accessibility-label :stage-command}
             [view st/send-container [icon :send st/send-icon]]]
            [touchable-highlight {:on-press cancel-command-input}
             [view st/cancel-container
              [icon :close-gray st/cancel-icon]]])]]))))
