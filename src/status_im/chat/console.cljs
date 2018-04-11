(ns status-im.chat.console
  (:require [status-im.ui.components.styles :refer [default-chat-color]]
            [status-im.utils.random :as random]
            [status-im.constants :as constants]
            [status-im.utils.clocks :as utils.clocks]
            [status-im.i18n :as i18n]
            [clojure.string :as string]))

(defn console-message [{:keys [message-id content content-type]
                        :or   {message-id (random/id)}}]
  {:message-id   message-id
   :outgoing     false
   :chat-id      constants/console-chat-id
   :from         constants/console-chat-id
   :to           "me"
   :clock-value  (utils.clocks/send 0)
   :content      content
   :content-type content-type})

(def chat
  {:chat-id      constants/console-chat-id
   :name         (i18n/label :t/status-console)
   :color        default-chat-color
   :group-chat   false
   :is-active    true
   :unremovable? true
   :timestamp    (.getTime (js/Date.))
   :photo-path   (str "contacts://" constants/console-chat-id)
   :contacts     [{:identity         constants/console-chat-id
                   :text-color       "#FFFFFF"
                   :background-color "#AB7967"}]
   :last-clock-value   0})

(def contact
  {:whisper-identity constants/console-chat-id
   :name             (i18n/label :t/status-console)
   :photo-path       (str "contacts://" constants/console-chat-id)
   :dapp?            true
   :unremovable?     true
   :bot-url          "local://console-bot"
   :status           (i18n/label :t/intro-status)})
