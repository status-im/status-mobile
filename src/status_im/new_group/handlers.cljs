(ns status-im.new-group.handlers
  (:require [status-im.protocol.api :as api]
            [re-frame.core :refer [after dispatch debug enrich]]
            [status-im.utils.handlers :refer [register-handler]]
            [status-im.components.styles :refer [default-chat-color]]
            [status-im.models.chats :as chats]
            [clojure.string :as s]
            [status-im.utils.handlers :as u]))

(defn deselect-contact
  [db [_ id]]
  (update db :selected-contacts disj id))

(register-handler :deselect-contact deselect-contact)

(defn select-contact
  [db [_ id]]
  (update db :selected-contacts conj id))

(register-handler :select-contact select-contact)

(defn start-group-chat!
  [{:keys [selected-contacts] :as db} [_ group-name]]
  (let [group-id (api/start-group-chat selected-contacts group-name)]
    (assoc db :new-group-id group-id)))

(defn group-name-from-contacts
  [{:keys [contacts selected-contacts username]}]
  (->> (select-keys contacts selected-contacts)
       vals
       (map :name)
       (cons username)
       (s/join ", ")))

(defn prepare-chat
  [{:keys [selected-contacts new-group-id] :as db} [_ group-name]]
  (let [contacts (mapv #(hash-map :identity %) selected-contacts)
        chat-name (if-not (s/blank? group-name)
                    group-name
                    (group-name-from-contacts db))]
    (assoc db :new-chat {:chat-id        new-group-id
                         :name           chat-name
                         :color          default-chat-color
                         :group-chat     true
                         :is-active      true
                         :timestamp      (.getTime (js/Date.))
                         :contacts       contacts
                         :same-author    false
                         :same-direction false})))

(defn add-chat
  [{:keys [new-chat] :as db} _]
  (-> db
      (assoc-in [:chats (:chat-id new-chat)] new-chat)
      (assoc :selected-contacts #{})))

(defn create-chat!
  [{:keys [new-chat]} _]
  (chats/create-chat new-chat))

(defn show-chat!
  [{:keys [new-group-id]} _]
  (dispatch [:navigation-replace :chat new-group-id]))

(defn enable-creat-buttion
  [db _]
  (assoc db :disable-group-creation false))

(register-handler :create-new-group
  (-> start-group-chat!
      ((enrich prepare-chat))
      ((enrich add-chat))
      ((after create-chat!))
      ((after show-chat!))
      ((enrich enable-creat-buttion))))

(defn disable-creat-button
  [db _]
  (assoc db :disable-group-creation true))

(defn dispatch-create-group
  [_ [_ group-name]]
  (dispatch [:create-new-group group-name]))

(register-handler :init-group-creation
  (after dispatch-create-group)
  disable-creat-button)

; todo rewrite
(register-handler :group-chat-invite-received
  (u/side-effect!
    (fn [{:keys [current-public-key] :as db}
         [action from group-id identities group-name]]
      (if (chats/chat-exists? group-id)
        (chats/re-join-group-chat db group-id identities group-name)
        (let [contacts (keep (fn [ident]
                               (when (not= ident current-public-key)
                                 {:identity ident})) identities)]
          (dispatch [:add-chat group-id {:name       group-name
                                         :group-chat true
                                         :contacts contacts}]))))))
