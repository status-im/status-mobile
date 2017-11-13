(ns status-im.chat.sign-up
  (:require [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [status-im.ui.components.styles :refer [default-chat-color]]
            [status-im.utils.random :as random]
            [status-im.constants :as const]
            [status-im.chat.constants :as chat-const]
            [status-im.i18n :refer [label]]
            [clojure.string :as s]))

(defn send-console-message [text]
  {:message-id   (random/id)
   :from         "me"
   :to           const/console-chat-id
   :content      text
   :content-type const/text-content-type
   :outgoing     true})

;; -- Send phone number ----------------------------------------
(defn- confirmation-code-event [correct? message-id]
  [:received-message
   {:message-id   message-id
    :content      {:command "confirmation-code"
                   :content (if correct?
                              (label :t/confirmation-code)
                              (label :t/incorrect-code))}
    :content-type const/content-type-command-request
    :outgoing     false
    :chat-id      const/console-chat-id
    :from         const/console-chat-id
    :to           "me"}])

(def enter-confirmation-code-event (partial confirmation-code-event true))
(def incorrect-confirmation-code-event (partial confirmation-code-event false))

;; -- Send confirmation code and synchronize contacts---------------------------
(defn contacts-synchronised-event [message-id]
  [:received-message
   {:message-id   message-id
    :content      (label :t/contacts-syncronized)
    :content-type const/text-content-type
    :outgoing     false
    :chat-id      const/console-chat-id
    :from         const/console-chat-id
    :to           "me"}])

(def start-signup-events
  [[:received-message
    {:message-id   (random/id)
     :content      {:command "phone"
                    :content (label :t/phone-number-required)}
     :content-type const/content-type-command-request
     :outgoing     false
     :chat-id      const/console-chat-id
     :from         const/console-chat-id
     :to           "me"}]
   [:received-message
    {:message-id   (random/id)
     :content      (label :t/shake-your-phone)
     :content-type const/text-content-type
     :outgoing     false
     :chat-id      const/console-chat-id
     :from         const/console-chat-id
     :to           "me"}]])

;; -- Saving password ----------------------------------------
(def account-generation-event
  [:received-message
   {:message-id   chat-const/crazy-math-message-id
    :content      (label :t/account-generation-message)
    :content-type const/text-content-type
    :outgoing     false
    :chat-id      const/console-chat-id
    :from         const/console-chat-id
    :to           "me"}])

(def move-to-internal-failure-event
  [:received-message
   {:message-id   chat-const/move-to-internal-failure-message-id
    :content      {:command "grant-permissions"
                   :content (label :t/move-to-internal-failure-message)}
    :content-type const/content-type-command-request
    :outgoing     false
    :chat-id      const/console-chat-id
    :from         const/console-chat-id
    :to           "me"}])

(defn passphrase-messages-events [mnemonic signing-phrase crazy-math-message?]
  (into [[:received-message
          {:message-id   chat-const/passphrase-message-id
           :content      (if crazy-math-message?
                           (label :t/phew-here-is-your-passphrase)
                           (label :t/here-is-your-passphrase))
           :content-type const/text-content-type
           :outgoing     false
           :chat-id      const/console-chat-id
           :from         const/console-chat-id
           :to           "me"}]
         [:received-message
          {:message-id   (random/id)
           :content      mnemonic
           :content-type const/text-content-type
           :outgoing     false
           :chat-id      const/console-chat-id
           :from         const/console-chat-id
           :to           "me"}]
         [:received-message
          {:message-id   chat-const/signing-phrase-message-id
           :content      (label :t/here-is-your-signing-phrase)
           :content-type const/text-content-type
           :outgoing     false
           :chat-id      const/console-chat-id
           :from         const/console-chat-id
           :to           "me"}]
         [:received-message
          {:message-id   (random/id)
           :content      signing-phrase
           :content-type const/text-content-type
           :outgoing     false
           :chat-id      const/console-chat-id
           :from         const/console-chat-id
           :to           "me"}]]
        start-signup-events))

(def intro-status
  {:message-id   chat-const/intro-status-message-id
   :content      (label :t/intro-status)
   :from         const/console-chat-id
   :chat-id      const/console-chat-id
   :content-type const/content-type-status
   :outgoing     false
   :to           "me"})

(def intro-events
  [[:received-message intro-status]
   [:received-message-when-commands-loaded
    const/console-chat-id
    {:chat-id      const/console-chat-id
     :message-id   chat-const/intro-message1-id
     :content      {:command "password"
                    :content (label :t/intro-message1)}
     :content-type const/content-type-command-request
     :outgoing     false
     :from         const/console-chat-id
     :to           "me"}]])

(def console-chat
  {:chat-id      const/console-chat-id
   :name         (s/capitalize const/console-chat-id)
   :color        default-chat-color
   :group-chat   false
   :is-active    true
   :unremovable? true
   :timestamp    (.getTime (js/Date.))
   :photo-path   const/console-chat-id
   :contacts     [{:identity         const/console-chat-id
                   :text-color       "#FFFFFF"
                   :background-color "#AB7967"}]})

(def console-contact
  {:whisper-identity  const/console-chat-id
   :name              (s/capitalize const/console-chat-id)
   :photo-path        const/console-chat-id
   :dapp?             true
   :unremovable?      true
   :bot-url           "local://console-bot"
   :dapp-hash         858845357})
