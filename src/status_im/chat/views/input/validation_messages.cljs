(ns status-im.chat.views.input.validation-messages
  (:require-macros [status-im.utils.views :refer [defview]])
  (:require [re-frame.core :refer [subscribe]]
            [status-im.components.react :as c]
            [status-im.chat.styles.input.validation-message :as style]
            [status-im.utils.listview :as lw]
            [status-im.i18n :as i18n]))

(defn validation-message [{:keys [title description]}]
  [c/view style/message-container
   [c/text {:style style/message-title}
    title]
   [c/text {:style style/message-description}
    description]])

(defn messages-list [markup]
  [c/view {:flex 1}
   markup])

(defview validation-messages-view []
  [chat-input-margin [:chat-input-margin]
   input-height [:chat-ui-props :input-height]
   messages [:chat-ui-props :validation-messages]]
  (when messages
    [c/view (style/root (+ input-height chat-input-margin))
     (if (string? messages)
       [messages-list [validation-message {:title       (i18n/label :t/error)
                                           :description messages}]]
       [messages-list messages])]))
