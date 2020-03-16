(ns status-im.ui.screens.chat.input.send-button
  (:require-macros [status-im.utils.views :refer [defview letsubs]])
  (:require [clojure.string :as string]
            [status-im.ui.screens.chat.styles.input.send-button :as style]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.icons.vector-icons :as vector-icons]
            [status-im.ui.components.colors :as colors]))

(defn sendable? [input-text disconnected? login-processing?]
  (let [trimmed (string/trim input-text)]
    (not (or (string/blank? trimmed)
             login-processing?
             disconnected?))))

(defview send-button-view [{:keys [input-text]} on-send-press]
  (letsubs [disconnected? [:disconnected?]
            {:keys [processing]} [:multiaccounts/login]]
    (when (sendable? input-text disconnected? processing)
      [react/touchable-highlight
       {:on-press on-send-press}
       [vector-icons/icon :main-icons/arrow-up
        {:container-style     style/send-message-container
         :accessibility-label :send-message-button
         :color               colors/white-persist}]])))