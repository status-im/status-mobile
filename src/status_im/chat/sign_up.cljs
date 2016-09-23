(ns status-im.chat.sign-up
  (:require [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [status-im.components.styles :refer [default-chat-color]]
            [status-im.utils.utils :refer [http-post]]
            [status-im.utils.random :as random]
            [status-im.utils.sms-listener :refer [add-sms-listener
                                                  remove-sms-listener]]
            [status-im.utils.phone-number :refer [format-phone-number]]
            [status-im.constants :refer [console-chat-id
                                         text-content-type
                                         content-type-command
                                         content-type-command-request
                                         content-type-status]]
            [status-im.i18n :refer [label]]))

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
                :from         console-chat-id
                :to           "me"}])))

(defn handle-sms [{body :body}]
  (when-let [matches (re-matches #"(\d{4})" body)]
    (dispatch [:sign-up-confirm (second matches)])))

(defn start-listening-confirmation-code-sms [db]
  (if (not (:confirmation-code-sms-listener db))
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
              :from         console-chat-id
              :to           "me"}])
  (let [status (keyword (:status body))]
    (when (= :confirmed status)
      (do
        (dispatch [:stop-listening-confirmation-code-sms])
        (sync-contacts)
        ;; TODO should be called after sync-contacts?
        (dispatch [:set-signed-up true])))
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
                :from         console-chat-id
                :to           "me"}])))

;; -- Saving password ----------------------------------------
(defn save-password [password mnemonic]
  ;; TODO validate and save password
  (dispatch [:received-message
             {:message-id   (random/id)
              :content      (label :t/password-saved)
              :content-type text-content-type
              :outgoing     false
              :from         console-chat-id
              :to           "me"
              :new?         false}])
  (dispatch [:received-message
             {:message-id   (random/id)
              :content      (label :t/generate-passphrase)
              :content-type text-content-type
              :outgoing     false
              :from         console-chat-id
              :to           "me"
              :new?         false}])
  (dispatch [:received-message
             {:message-id   (random/id)
              :content      (label :t/here-is-your-passphrase)
              :content-type text-content-type
              :outgoing     false
              :from         console-chat-id
              :to           "me"
              :new?         false}])
  (dispatch [:received-message
             {:message-id   (random/id)
              :content      mnemonic
              :content-type text-content-type
              :outgoing     false
              :from         console-chat-id
              :to           "me"
              :new?         false}])
  (dispatch [:received-message
             {:message-id   "8"
              :content      (label :t/written-down)
              :content-type text-content-type
              :outgoing     false
              :from         console-chat-id
              :to           "me"
              :new?         false}])
  ;; TODO highlight '!phone'
  (start-signup))



(def intro-status
  {:message-id      "intro-status"
   :content         (label :t/intro-status)
   :from            console-chat-id
   :chat-id         console-chat-id
   :content-type    content-type-status
   :outgoing        false
   :to              "me"})

(defn intro [logged-in?]
  (dispatch [:received-message intro-status])
  (dispatch [:received-message
             {:message-id   "intro-message1"
              :content      (label :t/intro-message1)
              :content-type text-content-type
              :outgoing     false
              :from         console-chat-id
              :to           "me"}])
  (when-not logged-in?
    (dispatch [:received-message
               {:message-id   "intro-message2"
                :content      (label :t/intro-message2)
                :content-type text-content-type
                :outgoing     false
                :from         console-chat-id
                :to           "me"}])

    (dispatch [:received-message
               {:message-id   "intro-message3"
                :content      (command-content
                                :keypair
                                (label :t/keypair-generated))
                :content-type content-type-command-request
                :outgoing     false
                :from         console-chat-id
                :to           "me"}])))

(def console-chat
  {:chat-id    console-chat-id
   :name       console-chat-id
   ; todo remove/change dapp config fot console
   :dapp-url   "http://localhost:8185/resources"
   :dapp-hash  858845357
   :color      default-chat-color
   :group-chat false
   :is-active  true
   :timestamp  (.getTime (js/Date.))
   :contacts   [{:identity         console-chat-id
                 :text-color       "#FFFFFF"
                 :background-color "#AB7967"}]})
