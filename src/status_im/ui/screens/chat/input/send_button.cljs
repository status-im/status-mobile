(ns status-im.ui.screens.chat.input.send-button
  (:require-macros [status-im.utils.views :refer [defview letsubs]])
  (:require [status-im.ui.screens.chat.styles.input.send-button :as style]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.icons.vector-icons :as vector-icons]
            [status-im.ui.components.colors :as colors]))

(defn sendable? [input-text-empty? disconnected? login-processing?]
  (not (or input-text-empty?
           login-processing?
           disconnected?)))

(defview send-button-view [input-text-empty? on-send-press]
  (letsubs [disconnected? [:disconnected?]
            {:keys [processing]} [:multiaccounts/login]]
    (when (sendable? input-text-empty? disconnected? processing)
      [react/touchable-highlight
       {:on-press on-send-press}
       [vector-icons/icon :main-icons/arrow-up
        {:container-style     style/send-message-container
         :accessibility-label :send-message-button
         :color               colors/white-persist}]])))