(ns status-im.data-store.realm.chats
  (:require [status-im.data-store.realm.core :as realm]
            [status-im.utils.random :refer [timestamp]]
            [taoensso.timbre :as log])
  (:refer-clojure :exclude [exists?]))

(defn get-all
  []
  (-> @realm/account-realm
      (realm/get-all :chat)
      (realm/sorted :timestamp :desc)))

(defn get-all-as-list
  []
  (realm/realm-collection->list (get-all)))

(defn get-all-active
  []
  (-> (realm/get-by-field @realm/account-realm :chat :is-active true)
      (realm/sorted :timestamp :desc)
      realm/realm-collection->list))

(defn- groups
  [active?]
  (realm/filtered (get-all)
                  (str "group-chat = true && is-active = "
                       (if active? "true" "false"))))

(defn get-active-group-chats
  []
  (map
    (fn [{:keys [chat-id public-key private-key]}]
      {:chat-id chat-id
       :keypair {:private private-key
                 :public  public-key}})
    (realm/realm-collection->list (groups true))))

(defn get-by-id
  [chat-id]
  (-> @realm/account-realm
      (realm/get-one-by-field-clj :chat :chat-id chat-id)
      (realm/list->array :contacts)))

(defn save
  [chat update?]
  (realm/save @realm/account-realm :chat chat update?))

(defn exists?
  [chat-id]
  (realm/exists? @realm/account-realm :chat {:chat-id chat-id}))

(defn delete
  [chat-id]
  (when-let [chat (realm/get-by-field @realm/account-realm :chat :chat-id chat-id)]
    (realm/delete @realm/account-realm chat)))

(defn set-inactive
  [chat-id]
  (when-let [chat (get-by-id chat-id)]
    (realm/write @realm/account-realm
                 (fn []
                   (doto chat
                     (aset "is-active" false)
                     (aset "removed-at" timestamp))))))

(defn get-contacts
  [chat-id]
  (-> @realm/account-realm
      (realm/get-one-by-field :chat :chat-id chat-id)
      (aget "contacts")))

(defn has-contact?
  [chat-id identity]
  (let [contacts (get-contacts chat-id)
        contact (.find contacts (fn [object _ _]
                                  (= identity (aget object "identity"))))]
    (if contact true false)))

(defn- save-contacts
  [identities contacts added-at]
  (doseq [contact-identity identities]
    (if-let [contact (.find contacts (fn [object _ _]
                                       (= contact-identity (aget object "identity"))))]
      (doto contact
        (aset "is-in-chat" true)
        (aset "added-at" added-at))
      (.push contacts (clj->js {:identity contact-identity
                                :added-at added-at})))))

(defn add-contacts
  [chat-id identities]
  (let [contacts (get-contacts chat-id)
        added-at (timestamp)]
    (realm/write @realm/account-realm
                 #(save-contacts identities contacts added-at))))

(defn- delete-contacts
  [identities contacts]
  (doseq [contact-identity identities]
     (when-let [contact (.find contacts (fn [object _ _]
                                        (= contact-identity (aget object "identity"))))]
       (realm/delete @realm/account-realm contact))))

(defn remove-contacts
  [chat-id identities]
  (let [contacts (get-contacts chat-id)]
    (delete-contacts identities contacts)))

(defn save-property
  [chat-id property-name value]
  (realm/write @realm/account-realm
               (fn []
                 (-> @realm/account-realm
                     (realm/get-one-by-field :chat :chat-id chat-id)
                     (aset (name property-name) value)))))

(defn get-property
  [chat-id property]
  (when-let [chat (realm/get-one-by-field @realm/account-realm :chat :chat-id chat-id)]
    (aget chat (name property))))
