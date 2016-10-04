(ns status-im.group-settings.handlers
  (:require [re-frame.core :refer [debug dispatch after enrich]]
            [status-im.utils.handlers :refer [register-handler]]
            [status-im.chat.handlers :refer [delete-messages!]]
            [status-im.protocol.core :as protocol]
            [status-im.utils.random :as random]
            [status-im.data-store.contacts :as contacts]
            [status-im.data-store.messages :as messages]
            [status-im.data-store.chats :as chats]
            [status-im.constants :refer [text-content-type]]
            [status-im.utils.handlers :as u]
            [status-im.navigation.handlers :as nav]))

(defmethod nav/preload-data! :group-settings
  [db _]
  (assoc db :selected-participants #{}))

(defn save-property!
  [current-chat-id property-name value]
  (chats/save-property current-chat-id property-name value))

(defn save-chat-property!
  [db-name property-name]
  (fn [{:keys [current-chat-id] :as db} _]
    (let [property (db-name db)]
      (save-property! current-chat-id property-name property))))

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
  (after (fn [{:keys [current-chat-id]} [_ color]]
           (save-property! current-chat-id :color (name color))))
  (fn [{:keys [current-chat-id] :as db} [_ color]]
    (assoc-in db [:chats current-chat-id :color] color)))

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

(defn remove-members-from-chat!
  [{:keys [current-chat-id selected-participants] :as db} _]
  (chats/remove-contacts current-chat-id selected-participants))

(defn notify-about-removing!
  [{:keys [current-chat-id selected-participants]} _]
  (doseq [participant selected-participants]
    ;;todo implement
    ))

(defn system-message [message-id content]
  {:from         "system"
   :message-id   message-id
   :content      content
   :content-type text-content-type})

(defn removed-participant-message [chat-id identity]
  (let [contact-name (:name (contacts/get-by-id identity))]
    (->> (str "You've removed " (or contact-name identity))
         (system-message (random/id))
         (messages/save chat-id))))

(defn create-removing-messages!
  [{:keys [current-chat-id selected-participants]} _]
  (doseq [participant selected-participants]
    (removed-participant-message current-chat-id participant)))

(defn deselect-members [db _]
  (assoc db :selected-participants #{}))

(register-handler :remove-participants
  ;; todo check if user have rights to add/remove participants
  ;; todo order of operations tbd
  (-> remove-members
      ;; todo shouldn't this be done only after receiving of the "ack message"
      ;; about the api call that removes participants from the group?
      ((after remove-members-from-chat!))
      ;; todo uncomment
      ;((after notify-about-removing!))
      ((after create-removing-messages!))
      ((enrich deselect-members))
      debug))

(defn add-memebers
  [{:keys [current-chat-id selected-participants] :as db} _]
  (let [new-identities (map #(hash-map :identity %) selected-participants)]
    (update db [:chats current-chat-id :contacts] concat new-identities)))

(defn add-members-to-chat!
  [{:keys [current-chat-id selected-participants]} _]
  (chats/add-contacts current-chat-id selected-participants))

(defn notify-about-new-members!
  [{:keys [current-chat-id selected-participants
           current-public-key chats web3]} _]
  (let [{:keys [public-key private-key name contacts]} (chats current-chat-id)
        identities (map :identity contacts)
        keypair {:public  public-key
                 :private private-key}]
    (protocol/invite-to-group!
      {:web3       web3
       :group      {:id       current-chat-id
                    :name     name
                    :contacts (conj identities current-public-key)
                    :admin    current-public-key
                    :keypair  keypair}
       :identities selected-participants
       :message    {:from       current-public-key
                    :message-id (random/id)}})
    (doseq [identity selected-participants]
      (protocol/add-to-group! {:web3     web3
                               :group-id current-chat-id
                               :identity identity
                               :keypair  keypair
                               :message  {:from       current-public-key
                                          :message-id (random/id)}}))))

(register-handler :add-new-participants
  ;; todo order of operations tbd
  (-> add-memebers
      ((after add-members-to-chat!))
      ((after notify-about-new-members!))
      ((enrich deselect-members))))
