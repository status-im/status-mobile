(ns syng-im.chat.views.command
  (:require [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [syng-im.components.react :refer [view
                                              image
                                              icon
                                              text
                                              text-input
                                              touchable-highlight]]
            [syng-im.resources :as res]
            [syng-im.chat.styles.input :as st]))

(defn cancel-command-input []
  (dispatch [:cancel-command]))

(defn set-input-message [message]
  (dispatch [:set-chat-command-content message]))

(defn send-command []
  (dispatch [:stage-command])
  (cancel-command-input))

(defn simple-command-input-view [command input-options]
  (let [message-atom (subscribe [:get-chat-command-content])]
    (fn [command input-options]
      (let [message @message-atom]
        [view st/command-input-container
         [view (st/command-text-container command)
          [text {:style st/command-text} (:text command)]]
         [text-input (merge {:style           st/command-input
                             :autoFocus       true
                             :onChangeText    set-input-message
                             :onSubmitEditing send-command}
                            input-options)
          message]
         (if (pos? (count message))
           [touchable-highlight {:on-press send-command}
            [view st/send-container [icon :send st/send-icon]]]
           [touchable-highlight {:on-press cancel-command-input}
            [view st/cancel-container
             [image {:source res/icon-close-gray
                     :style  st/cancel-icon}]]])]))))
