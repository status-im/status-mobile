(ns status-im.data-store.realm.chats
  (:require [goog.object :as object]
            [status-im.data-store.realm.core :as realm]
            [status-im.data-store.realm.messages :as messages]
            [status-im.utils.datetime :as datetime]
            [taoensso.timbre :as log])
  (:refer-clojure :exclude [exists?]))

(defn- normalize-chat [{:keys [chat-id] :as chat}]
  (let [last-clock-value (messages/get-last-clock-value chat-id)] 
    (assoc chat :last-clock-value  (or last-clock-value 0))))

(defn get-all
  []
  (map normalize-chat
       (-> @realm/account-realm
           (realm/get-all :chat)
           (realm/sorted :timestamp :desc)
           (realm/all-clj :chat))))

(defn- get-by-id-obj
  [chat-id]
  (realm/single (realm/get-by-field @realm/account-realm :chat :chat-id chat-id)))

(defn get-by-id
  [chat-id]
  (-> @realm/account-realm
      (realm/get-by-field :chat :chat-id chat-id)
      (realm/single-clj :chat)
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

(defn add-contacts
  [chat-id identities]
  (let [chat     (get-by-id-obj chat-id)
        contacts (object/get chat "contacts")]
    (realm/write @realm/account-realm
                 #(aset chat "contacts"
                        (clj->js (into #{} (concat identities
                                                   (realm/list->clj contacts))))))))

(defn remove-contacts
  [chat-id identities]
  (let [chat     (get-by-id-obj chat-id)
        contacts (object/get chat "contacts")]
    (realm/write @realm/account-realm
                 #(aset chat "contacts"
                        (clj->js (remove (into #{} identities)
                                         (realm/list->clj contacts)))))))

(defn save-property
  [chat-id property-name value]
  (realm/write @realm/account-realm
               (fn []
                 (-> @realm/account-realm
                     (realm/get-by-field :chat :chat-id chat-id)
                     realm/single
                     (aset (name property-name) value)))))

(defn get-property
  [chat-id property]
  (when-let [chat (realm/single (realm/get-by-field @realm/account-realm :chat :chat-id chat-id))]
    (object/get chat (name property))))
