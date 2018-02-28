(ns status-im.chat.console
  (:require [status-im.ui.components.styles :refer [default-chat-color]]
            [status-im.utils.random :as random]
            [status-im.constants :as const]
            [status-im.i18n :as i18n]
            [clojure.string :as string]))

(defn console-message [{:keys [message-id content content-type]
                        :or   {message-id (random/id)}}]
  {:message-id   message-id
   :outgoing     false
   :chat-id      const/console-chat-id
   :from         const/console-chat-id
   :to           "me"
   :content      content
   :content-type content-type})

(def chat
  {:chat-id      const/console-chat-id
   :name         (string/capitalize const/console-chat-id)
   :color        default-chat-color
   :group-chat   false
   :is-active    true
   :unremovable? true
   :timestamp    (.getTime (js/Date.))
   :photo-path   const/console-chat-id
   :contacts     [{:identity         const/console-chat-id
                   :text-color       "#FFFFFF"
                   :background-color "#AB7967"}]})

(def contact
  {:whisper-identity const/console-chat-id
   :name             (string/capitalize const/console-chat-id)
   :photo-path       const/console-chat-id
   :dapp?            true
   :unremovable?     true
   :bot-url          "local://console-bot"
   :status           (i18n/label :t/intro-status)})
