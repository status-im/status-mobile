(ns status-im.chat.console
  (:require [status-im.ui.components.styles :refer [default-chat-color]]
            [status-im.constants :as constants]
            [status-im.utils.clocks :as utils.clocks]
            [status-im.i18n :as i18n]))

(defn console-message [{:keys [timestamp message-id content content-type]}]
  {:message-id   message-id
   :chat-id      constants/console-chat-id
   :from         constants/console-chat-id
   :to           "me"
   :timestamp    timestamp
   :clock-value  (utils.clocks/send 0)
   :content      content
   :content-type content-type
   :show?        true})

(def chat
  {:chat-id          constants/console-chat-id
   :name             (i18n/label :t/status-console)
   :color            default-chat-color
   :group-chat       false
   :is-active        true
   :unremovable?     true
   :timestamp        (.getTime (js/Date.))
   :photo-path       (str "contacts://" constants/console-chat-id)
   :contacts         [constants/console-chat-id]
   :last-clock-value 0})

(def contact
  {:whisper-identity constants/console-chat-id
   :name             (i18n/label :t/status-console)
   :photo-path       (str "contacts://" constants/console-chat-id)
   :dapp?            true
   :unremovable?     true
   :bot-url          "local://console-bot"
   :status           (i18n/label :t/intro-status)})
