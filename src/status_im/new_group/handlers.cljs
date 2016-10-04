(ns status-im.new-group.handlers
  (:require [status-im.protocol.core :as protocol]
            [re-frame.core :refer [after dispatch debug enrich]]
            [status-im.utils.handlers :refer [register-handler]]
            [status-im.components.styles :refer [default-chat-color]]
            [status-im.data-store.chats :as chats]
            [clojure.string :as s]
            [status-im.utils.handlers :as u]
            [status-im.utils.random :as random]
            [taoensso.timbre :refer-macros [debug]]))

(defn deselect-contact
  [db [_ id]]
  (update db :selected-contacts disj id))

(register-handler :deselect-contact deselect-contact)

(defn select-contact
  [db [_ id]]
  (update db :selected-contacts conj id))

(register-handler :select-contact select-contact)

(defn group-name-from-contacts
  [{:keys [contacts selected-contacts username]}]
  (->> (select-keys contacts selected-contacts)
       vals
       (map :name)
       (cons username)
       (s/join ", ")))

(defn prepare-chat
  [{:keys [selected-contacts] :as db} [_ group-name]]
  (let [contacts (mapv #(hash-map :identity %) selected-contacts)
        chat-name (if-not (s/blank? group-name)
                    group-name
                    (group-name-from-contacts db))
        {:keys [public private]} (protocol/new-keypair!)]
    (assoc db :new-chat {:chat-id     (random/id)
                         :public-key  public
                         :private-key private
                         :name        chat-name
                         :color       default-chat-color
                         :group-chat  true
                         :is-active   true
                         :timestamp   (.getTime (js/Date.))
                         :contacts    contacts})))

(defn add-chat
  [{:keys [new-chat] :as db} _]
  (-> db
      (assoc-in [:chats (:chat-id new-chat)] new-chat)
      (assoc :selected-contacts #{})))

(defn create-chat!
  [{:keys [new-chat]} _]
  (chats/save new-chat))

(defn show-chat!
  [{:keys [new-chat]} _]
  (dispatch [:navigation-replace :chat (:chat-id new-chat)]))

(defn start-listen-group!
  [{:keys [new-chat web3 current-public-key]}]
  (let [{:keys [chat-id public-key private-key contacts name]} new-chat
        identities (mapv :identity contacts)]
    (protocol/invite-to-group!
      {:web3       web3
       :group      {:id       chat-id
                    :name     name
                    :contacts (conj identities current-public-key)
                    :admin    current-public-key
                    :keypair  {:public  public-key
                               :private private-key}}
       :identities identities
       :message    {:from       current-public-key
                    :message-id (random/id)}})
    (protocol/start-watching-group!
      {:web3     web3
       :group-id chat-id
       :identity current-public-key
       :keypair  {:public  public-key
                  :private private-key}
       :callback #(dispatch [:incoming-message %1 %2])})))

(register-handler :create-new-group
  (-> prepare-chat
      ((enrich add-chat))
      ((after create-chat!))
      ((after show-chat!))
      ((after start-listen-group!))))

(register-handler :group-chat-invite-received
  (u/side-effect!
    (fn [{:keys [current-public-key web3] :as db}
         [_ {{:keys [group-id group-name contacts keypair timestamp] :as payload} :payload}]]
      (let [{:keys [private public]} keypair]
        (let [removed-at (chats/removed-at group-id)
              is-active (chats/is-active? group-id)
              contacts' (keep (fn [ident]
                                (when (not= ident current-public-key)
                                  {:identity ident})) contacts)
              chat {:name        group-name
                    :group-chat  true
                    :public-key  public
                    :private-key private
                    :contacts    contacts'}]
          (when (or (not (chats/exists? group-id))
                    is-active
                    (> timestamp removed-at))
            (dispatch [:add-chat group-id (assoc chat :is-active true
                                                      :timestamp timestamp)])
            (when-not is-active
              (protocol/start-watching-group!
                {:web3     web3
                 :group-id group-id
                 :identity current-public-key
                 :keypair  keypair
                 :callback #(dispatch [:incoming-message %1 %2])}))))))))
