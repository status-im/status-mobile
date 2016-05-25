(ns status-im.chat.views.command
  (:require [re-frame.core :refer [subscribe dispatch]]
            [status-im.components.react :refer [view
                                                icon
                                                text
                                                text-input
                                                touchable-highlight]]
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

(defn try-send [message validator]
  (when (valid? message validator)
    (send-command)))

(defn command-icon [command]
  [view (st/command-text-container command)
   [text {:style st/command-text} (:text command)]])
