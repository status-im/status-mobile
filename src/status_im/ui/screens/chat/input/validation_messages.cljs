(ns status-im.ui.screens.chat.input.validation-messages
  (:require-macros [status-im.utils.views :refer [defview letsubs]])
  (:require [re-frame.core :as re-frame]
            [status-im.ui.components.react :as react]
            [status-im.ui.screens.chat.styles.input.validation-message :as style]
            [status-im.i18n :as i18n]))

(defn validation-message [{:keys [title description]}]
  [react/view style/message-container
   [react/text {:style style/message-title}
    title]
   [react/text {:style style/message-description}
    description]])

(defn- messages-list [markup]
  [react/view {:flex 1}
   markup])

(defview validation-messages-view []
  (letsubs [chat-input-margin [:chats/input-margin]
            input-height      [:chats/current-chat-ui-prop :input-height]
            validation-result [:chats/validation-messages]]
    (when validation-result
      (let [message (if (string? validation-result)
                      {:title       (i18n/label :t/error)
                       :description validation-result}
                      validation-result)]
        [react/view (style/root (+ input-height chat-input-margin))
         [messages-list [validation-message message]]]))))
