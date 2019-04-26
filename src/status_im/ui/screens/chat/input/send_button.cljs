(ns status-im.ui.screens.chat.input.send-button
  (:require-macros [status-im.utils.views :refer [defview letsubs]])
  (:require [clojure.string :as string]
            [re-frame.core :as re-frame]
            [status-im.ui.screens.chat.styles.input.send-button :as style]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.icons.vector-icons :as vector-icons]))

(defn sendable? [input-text disconnected? login-processing?]
  (let [trimmed (string/trim input-text)]
    (not (or (string/blank? trimmed)
             (= trimmed "/")
             login-processing?
             disconnected?))))

(defview send-button-view []
  (letsubs [{:keys [command-completion]} [:chats/selected-chat-command]
            {:keys [input-text]} [:chats/current-chat]
            disconnected? [:disconnected?]
            {:keys [processing]} [:accounts/login]]
    (when (and (sendable? input-text disconnected? processing)
               (or (not command-completion)
                   (#{:complete :less-than-needed} command-completion)))
      [react/touchable-highlight
       {:on-press #(re-frame/dispatch [:chat.ui/send-current-message])}
       [vector-icons/icon :main-icons/arrow-up
        {:container-style     style/send-message-container
         :accessibility-label :send-message-button
         :color               :white}]])))
