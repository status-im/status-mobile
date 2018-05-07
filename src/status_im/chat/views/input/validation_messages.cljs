(ns status-im.chat.views.input.validation-messages
  (:require-macros [status-im.utils.views :refer [defview letsubs]])
  (:require [re-frame.core :as re-frame]
            [status-im.ui.components.react :as react]
            [status-im.chat.styles.input.validation-message :as style]
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
  (letsubs [chat-input-margin [:chat-input-margin]
            input-height      [:get-current-chat-ui-prop :input-height]
            messages          [:validation-messages]]
           (when messages
             [react/view (style/root (+ input-height chat-input-margin))
              (if (string? messages)
                [messages-list [validation-message {:title       (i18n/label :t/error)
                                                    :description messages}]]
                [messages-list messages])])))
