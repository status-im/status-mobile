(ns status-im.data-store.realm.chats
  (:require [goog.object :as object]
            [status-im.data-store.realm.core :as realm]
            [status-im.data-store.realm.messages :as messages]
            [status-im.utils.datetime :as datetime]
            [taoensso.timbre :as log])
  (:refer-clojure :exclude [exists?]))

(defn- normalize-chat [{:keys [chat-id] :as chat}]
  (let [last-message (messages/get-last-message chat-id)]
    (-> chat
        (realm/fix-map->vec :contacts)
        (assoc :last-clock-value (or (:clock-value last-message) 0)))))

(defn get-all-active
  []
  (map normalize-chat
       (-> (realm/get-by-field @realm/account-realm :chat :is-active true)
           (realm/sorted :timestamp :desc)
           realm/js-object->clj)))

(defn get-inactive-ids
  []
  (-> (realm/get-by-field @realm/account-realm :chat :is-active false)
      (.map (fn [chat _ _]
              (aget chat "chat-id")))
      realm/js-object->clj
      set))

(defn- groups
  [active?]
  (-> @realm/account-realm
      (realm/get-all :chat)
      (realm/sorted :timestamp :desc)
      (realm/filtered (str "group-chat = true && is-active = "
                           (if active? "true" "false")))))

(defn get-active-group-chats
  []
  (map (fn [{:keys [chat-id public-key private-key public?]}]
         (let [group {:group-id chat-id
                      :public?  public?}]
           (if (and public-key private-key)
             (assoc group :keypair {:private private-key
                                    :public  public-key})
             group)))
       (realm/js-object->clj (groups true))))

(defn- get-by-id-obj
  [chat-id]
  (realm/get-one-by-field @realm/account-realm :chat :chat-id chat-id))

(defn get-by-id
  [chat-id]
  (-> @realm/account-realm
      (realm/get-one-by-field-clj :chat :chat-id chat-id)
      normalize-chat))

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
  (when-let [chat (get-by-id-obj chat-id)]
    (realm/write @realm/account-realm
                 (fn []
                   (doto chat
                     (aset "is-active" false)
                     (aset "removed-at" (datetime/timestamp)))))))

(defn get-contacts
  [chat-id]
  (-> @realm/account-realm
      (realm/get-one-by-field :chat :chat-id chat-id)
      (object/get "contacts")))

(defn- save-contacts
  [identities contacts added-at]
  (doseq [contact-identity identities]
    (if-let [contact (.find contacts (fn [object _ _]
                                       (= contact-identity (object/get object "identity"))))]
      (doto contact
        (aset "is-in-chat" true)
        (aset "added-at" added-at))
      (.push contacts (clj->js {:identity contact-identity
                                :added-at added-at})))))

(defn add-contacts
  [chat-id identities]
  (let [contacts (get-contacts chat-id)
        added-at (datetime/timestamp)]
    (realm/write @realm/account-realm
                 #(save-contacts identities contacts added-at))))

(defn- delete-contacts
  [identities contacts]
  (doseq [contact-identity identities]
    (when-let [contact (.find contacts (fn [object _ _]
                                         (= contact-identity (object/get object "identity"))))]
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
    (object/get chat (name property))))
