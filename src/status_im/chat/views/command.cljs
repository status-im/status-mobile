(ns status-im.chat.views.command
  (:require-macros [status-im.utils.views :refer [defview]])
  (:require [re-frame.core :refer [subscribe dispatch]]
            [status-im.components.react :refer [view
                                                icon
                                                text
                                                text-input
                                                touchable-highlight]]
            [status-im.chat.styles.input :as st]))

(defn cancel-command-input []
  (dispatch [:start-cancel-command]))

(defn set-input-message [message]
  (dispatch [:set-chat-command-content message]))

(defn ready? [message]
  (not (empty? message)))

(defn try-send []
  (dispatch [:try-stage-command]))

(defn command-icon [command]
  [view (st/command-text-container command)
   [text {:style st/command-text} (:text command)]])

(defview cancel-button []
  [commands-input-is-switching? [:animations :commands-input-is-switching?]]
  [touchable-highlight {:disabled commands-input-is-switching?
                        :on-press cancel-command-input}
   [view st/cancel-container
    [icon :close-gray st/cancel-icon]]])
