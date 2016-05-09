(ns syng-im.handlers.sign-up
  (:require [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [syng-im.persistence.simple-kv-store :as kv]
            [syng-im.protocol.state.storage :as s]
            [syng-im.models.chat :refer [set-current-chat-id]]
            [syng-im.models.chats :as c]
            [syng-im.utils.utils :refer [log on-error http-post toast]]
            [syng-im.utils.random :as random]
            [syng-im.utils.phone-number :refer [format-phone-number]]
            [syng-im.constants :refer [text-content-type
                                       content-type-command
                                       content-type-command-request
                                       content-type-status]]))

(defn send-console-msg [text]
  {:msg-id       (random/id)
   :from         "me"
   :to           "console"
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
              :content      (str "Your contacts have been synchronized")
              :content-type text-content-type
              :outgoing     false
              :from         "console"
              :to           "me"}])
  (dispatch [:set-signed-up true]))

(defn sync-contacts []
  (dispatch [:sync-contacts on-sync-contacts]))

(defn on-send-code-response [body]
  (dispatch [:received-msg
             {:msg-id       (random/id)
              ;; todo replace by real check
              :content      (if (= "1111" body)
                              "Confirmed"
                              "Wrong code")
              :content-type text-content-type
              :outgoing     false
              :from         "console"
              :to           "me"}]))

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
                                (str "Thanks! We've sent you a text message with a confirmation "
                                     "code. Please provide that code to confirm your phone number"))
                :content-type content-type-command-request
                :outgoing     false
                :from         "console"
                :to           "me"}])))

;; -- Saving password ----------------------------------------
(defn save-password [password]
  ;; TODO validate and save password  
  (dispatch [:received-msg
             {:msg-id       (random/id)
              :content      (str "OK great! Your password has been saved. Just to let you "
                                 "know you can always change it in the Console by the way "
                                 "it's me the Console nice to meet you!")
              :content-type text-content-type
              :outgoing     false
              :from         "console"
              :to           "me"}])
  (dispatch [:received-msg
             {:msg-id       (random/id)
              :content      (str "I'll generate a passphrase for you so you can restore your "
                                 "access or log in from another device")
              :content-type text-content-type
              :outgoing     false
              :from         "console"
              :to           "me"}])
  (dispatch [:received-msg
             {:msg-id       (random/id)
              :content      "Here's your passphrase:"
              :content-type text-content-type
              :outgoing     false
              :from         "console"
              :to           "me"}])
  ;; TODO generate passphrase
  (let [passphrase (str "The brash businessman's braggadocio and public squabbing with "
                        "candidates in the US presidential election")]
    (dispatch [:received-msg
               {:msg-id       (random/id)
                :content      passphrase
                :content-type text-content-type
                :outgoing     false
                :from         "console"
                :to           "me"}]))
  (dispatch [:received-msg
             {:msg-id       "8"
              :content      "Make sure you had securely written it down"
              :content-type text-content-type
              :outgoing     false
              :from         "console"
              :to           "me"}])
  ;; TODO highlight '!phone'
  (let [msg-id (random/id)]
    (dispatch [:received-msg
               {:msg-id       msg-id
                :content      (command-content
                                :phone
                                (str "Your phone number is also required to use the app. Type the "
                                     "exclamation mark or hit the icon to open the command list "
                                     "and choose the !phone command"))
                :content-type content-type-command-request
                :outgoing     false
                :from         "console"
                :to           "me"}])))

(def intro-status
  {:msg-id          "intro-status"
   :content         (str "The brash businessman’s braggadocio "
                         "and public exchange with candidates "
                         "in the US presidential election")
   :delivery-status "seen"
   :from            "console"
   :chat-id         "console"
   :content-type    content-type-status
   :outgoing        false
   :to              "me"})

(defn intro [db]
  (dispatch [:received-msg intro-status])
  (dispatch [:received-msg
             {:msg-id       "intro-message1"
              :content      "Hello there! It's Syng a Dapp browser in your phone."
              :content-type text-content-type
              :outgoing     false
              :from         "console"
              :to           "me"}])
  (dispatch [:received-msg
             {:msg-id       "intro-message2"
              :content      (str "Syng uses  a highly secure key-pair authentication type "
                                 "to provide you a reliable way to access your account")
              :content-type text-content-type
              :outgoing     false
              :from         "console"
              :to           "me"}])
  (let [msg-id "into-message3"]
    (dispatch [:received-msg
               {:msg-id       msg-id
                :content      (command-content
                                :keypair-password
                                (str "A key pair has been generated and saved to your device. "
                                     "Create a password to secure your key"))
                :content-type content-type-command-request
                :outgoing     false
                :from         "console"
                :to           "me"}]))
  db)

(def console-chat
  {:chat-id    "console"
   :name       "console"
   :group-chat false
   :is-active  true
   :timestamp  (.getTime (js/Date.))
   :contacts   [{:identity         "console"
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
      (if (chats "console")
        db
        (-> db
            (assoc-in [:chats "console"] console-chat)
            (assoc :new-chat console-chat)
            (set-current-chat-id "console")
            (intro))))))
