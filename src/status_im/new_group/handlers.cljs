(ns status-im.new-group.handlers
  (:require [status-im.protocol.core :as protocol]
            [re-frame.core :refer [after dispatch debug enrich]]
            [status-im.utils.handlers :refer [register-handler] :as u]
            [status-im.components.styles :refer [default-chat-color]]
            [status-im.data-store.chats :as chats]
            [status-im.data-store.contact-groups :as groups]
            [clojure.string :as s]
            [status-im.i18n :refer [label]]
            [status-im.utils.handlers :as u]
            [status-im.utils.random :as random]
            [taoensso.timbre :refer-macros [debug]]
            [taoensso.timbre :as log]
            [status-im.navigation.handlers :as nav]))

(defn clear-toolbar-search [db]
  (-> db
    (assoc-in [:toolbar-search :show] nil)
    (assoc-in [:toolbar-search :text] "")))

(defmethod nav/preload-data! :add-contacts-toggle-list
  [db _]
  (->
    (assoc db :selected-contacts #{})
    (clear-toolbar-search)))


(defmethod nav/preload-data! :add-participants-toggle-list
  [db _]
  (->
    (assoc db :selected-participants #{})
    (clear-toolbar-search)))

(defn deselect-contact
  [db [_ id]]
  (update db :selected-contacts disj id))

(register-handler :deselect-contact deselect-contact)

(defn select-contact
  [db [_ id]]
  (update db :selected-contacts conj id))

(register-handler :select-contact select-contact)

(defn group-name-from-contacts
  [{:keys [selected-contacts username]
    :contacts/keys [contacts]}]
  (->> (select-keys contacts selected-contacts)
       vals
       (map :name)
       (cons username)
       (s/join ", ")))

(defn prepare-chat
  [{:keys [selected-contacts current-public-key] :as db} [_ group-name]]
  (let [contacts  (mapv #(hash-map :identity %) selected-contacts)
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
                         :group-admin current-public-key
                         :is-active   true
                         :timestamp   (random/timestamp)
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
  (dispatch [:navigate-to-clean :chat-list])
  (dispatch [:navigate-to :chat (:chat-id new-chat)]))

(defn start-listen-group!
  [{:keys [new-chat web3 current-public-key]} _]
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

(register-handler :create-new-group-chat
  (u/handlers->
    prepare-chat
    add-chat
    create-chat!
    show-chat!
    start-listen-group!))

(register-handler :create-new-public-group
  (after (fn [_ [_ topic]]
           (dispatch [:navigate-to-clean :chat-list])
           (dispatch [:navigate-to :chat topic])))
  (u/side-effect!
    (fn [db [_ topic]]
      (let [exists? (boolean (get-in db [:chats topic]))
            group   {:chat-id     topic
                     :name        topic
                     :color       default-chat-color
                     :group-chat  true
                     :public?     true
                     :is-active   true
                     :timestamp   (random/timestamp)}]
        (when-not exists?
          (dispatch [::add-public-group group])
          (dispatch [::save-public-group group])
          (dispatch [::start-watching-group topic]))))))

(register-handler ::add-public-group
  (fn [db [_ {:keys [chat-id] :as group}]]
    (assoc-in db [:chats chat-id] group)))

(register-handler ::save-public-group
  (u/side-effect!
    (fn [_ [_ group]]
      (chats/save group))))

(register-handler ::start-watching-group
  (u/side-effect!
    (fn [{:keys [web3 current-public-key]} [_ topic]]
      (protocol/start-watching-group!
        {:web3     web3
         :group-id topic
         :identity current-public-key
         :callback #(dispatch [:incoming-message %1 %2])}))))

(register-handler :group-chat-invite-received
  (u/side-effect!
    (fn [{:keys [current-public-key web3]}
         [_ {:keys                                                    [from]
             {:keys [group-id group-name contacts keypair timestamp]} :payload}]]
      (let [{:keys [private public]} keypair]
        (let [contacts' (keep (fn [ident]
                                (when (not= ident current-public-key)
                                  {:identity ident})) contacts)
              chat      {:chat-id     group-id
                         :name        group-name
                         :group-chat  true
                         :group-admin from
                         :public-key  public
                         :private-key private
                         :contacts    contacts'
                         :added-to-at timestamp
                         :timestamp   timestamp
                         :is-active   true}

              exists?   (chats/exists? group-id)]
          (when (or (not exists?) (chats/new-update? timestamp group-id))
            (if exists?
              (dispatch [:update-chat! chat])
              (dispatch [:add-chat group-id chat]))
            (protocol/start-watching-group!
              {:web3     web3
               :group-id group-id
               :identity current-public-key
               :keypair  keypair
               :callback #(dispatch [:incoming-message %1 %2])})))))))

(defn prepare-group
  [{:keys [selected-contacts contact-groups] :as db} [_ group-name]]
  (let [contacts (mapv #(hash-map :identity %) selected-contacts)]
    (assoc db :new-group {:group-id    (random/id)
                          :name        group-name
                          :order       (count contact-groups)
                          :timestamp   (random/timestamp)
                          :contacts    contacts})))

(defn add-group
  [{:keys [new-group] :as db} _]
  (update db :contact-groups merge {(:group-id new-group) new-group}))

(defn update-group
  [{:keys [new-group] :as db} _]
  (update db :contact-groups merge {(:group-id new-group) new-group}))

(defn save-group!
  [{:keys [new-group]} _]
  (groups/save new-group))

(defn show-contact-list!
  [_ _]
  (dispatch [:navigate-to-clean :contact-list]))

(register-handler :create-new-group
  (u/handlers->
    prepare-group
    add-group
    save-group!
    show-contact-list!))

(defn update-new-group
  [db [_ new-group]]
  (assoc db :new-group new-group))

(register-handler :update-group
  (u/handlers->
    update-new-group
    update-group
    save-group!))

(defn save-groups! [{:keys [new-groups]} _]
  (groups/save-all new-groups))

(defn update-pending-status [old-groups {:keys [group-id pending?] :as group}]
  (let [{old-pending :pending?
         :as         old-group} (get old-groups group-id)
        pending?' (if old-pending (and old-pending pending?) pending?)]
    (assoc group :pending? (boolean pending?'))))

(defn add-new-groups
  [{:keys [contact-groups] :as db} [_ new-groups]]
  (let [identities  (set (keys contact-groups))
        old-groups-count (count identities)
        new-groups' (->> new-groups
                           (map #(update-pending-status contact-groups %))
                           (remove #(identities (:group-id %)))
                           (map #(vector (:group-id %2) (assoc %2 :order %1)) (iterate inc old-groups-count))
                           (into {}))]
    (-> db
        (update :contact-groups merge new-groups')
        (assoc :new-groups (into [] (vals new-groups'))))))

(register-handler :add-groups
  (after save-groups!)
  add-new-groups)

(defn load-groups! [db _]
  (let [groups (->> (groups/get-all)
                    (map (fn [{:keys [group-id] :as group}]
                           [group-id group]))
                    (into {}))]
    (assoc db :contact-groups groups)))

(register-handler :load-groups load-groups!)

(defmethod nav/preload-data! :new-public-group
  [db]
  (dissoc db :public-group-topic))

(defn move-item [v from to]
  (if (< from to)
    (concat (subvec v 0 from)
            (subvec v (inc from) (inc to))
            [(v from)]
            (subvec v (inc to)))
    (concat (subvec v 0 to)
            [(v from)]
            (subvec v to from)
            (subvec v (inc from)))))

(register-handler :change-group-order
  (fn [{:keys [groups-order] :as db} [_ from to]]
    (if (>= to 0)
      (assoc db :groups-order (move-item (vec groups-order) from to))
      db)))

(register-handler :update-groups
  (after save-groups!)
  (fn [db [_ new-groups]]
    (-> db
        (update :contact-groups merge (map #(vector (:group-id %) %) new-groups))
        (assoc :new-groups new-groups))))

(register-handler :save-group-order
  (u/side-effect!
    (fn [{:keys [groups-order contact-groups] :as db} _]
      (let [new-groups (mapv #(assoc (contact-groups (second %)) :order (first %))
                              (map-indexed vector (reverse groups-order)))]
        (dispatch [:update-groups new-groups])
        (dispatch [:navigate-to-clean :contact-list])))))

(defn save-property!
  [contact-group-id property-name value]
  (groups/save-property contact-group-id property-name value))

(defn save-group-property!
  [db-name property-name]
  (fn [{:keys [contact-group-id] :as db} _]
    (let [property (db-name db)]
      (save-property! contact-group-id property-name property))))

(defn update-group-property
  [db-name property-name]
  (fn [{:keys [contact-group-id] :as db} _]
    (let [property (db-name db)]
      (assoc-in db [:contact-groups contact-group-id property-name] property))))

(register-handler :set-group-name
  (after (save-group-property! :new-chat-name :name))
  (update-group-property :new-chat-name :name))

(defn add-selected-contacts-to-group
  [{:keys [selected-contacts contact-groups contact-group-id] :as db} _]
  (let [new-identities (mapv #(hash-map :identity %) selected-contacts)]
    (update-in db [:contact-groups contact-group-id :contacts] #(into [] (set (concat % new-identities))))))

(defn add-selected-contacts-to-group!
  [{:keys [contact-group-id selected-contacts]} _]
  (groups/add-contacts contact-group-id selected-contacts))

(register-handler :add-selected-contacts-to-group
  (after add-selected-contacts-to-group!)
  add-selected-contacts-to-group)

(defn add-contacts-to-group
  [db [_ group-id contacts]]
  (let [new-identities (mapv #(hash-map :identity %) contacts)]
    (if (get-in db [:contact-groups group-id])
      (update-in db [:contact-groups group-id :contacts] #(into [] (set (concat % new-identities))))
      db)))

(defn add-contacts-to-group!
  [db [_ group-id contacts]]
  (when (get-in db [:contact-groups group-id])
    (groups/add-contacts group-id contacts)))

(register-handler :add-contacts-to-group
  (after add-contacts-to-group!)
  add-contacts-to-group)

(defn delete-group []
  (fn [{:keys [contact-group-id] :as db} _]
    (assoc-in db [:contact-groups contact-group-id :pending?] true)))

(defn delete-group! []
  (fn [{:keys [contact-group-id]} _]
    (save-property! contact-group-id :pending? true)))

(register-handler :delete-group
  (after (delete-group!))
  (delete-group))

(defn deselect-participant
  [db [_ id]]
  (update db :selected-participants disj id))

(register-handler :deselect-participant deselect-participant)

(defn select-participant
  [db [_ id]]
  (update db :selected-participants conj id))

(register-handler :select-participant select-participant)



