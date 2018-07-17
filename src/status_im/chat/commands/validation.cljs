(ns status-im.chat.commands.validation
  (:require [status-im.ui.components.react :as react]
            [status-im.chat.commands.styles.validation :as styles]))

(defn validation-message [{:keys [title description]}]
  [react/view styles/message-container
   [react/text {:style styles/message-title}
    title]
   [react/text {:style styles/message-description}
    description]])
