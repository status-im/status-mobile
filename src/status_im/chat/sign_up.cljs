(ns status-im.chat.sign-up
  (:require [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [status-im.components.styles :refer [default-chat-color]]
            [status-im.utils.utils :refer [http-post]]
            [status-im.utils.random :as random]
            [status-im.utils.sms-listener :refer [add-sms-listener
                                                  remove-sms-listener]]
            [status-im.constants :refer [console-chat-id
                                         text-content-type
                                         content-type-command
                                         content-type-command-request
                                         content-type-status]]
            [status-im.chat.constants :as const]
            [status-im.i18n :refer [label]]
            [clojure.string :as s]))

(defn send-console-message [text]
  {:message-id   (random/id)
   :from         "me"
   :to           console-chat-id
   :content      text
   :content-type text-content-type
   :outgoing     true})

; todo fn name is not too smart, but...
(defn command-content
  [command content]
  {:command (name command)
   :content content})

;; -- Send phone number ----------------------------------------
(defn on-sign-up-response [& [message]]
  (let [message-id (random/id)]
    (dispatch [:received-message
               {:message-id   message-id
                :content      (command-content
                                :confirmation-code
                                (or message (label :t/confirmation-code)))
                :content-type content-type-command-request
                :outgoing     false
                :chat-id      console-chat-id
                :from         console-chat-id
                :to           "me"}])))

(defn handle-sms [{body :body}]
  (when-let [matches (re-matches #"(\d{4})" body)]
    (dispatch [:sign-up-confirm (second matches)])))

(defn start-listening-confirmation-code-sms [db]
  (if-not (:confirmation-code-sms-listener db)
    (assoc db :confirmation-code-sms-listener (add-sms-listener handle-sms))
    db))

(defn stop-listening-confirmation-code-sms [db]
  (when-let [listener (:confirmation-code-sms-listener db)]
    (remove-sms-listener listener)
    (dissoc db :confirmation-code-sms-listener)))

;; -- Send confirmation code and synchronize contacts---------------------------
(defn on-sync-contacts []
  (dispatch [:received-message
             {:message-id   (random/id)
              :content      (label :t/contacts-syncronized)
              :content-type text-content-type
              :outgoing     false
              :chat-id      console-chat-id
              :from         console-chat-id
              :to           "me"}])
  (dispatch [:set-signed-up true]))

(defn sync-contacts []
  ;; TODO 'on-sync-contacts' is never called
  (dispatch [:sync-contacts on-sync-contacts]))

(defn on-send-code-response [body]
  (dispatch [:received-message
             {:message-id   (random/id)
              :content      (:message body)
              :content-type text-content-type
              :outgoing     false
              :chat-id      console-chat-id
              :from         console-chat-id
              :to           "me"}])
  (let [status (keyword (:status body))]
    (when (= :confirmed status)
      (dispatch [:stop-listening-confirmation-code-sms])
      (sync-contacts)
      ;; TODO should be called after sync-contacts?
      (dispatch [:set-signed-up true]))
    (when (= :failed status)
      (on-sign-up-response (label :t/incorrect-code)))))

(defn start-signup []
  (let [message-id (random/id)]
    (dispatch [:received-message
               {:message-id   message-id
                :content      (command-content
                                :phone
                                (label :t/phone-number-required))
                :content-type content-type-command-request
                :outgoing     false
                :chat-id      console-chat-id
                :from         console-chat-id
                :to           "me"}]))
  (dispatch [:received-message
             {:message-id   (random/id)
              :content      (label :t/shake-your-phone)
              :content-type text-content-type
              :outgoing     false
              :chat-id      console-chat-id
              :from         console-chat-id
              :to           "me"}]))

;; -- Saving password ----------------------------------------
(defn account-generation-message []
  (dispatch [:received-message
             {:message-id   const/crazy-math-message-id
              :content      (label :t/account-generation-message)
              :content-type text-content-type
              :outgoing     false
              :chat-id      console-chat-id
              :from         console-chat-id
              :to           "me"}]))

(defn passphrase-messages [mnemonic crazy-math-message?]
  (dispatch [:received-message
             {:message-id   const/passphrase-message-id
              :content      (if crazy-math-message?
                              (label :t/phew-here-is-your-passphrase)
                              (label :t/here-is-your-passphrase))
              :content-type text-content-type
              :outgoing     false
              :chat-id      console-chat-id
              :from         console-chat-id
              :to           "me"}])
  (dispatch [:received-message
             {:message-id   (random/id)
              :content      mnemonic
              :content-type text-content-type
              :outgoing     false
              :chat-id      console-chat-id
              :from         console-chat-id
              :to           "me"}])
  (start-signup))

(def intro-status
  {:message-id   const/intro-status-message-id
   :content      (label :t/intro-status)
   :from         console-chat-id
   :chat-id      console-chat-id
   :content-type content-type-status
   :outgoing     false
   :to           "me"})

(defn intro []
  (dispatch [:received-message intro-status])
  (dispatch [:received-message-when-commands-loaded
             console-chat-id
             {:chat-id      console-chat-id
              :message-id   const/intro-message1-id
              :content      (command-content
                              :password
                              (label :t/intro-message1))
              :content-type content-type-command-request
              :outgoing     false
              :from         console-chat-id
              :to           "me"}]))

(def console-chat
  {:chat-id    console-chat-id
   :name       (s/capitalize console-chat-id)
   :color      default-chat-color
   :group-chat false
   :is-active  true
   :timestamp  (.getTime (js/Date.))
   :photo-path console-chat-id
   :contacts   [{:identity         console-chat-id
                 :text-color       "#FFFFFF"
                 :background-color "#AB7967"}]})

(def console-contact
  {:whisper-identity console-chat-id
   :name             (s/capitalize console-chat-id)
   :photo-path       console-chat-id
   :dapp?            true
   :bot-url          "local://console-bot"
   :dapp-hash        858845357})
