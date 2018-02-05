(ns status-im.chat.console
  (:require [status-im.ui.components.styles :refer [default-chat-color]]
            [status-im.utils.random :as random]
            [status-im.constants :as const]
            [status-im.chat.constants :as chat-const]
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

(def shake-your-phone-message
  (console-message {:content      (i18n/label :t/shake-your-phone)
                    :content-type const/text-content-type}))

(def account-generation-message
  (console-message {:message-id   chat-const/crazy-math-message-id
                    :content      (i18n/label :t/account-generation-message)
                    :content-type const/text-content-type}))

(def move-to-internal-failure-message
  (console-message {:message-id   chat-const/move-to-internal-failure-message-id
                    :content      {:command "grant-permissions"
                                   :content (i18n/label :t/move-to-internal-failure-message)}
                    :content-type const/content-type-command-request}))

(defn passphrase-messages [mnemonic signing-phrase crazy-math-message?]
  [(console-message {:message-id   chat-const/passphrase-message-id
                     :content      (if crazy-math-message?
                                     (i18n/label :t/phew-here-is-your-passphrase)
                                     (i18n/label :t/here-is-your-passphrase))
                     :content-type const/text-content-type})

   (console-message {:message-id   (random/id)
                     :content      mnemonic
                     :content-type const/text-content-type})

   (console-message {:message-id   chat-const/signing-phrase-message-id
                     :content      (i18n/label :t/here-is-your-signing-phrase)
                     :content-type const/text-content-type})

   (console-message {:message-id   (random/id)
                     :content      signing-phrase
                     :content-type const/text-content-type})])

(def intro-status-message
  (console-message {:message-id   chat-const/intro-status-message-id
                    :content      (i18n/label :t/intro-status)
                    :content-type const/content-type-status}))

(def intro-message1
  (console-message {:message-id   chat-const/intro-message1-id
                    :content      {:command "password"
                                   :content (i18n/label :t/intro-message1)}
                    :content-type const/content-type-command-request}))

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
