(ns status-im.chat.sign-up
  ;status-im.handlers.sign-up
  (:require [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [status-im.persistence.simple-kv-store :as kv]
            [status-im.protocol.state.storage :as s]
            [status-im.models.chats :as c]
            [status-im.components.styles :refer [default-chat-color]]
            [status-im.utils.utils :refer [log on-error http-post toast]]
            [status-im.utils.random :as random]
            [status-im.utils.sms-listener :refer [add-sms-listener
                                                  remove-sms-listener]]
            [status-im.utils.phone-number :refer [format-phone-number]]
            [status-im.constants :refer [text-content-type
                                         content-type-command
                                         content-type-command-request
                                         content-type-status]]
            [status-im.i18n :refer [t]]))

(defn send-console-msg [text]
  {:msg-id       (random/id)
   :from         (t :me)
   :to           (t :console)
   :content      text
   :content-type text-content-type
   :outgoing     true})

(defn- set-signed-up [db signed-up]
  (s/put kv/kv-store :signed-up signed-up)
  (assoc db :signed-up signed-up))


;; -- Send confirmation code and synchronize contacts---------------------------
(defn on-sync-contacts []
  (dispatch [:received-msg
             {:msg-id       (random/id)
              :content      (t :sign-up.contacts-syncronized)
              :content-type text-content-type
              :outgoing     false
              :from         (t :console)
              :to           (t :me)}])
  (dispatch [:set-signed-up true]))

(defn sync-contacts []
  ;; TODO 'on-sync-contacts' is never called
  (dispatch [:sync-contacts on-sync-contacts]))

(defn on-send-code-response [body]
  (dispatch [:received-msg
             {:msg-id       (random/id)
              :content      (:message body)
              :content-type text-content-type
              :outgoing     false
              :from         (t :console)
              :to           (t :me)}])
  (when (:confirmed body)
    (dispatch [:stop-listening-confirmation-code-sms])
    (sync-contacts)
    ;; TODO should be called after sync-contacts?
    (dispatch [:set-signed-up true])))

; todo fn name is not too smart, but...
(defn command-content
  [command content]
  {:command (name command)
   :content content})

;; -- Send phone number ----------------------------------------
(defn on-sign-up-response []
  (let [msg-id (random/id)]
    (dispatch [:received-msg
               {:msg-id       msg-id
                :content      (command-content
                                :confirmation-code
                                (t :sign-up.confirmation-code))
                :content-type content-type-command-request
                :outgoing     false
                :from         (t :console)
                :to           (t :me)}])))

(defn handle-sms [{body :body}]
  (when-let [matches (re-matches #"(\d{4})" body)]
    (dispatch [:sign-up-confirm (second matches)])))

(defn start-listening-confirmation-code-sms [db]
  (when (not (:confirmation-code-sms-listener db))
    (assoc db :confirmation-code-sms-listener (add-sms-listener handle-sms))))

(defn stop-listening-confirmation-code-sms [db]
  (when-let [listener (:confirmation-code-sms-listener db)]
    (remove-sms-listener listener)
    (dissoc db :confirmation-code-sms-listener)))

;; -- Saving password ----------------------------------------
(defn save-password [password]
  ;; TODO validate and save password
  (dispatch [:received-msg
             {:msg-id       (random/id)
              :content      (t :sign-up.password-saved)
              :content-type text-content-type
              :outgoing     false
              :from         (t :console)
              :to           (t :me)}])
  (dispatch [:received-msg
             {:msg-id       (random/id)
              :content      (t :sign-up.generate-passphrase)
              :content-type text-content-type
              :outgoing     false
              :from         (t :console)
              :to           (t :me)}])
  (dispatch [:received-msg
             {:msg-id       (random/id)
              :content      (t :sign-up.passphrase)
              :content-type text-content-type
              :outgoing     false
              :from         (t :console)
              :to           (t :me)}])
  ;; TODO generate passphrase
  (let [passphrase (str "The brash businessman's braggadocio and public squabbing with "
                        "candidates in the US presidential election")]
    (dispatch [:received-msg
               {:msg-id       (random/id)
                :content      passphrase
                :content-type text-content-type
                :outgoing     false
                :from         (t :console)
                :to           (t :me)}]))
  (dispatch [:received-msg
             {:msg-id       "8"
              :content      (t :sign-up.written-down)
              :content-type text-content-type
              :outgoing     false
              :from         (t :console)
              :to           (t :me)}])
  ;; TODO highlight '!phone'
  (let [msg-id (random/id)]
    (dispatch [:received-msg
               {:msg-id       msg-id
                :content      (command-content
                                :phone
                                (t :sign-up.phone-number-required))
                :content-type content-type-command-request
                :outgoing     false
                :from         (t :console)
                :to           (t :me)}])))

(def intro-status
  {:msg-id          "intro-status"
   :content         (t :sign-up.intro-status)
   :delivery-status "seen"
   :from            (t :console)
   :chat-id         (t :console)
   :content-type    content-type-status
   :outgoing        false
   :to              (t :me)})

(defn intro [db]
  (dispatch [:received-msg intro-status])
  (dispatch [:received-msg
             {:msg-id       "intro-message1"
              :content      (t :sign-up.intro-message1)
              :content-type text-content-type
              :outgoing     false
              :from         (t :console)
              :to           (t :me)}])
  (dispatch [:received-msg
             {:msg-id       "intro-message2"
              :content      (t :sign-up.intro-message2)
              :content-type text-content-type
              :outgoing     false
              :from         (t :console)
              :to           (t :me)}])
  (let [msg-id "into-message3"]
    (dispatch [:received-msg
               {:msg-id       msg-id
                :content      (command-content
                                :keypair-password
                                (t :sign-up.keypair-generated))
                :content-type content-type-command-request
                :outgoing     false
                :from         (t :console)
                :to           (t :me)}]))
  db)

(def console-chat
  {:chat-id    (t :console)
   :name       (t :console)
   :color      default-chat-color
   :group-chat false
   :is-active  true
   :timestamp  (.getTime (js/Date.))
   :contacts   [{:identity         (t :console)
                 :text-color       "#FFFFFF"
                 :background-color "#AB7967"}]})

(defn create-chat [handler]
  (fn [db]
    (let [{:keys [new-chat] :as db'} (handler db)]
      (when new-chat
        (c/create-chat new-chat))
      (dissoc db' :new-chat))))

(def init
  (create-chat
    (fn [{:keys [chats] :as db}]
      (if (chats (t :console))
        db
        (-> db
            (assoc-in [:chats (t :console)] console-chat)
            (assoc :new-chat console-chat)
            (assoc :current-chat-id (t :console))
            (intro))))))
