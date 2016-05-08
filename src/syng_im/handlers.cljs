(ns syng-im.handlers
  (:require
    [re-frame.core :refer [register-handler after dispatch debug enrich]]
    [schema.core :as s :include-macros true]
    [syng-im.db :as db :refer [app-db schema]]
    [syng-im.protocol.api :refer [init-protocol]]
    [syng-im.protocol.protocol-handler :refer [make-handler]]
    [syng-im.models.protocol :refer [update-identity
                                     set-initialized]]
    [syng-im.models.user-data :as user-data]
    [syng-im.models.contacts :as contacts]
    [syng-im.models.messages :refer [save-message
                                     update-message!
                                     message-by-id
                                     get-messages]]
    [syng-im.models.commands :as commands :refer [set-chat-command
                                                  set-response-chat-command
                                                  set-chat-command-content
                                                  set-chat-command-request
                                                  stage-command
                                                  unstage-command
                                                  set-commands]]
    [syng-im.handlers.server :as server]
    [syng-im.handlers.contacts :as contacts-service]
    [syng-im.handlers.suggestions :refer [get-command
                                          handle-command
                                          get-command-handler
                                          load-commands
                                          apply-staged-commands
                                          check-suggestion]]
    [syng-im.handlers.sign-up :as sign-up-service]
    [syng-im.models.chats :refer [chat-exists?
                                  create-chat
                                  chat-add-participants
                                  chat-remove-participants
                                  set-chat-active
                                  re-join-group-chat
                                  chat-by-id2] :as chats]
    [syng-im.models.chat :refer [signal-chat-updated
                                 set-current-chat-id
                                 current-chat-id
                                 update-new-group-selection
                                 update-new-participants-selection
                                 clear-new-group
                                 clear-new-participants
                                 new-group-selection
                                 set-chat-input-text
                                 new-participants-selection]]
    [syng-im.utils.logging :as log]
    [syng-im.protocol.api :as api]
    [syng-im.constants :refer [text-content-type
                               content-type-command]]
    [syng-im.navigation :refer [nav-push
                                nav-replace
                                nav-pop]]
    [syng-im.utils.crypt :refer [gen-random-bytes]]
    [syng-im.utils.random :as random]
    [clojure.string :as str]
    [syng-im.components.react :as r]))

;; -- Middleware ------------------------------------------------------------
;;
;; See https://github.com/Day8/re-frame/wiki/Using-Handler-Middleware
;;
(defn check-and-throw
  "throw an exception if db doesn't match the schema."
  [a-schema db]
  (if-let [problems (s/check a-schema db)]
    (throw (js/Error. (str "schema check failed: " problems)))))

(def validate-schema-mw
  (after (partial check-and-throw schema)))


;; -- Common --------------------------------------------------------------

(register-handler :initialize-db
  (fn [_ _] app-db))

(register-handler :set-loading
  (fn [db [_ value]]
    (assoc db :loading value)))

(register-handler :initialize-crypt
  (fn [db _]
    (log/debug "initializing crypt")
    (gen-random-bytes 1024 (fn [{:keys [error buffer]}]
                             (if error
                               (do
                                 (log/error "Failed to generate random bytes to initialize sjcl crypto")
                                 (dispatch [:notify-user {:type  :error
                                                          :error error}]))
                               (do
                                 (->> (.toString buffer "hex")
                                      (.toBits (.. js/ecc -sjcl -codec -hex))
                                      (.addEntropy (.. js/ecc -sjcl -random)))
                                 (dispatch [:crypt-initialized])))))
    db))

(register-handler :crypt-initialized
  (fn [db _]
    (log/debug "crypt initialized")
    db))

(register-handler :load-commands
  (fn [db [action]]
    (log/debug action)
    (load-commands)
    db))

(register-handler :set-commands
  (fn [db [action commands]]
    (log/debug action commands)
    (set-commands db commands)))

(register-handler :set-show-actions
  (fn [db [action show-actions]]
    (log/debug action)
    (assoc-in db db/show-actions-path show-actions)))

;; -- Protocol --------------------------------------------------------------

(register-handler :initialize-protocol
  (fn [db [_]]
    (init-protocol (make-handler db))
    db))

(register-handler :protocol-initialized
  (fn [db [_ identity]]
    (-> db
        (update-identity identity)
        (set-initialized true))))

(defn gen-messages [n]
  (mapv (fn [_]
          (let [id (random-uuid)]
            {:msg-id       id
             :content      (str id
                                "ooops sdfg  dsfg"
                                "s dfg\ndsfg dfg\ndsfgdsfgdsfg")
             :content-type text-content-type
             :outgoing     false
             :from         "console"
             :to           "me"})) (range n)))

(defn store-message!
  [{:keys [new-message]} [_ {chat-id  :from}]]
  (save-message chat-id new-message))

(defn add-message-to-db
  [db chat-id message]
  (let [messages [:chats chat-id :messages]]
    (update-in db messages conj message)))

(defn check-author-direction
  [db chat-id {:keys [from outgoing] :as message}]
  (let [previous-message (first (get-in db [:chats chat-id :messages]))]
    (merge message
           {:same-author    (if previous-message
                              (= (:from previous-message) from)
                              true)
            :same-direction (if previous-message
                              (= (:outgoing previous-message) outgoing)
                              true)})))

(defn receive-message
  [db [_ {chat-id :from :as message}]]
  (let [message' (check-author-direction db chat-id message)]
    (-> db
        (add-message-to-db chat-id message')
        (assoc :new-message message'))))

(register-handler :received-msg
  (-> receive-message
      ((after store-message!))))

(register-handler :group-received-msg
  (fn [db [action {chat-id :group-id :as msg}]]
    (log/debug action "msg" msg)
    (save-message chat-id msg)
    (signal-chat-updated db chat-id)))

(defn system-message [msg-id content]
  {:from         "system"
   :msg-id       msg-id
   :content      content
   :content-type text-content-type})

(defn joined-chat-msg [chat-id from msg-id]
  (let [contact-name (:name (contacts/contact-by-identity from))]
    (save-message chat-id {:from         "system"
                           :msg-id       (str msg-id "_" from)
                           :content      (str (or contact-name from) " received chat invitation")
                           :content-type text-content-type})))

(defn participant-invited-to-group-msg [chat-id identity from msg-id]
  (let [inviter-name (:name (contacts/contact-by-identity from))
        invitee-name (if (= identity (api/my-identity))
                       "You"
                       (:name (contacts/contact-by-identity identity)))]
    (save-message chat-id {:from         "system"
                           :msg-id       msg-id
                           :content      (str (or inviter-name from) " invited " (or invitee-name identity))
                           :content-type text-content-type})))

(defn participant-removed-from-group-msg [chat-id identity from msg-id]
  (let [remover-name (:name (contacts/contact-by-identity from))
        removed-name (:name (contacts/contact-by-identity identity))]
    (->> (str (or remover-name from) " removed " (or removed-name identity))
         (system-message msg-id)
         (save-message chat-id))))

(defn you-removed-from-group-msg [chat-id from msg-id]
  (let [remover-name (:name (contacts/contact-by-identity from))]
    (->> (str (or remover-name from) " removed you from group chat")
         (system-message msg-id)
         (save-message chat-id))))

(defn participant-left-group-msg [chat-id from msg-id]
  (let [left-name (:name (contacts/contact-by-identity from))]
    (->> (str (or left-name from) " left")
         (system-message msg-id)
         (save-message chat-id))))

(defn removed-participant-msg [chat-id identity]
  (let [contact-name (:name (contacts/contact-by-identity identity))]
    (->> (str "You've removed " (or contact-name identity))
         (system-message (random/id))
         (save-message chat-id))))

(defn left-chat-msg [chat-id]
  (save-message chat-id {:from         "system"
                         :msg-id       (random/id)
                         :content      "You left this chat"
                         :content-type text-content-type}))

(register-handler :group-chat-invite-acked
  (fn [db [action from group-id ack-msg-id]]
    (log/debug action from group-id ack-msg-id)
    (joined-chat-msg group-id from ack-msg-id)
    (signal-chat-updated db group-id)))

(register-handler :participant-removed-from-group
  (fn [db [action from group-id identity msg-id]]
    (log/debug action msg-id from group-id identity)
    (chat-remove-participants group-id [identity])
    (participant-removed-from-group-msg group-id identity from msg-id)
    (signal-chat-updated db group-id)))

(register-handler :you-removed-from-group
  (fn [db [action from group-id msg-id]]
    (log/debug action msg-id from group-id)
    (you-removed-from-group-msg group-id from msg-id)
    (set-chat-active group-id false)
    (signal-chat-updated db group-id)))

(register-handler :participant-left-group
  (fn [db [action from group-id msg-id]]
    (log/debug action msg-id from group-id)
    (if (= (api/my-identity) from)
      db
      (do (participant-left-group-msg group-id from msg-id)
          (signal-chat-updated db group-id)))))

(register-handler :participant-invited-to-group
  (fn [db [action from group-id identity msg-id]]
    (log/debug action msg-id from group-id identity)
    (participant-invited-to-group-msg group-id identity from msg-id)
    (signal-chat-updated db group-id)))

(register-handler :acked-msg
  (fn [db [action from msg-id]]
    (log/debug action from msg-id)
    (update-message! {:msg-id          msg-id
                      :delivery-status :delivered})
    (signal-chat-updated db from)))

(register-handler :msg-delivery-failed
  (fn [db [action msg-id]]
    (log/debug action msg-id)
    (update-message! {:msg-id          msg-id
                      :delivery-status :failed})
    (let [{:keys [chat-id]} (message-by-id msg-id)]
      (signal-chat-updated db chat-id))))

(defn console? [s]
  (= "console" s))

(def not-console?
  (complement console?))

(defn prepare-message
  [{:keys [identity current-chat-id] :as db} _]
  (let [text    (get-in db [:chats current-chat-id :input-text])
        {:keys [command]} (check-suggestion db (str text " "))
        message (check-author-direction
                  db current-chat-id
                  {:msg-id       (random/id)
                   :chat-id      current-chat-id
                   :content      text
                   :to           current-chat-id
                   :from         identity
                   :content-type text-content-type
                   :outgoing     true})]
    (if command
      (set-chat-command db command)
      (assoc db :new-message (when-not (str/blank? text) message)))))

(defn prepare-command [identity chat-id staged-command]
  (let [command-key (get-in staged-command [:command :command])
        content     {:command (name command-key)
                     :content (:content staged-command)}]
    {:msg-id       (random/id)
     :from         identity
     :to           chat-id
     :content      content
     :content-type content-type-command
     :outgoing     true
     :handler      (:handler staged-command)}))

(defn prepare-staged-commans
  [{:keys [current-chat-id identity] :as db} _]
  (let [staged-commands (get-in db [:chats current-chat-id :staged-commands])]
    (->> staged-commands
         (map #(prepare-command identity current-chat-id %))
         ;todo this is wrong :(
         (map #(check-author-direction db current-chat-id %))
         (assoc db :new-commands))))

(defn add-message
  [{:keys [new-message current-chat-id] :as db}]
  (if new-message
    (add-message-to-db db current-chat-id new-message)
    db))

(defn add-commands
  [{:keys [new-commands current-chat-id] :as db}]
  (reduce
    #(add-message-to-db %1 current-chat-id %2)
    db
    new-commands))

(defn clear-input
  [{:keys [current-chat-id new-message] :as db} _]
  (if new-message
    (assoc-in db [:chats current-chat-id :input-text] nil)
    db))

(defn clear-staged-commands
  [{:keys [current-chat-id] :as db} _]
  (assoc-in db [:chats current-chat-id :staged-commands] []))

(defn send-message!
  [{:keys [new-message current-chat-id]} _]
  (when (and new-message (not-console? current-chat-id))
    (api/send-user-msg {:to      current-chat-id
                        :content (:content new-message)})))

(defn save-message-to-realm!
  [{:keys [new-message current-chat-id]} _]
  (when new-message
    (save-message current-chat-id new-message)))

(defn save-commands-to-realm!
  [{:keys [new-commands current-chat-id]} _]
  (doseq [new-command new-commands]
    (save-message current-chat-id (dissoc new-command :handler))))

(defn handle-commands
  [{:keys [new-commands]}]
  (doseq [{{content :content} :content
           handler            :handler} new-commands]
    (when handler
      (handler content))))

(register-handler :send-chat-msg
  (-> prepare-message
      ((enrich prepare-staged-commans))
      ((enrich add-message))
      ((enrich add-commands))
      ((enrich clear-input))
      ((enrich clear-staged-commands))
      ((after (fn [_ _] (r/dismiss-keyboard!))))
      ((after send-message!))
      ((after save-message-to-realm!))
      ((after save-commands-to-realm!))
      ((after handle-commands))))

(register-handler :leave-group-chat
  (fn [db [action navigator]]
    (log/debug action)
    (let [chat-id (current-chat-id db)]
      (api/leave-group-chat chat-id)
      (set-chat-active chat-id false)
      (left-chat-msg chat-id)
      (signal-chat-updated db chat-id))))

(register-handler :send-group-chat-msg
  (fn [db [action chat-id text]]
    (log/debug action "chat-id" chat-id "text" text)
    (let [{msg-id       :msg-id
           {from :from} :msg} (api/send-group-user-msg {:group-id chat-id
                                                        :content  text})
          msg {:msg-id       msg-id
               :from         from
               :to           nil
               :content      text
               :content-type text-content-type
               :outgoing     true}]
      (save-message chat-id msg)
      (signal-chat-updated db chat-id))))

;; -- User data --------------------------------------------------------------

(register-handler :set-user-phone-number
  (fn [db [_ value]]
    (assoc db :user-phone-number value)))

(register-handler :load-user-phone-number
  (fn [db [_]]
    (user-data/load-phone-number)
    db))

;; -- Sign up --------------------------------------------------------------

(register-handler :sign-up
  (-> (fn [db [_ phone-number]]
        (assoc db :user-phone-number phone-number))
      ((after (fn [& _] (sign-up-service/on-sign-up-response))))))

(register-handler :sign-up-confirm
  (fn [db [_ confirmation-code]]
    (sign-up-service/on-send-code-response confirmation-code)
    db))

(register-handler :sync-contacts
  (fn [db [_ handler]]
    (contacts-service/sync-contacts handler)
    db))

;; -- Contacts --------------------------------------------------------------

(register-handler :load-syng-contacts
  (fn [db [_ value]]
    (contacts/load-syng-contacts db)))

;; -- Chats --------------------------------------------------------------

(register-handler :show-chat
  (fn [db [action chat-id navigator nav-type]]
    (log/debug action "chat-id" chat-id)
    (let [db (set-current-chat-id db chat-id)]
      (dispatch [:navigate-to navigator {:view-id :chat} nav-type])
      db)))

(register-handler :init-console-chat
  (fn [db [_]]
    (sign-up-service/init db)))

(register-handler :set-signed-up
  (fn [db [_ signed-up]]
    (sign-up-service/set-signed-up db signed-up)))

;; -- Chat --------------------------------------------------------------

(defn update-text [db [_ text]]
  (set-chat-input-text db text))

(defn update-command [db [_ text]]
  (let [{:keys [command]} (check-suggestion db text)]
    (set-chat-command db command)))

(register-handler :set-chat-input-text
  ((enrich update-command) update-text))

(register-handler :set-chat-command
  (fn [db [_ command-key]]
    ;; todo what is going on there?!
    (set-chat-command db command-key)))

(register-handler :stage-command
  (fn [{:keys [current-chat-id] :as db} _]
    (let [db           (set-chat-input-text db nil)
          {:keys [command content]}
          (get-in db [:chats current-chat-id :command-input])
          command-info {:command command
                        :content content
                        :handler (:handler command)}]
      (stage-command db command-info))))

(register-handler :unstage-command
  (fn [db [_ staged-command]]
    (let []
      (unstage-command db staged-command))))

(register-handler :set-response-chat-command
  (fn [db [_ to-msg-id command-key]]
    (set-response-chat-command db to-msg-id command-key)))

(register-handler :set-chat-command-content
  (fn [db [_ content]]
    (set-chat-command-content db content)))

(register-handler :set-chat-command-request
  (fn [db [_ msg-id handler]]
    (set-chat-command-request db msg-id handler)))

(register-handler :show-contacts
  (fn [db [action navigator]]
    (nav-push navigator {:view-id :contact-list})
    db))

(register-handler :select-new-participant
  (fn [db [action identity add?]]
    (log/debug action identity add?)
    (update-new-participants-selection db identity add?)))

(register-handler :show-remove-participants
  (fn [db [action navigator]]
    (log/debug action)
    (nav-push navigator {:view-id :remove-participants})
    (clear-new-participants db)))

(register-handler :remove-selected-participants
  (fn [db [action navigator]]
    (log/debug action)
    (let [identities (vec (new-participants-selection db))
          chat-id    (current-chat-id db)]
      (chat-remove-participants chat-id identities)
      (nav-pop navigator)
      (doseq [ident identities]
        (api/group-remove-participant chat-id ident)
        (removed-participant-msg chat-id ident))
      (signal-chat-updated db chat-id))))

(register-handler :show-add-participants
  (fn [db [action navigator]]
    (log/debug action)
    (nav-push navigator {:view-id :add-participants})
    (clear-new-participants db)))

(register-handler :add-new-participants
  (fn [db [action navigator]]
    (log/debug action)
    (let [identities (vec (new-participants-selection db))
          chat-id    (current-chat-id db)]
      (chat-add-participants chat-id identities)
      (nav-pop navigator)
      (doseq [ident identities]
        (api/group-add-participant chat-id ident))
      db)))

(register-handler :show-group-new
  (fn [db [action navigator]]
    (log/debug action)
    (nav-push navigator {:view-id :new-group})
    (clear-new-group db)))

(register-handler :select-for-new-group
  (fn [db [action identity add?]]
    (log/debug action identity add?)
    (update-new-group-selection db identity add?)))

(register-handler :create-new-group
  (fn [db [action group-name navigator]]
    (log/debug action)
    (let [identities (vec (new-group-selection db))
          group-id   (api/start-group-chat identities group-name)
          db         (create-chat db group-id identities true group-name)]
      (dispatch [:show-chat group-id navigator :replace])
      db)))

(register-handler :group-chat-invite-received
  (fn [db [action from group-id identities group-name]]
    (log/debug action from group-id identities)
    (if (chat-exists? group-id)
      (re-join-group-chat db group-id identities group-name)
      (create-chat db group-id identities true group-name))))

(register-handler :navigate-to
  (fn [db [_ view-id]]
    (-> db
        (assoc :view-id view-id)
        (update :navigation-stack conj view-id))))

(register-handler :navigate-back
  (fn [{:keys [navigation-stack] :as db} _]
    (log/debug :navigate-back)
    (if (>= 1 (count navigation-stack))
      db
      (let [[view-id :as navigation-stack'] (pop navigation-stack)]
        (-> db
            (assoc :view-id view-id)
            (assoc :navigation-stack navigation-stack'))))))

(register-handler :load-more-messages
  (fn [db _]
    db
    ;; TODO implement
    #_(let [chat-id      (get-in db [:chat :current-chat-id])
            messages     [:chats chat-id :messages]
            new-messages (gen-messages 10)]
        (update-in db messages concat new-messages))))

(defn load-messages!
  [db _]
  db
  (->> (current-chat-id db)
       get-messages
       (assoc db :messages)))

(defn init-chat
  [{:keys [messages] :as db} _]
  (let [id (current-chat-id db)]
    (assoc-in db [:chats id :messages] messages)))

(register-handler :init-chat
  (-> load-messages!
      ((enrich init-chat))
      debug))

(defn initialize-chats
  [{:keys [loaded-chats] :as db} _]
  (let [chats (->> loaded-chats
                   (map (fn [{:keys [chat-id] :as chat}]
                          [chat-id chat]))
                   (into {}))]
    (-> db
        (assoc :chats chats)
        (dissoc :loaded-chats))))

(defn load-chats!
  [db _]
  (assoc db :loaded-chats (chats/chats-list)))

(register-handler :initialize-chats
  ((enrich initialize-chats) load-chats!))

(defn safe-trim [s]
  (when (string? s)
    (str/trim s)))

(register-handler :cancel-command
  (fn [{:keys [current-chat-id] :as db} _]
    (-> db
        (assoc-in [:chats current-chat-id :command-input] {})
        (update-in [:chats current-chat-id :input-text] safe-trim))))

(register-handler :save-password
  (fn [db [_ password]]
    (sign-up-service/save-password password)
    (assoc db :password-saved true)))
