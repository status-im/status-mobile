(ns status-im.group-settings.handlers
  (:require [re-frame.core :refer [register-handler debug dispatch after
                                   enrich]]
            [status-im.persistence.realm :as r]
            [status-im.chat.handlers :refer [delete-messages!]]
            [status-im.protocol.api :as api]
            [status-im.utils.random :as random]
            [status-im.models.contacts :as contacts]
            [status-im.models.messages :as messages]
            [status-im.models.chats :as chats]
            [status-im.constants :refer [text-content-type]]
            [status-im.utils.handlers :as u]
            [status-im.navigation.handlers :as nav]))

(defmethod nav/preload-data! :group-settings
  [db _]
  (assoc db :selected-participants #{}))

(defn save-chat-property!
  [db-name property-name]
  (fn [{:keys [current-chat-id] :as db} _]
    (let [property (db-name db)]
      (r/write (fn []
                 (-> (r/get-by-field :chats :chat-id current-chat-id)
                     (r/single)
                     (aset (name property-name) property)))))))

(defn update-chat-property
  [db-name property-name]
  (fn [{:keys [current-chat-id] :as db} _]
    (let [property (db-name db)]
      (assoc-in db [:chats current-chat-id property-name] property))))

(defn prepare-chat-settings
  [{:keys [current-chat-id] :as db} _]
  (let [{:keys [name color]} (-> db
                                 (get-in [:chats current-chat-id])
                                 (select-keys [:name :color]))]
    (-> db
        (assoc :new-chat-name name
               :new-chat-color color
               :group-settings {}))))

(register-handler :show-group-settings
  (after (fn [_ _] (dispatch [:navigate-to :group-settings])))
  prepare-chat-settings)

(register-handler :set-chat-name
  (after (save-chat-property! :new-chat-name :name))
  (update-chat-property :new-chat-name :name))

(register-handler :set-chat-color
  (after (save-chat-property! :new-chat-color :color))
  (update-chat-property :new-chat-color :color))

(defn clear-messages
  [{:keys [current-chat-id] :as db} _]
  (assoc-in db [:chats current-chat-id :messages] '()))

(register-handler :clear-history
  (after delete-messages!)
  clear-messages)

(register-handler :group-settings
  (fn [db [_ k v]]
    (assoc-in db [:group-settings k] v)))

(defn remove-identities [collection identities]
  (remove #(identities (:identity %)) collection))

(defn remove-members
  [{:keys [current-chat-id selected-participants] :as db} _]
  (update-in db [:chats current-chat-id :contacts]
             remove-identities selected-participants))

(defn remove-members-from-realm!
  [{:keys [current-chat-id selected-participants] :as db} _]
  (let [chat (get-in db [:chats current-chat-id])]
    (r/write
      (fn []
        (r/create
          :chats
          (update chat :contacts remove-identities selected-participants)
          true)))))

(defn notify-about-removing!
  [{:keys [current-chat-id selected-participants]} _]
  (doseq [participant selected-participants]
    (api/group-remove-participant current-chat-id participant)))

(defn system-message [msg-id content]
  {:from         "system"
   :msg-id       msg-id
   :content      content
   :content-type text-content-type})

(defn removed-participant-msg [chat-id identity]
  (let [contact-name (:name (contacts/contact-by-identity identity))]
    (->> (str "You've removed " (or contact-name identity))
         (system-message (random/id))
         (messages/save-message chat-id))))

(defn create-removing-messages!
  [{:keys [current-chat-id selected-participants]} _]
  (doseq [participant selected-participants]
    (removed-participant-msg current-chat-id participant)))

(defn deselect-members [db _]
  (assoc db :selected-participants #{}))

(register-handler :remove-participants
  ;; todo check if user have rights to add/remove participants
  ;; todo order of operations tbd
  (-> remove-members
      ;; todo shouldn't this be done only after receiving of the "ack message"
      ;; about the api call that removes participants from the group?
      ((after remove-members-from-realm!))
      ;; todo uncomment
      ;((after notify-about-removing!))
      ((after create-removing-messages!))
      ((enrich deselect-members))
      debug))

(defn add-memebers
  [{:keys [current-chat-id selected-participants] :as db} _]
  (let [new-identities (map #(hash-map :identity %) selected-participants)]
    (update db [:chats current-chat-id :contacts] concat new-identities)))

(defn add-members-to-realm!
  [{:keys [current-chat-id selected-participants]} _]
  (chats/chat-add-participants current-chat-id selected-participants))

(defn notify-about-new-members!
  [{:keys [current-chat-id selected-participants]} _]
  (doseq [identity selected-participants]
    (api/group-add-participant current-chat-id identity)))

(register-handler :add-new-participants
  ;; todo order of operations tbd
  (-> add-memebers
      ((after add-members-to-realm!))
      ;; todo uncomment
      ;((after notify-about-new-members!))
      ((enrich deselect-members))))
