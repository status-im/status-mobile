(ns status-im.data-store.contacts
  (:require [re-frame.core :as re-frame]
            [status-im.utils.fx :as fx]
            [status-im.data-store.chats :as data-store.chats]
            [status-im.ethereum.json-rpc :as json-rpc]
            [taoensso.timbre :as log]
            [status-im.data-store.realm.core :as core]))

(defn deserialize-device-info [contact]
  (update contact :deviceInfo (fn [device-info]
                                (reduce (fn [acc info]
                                          (assoc acc
                                                 (:installationId info)
                                                 (clojure.set/rename-keys info {:fcmToken :fcm-token :installationId :id})))
                                        {}
                                        device-info))))

(defn serialize-device-info [contact]
  (update contact :device-info (fn [device-info]
                                 (map
                                  #(clojure.set/rename-keys % {:fcm-token :fcmToken :id :installationId})
                                  (vals device-info)))))

(defn <-rpc [contact]
  (-> contact
      deserialize-device-info
      (update :tributeToTalk core/deserialize)
      (update :systemTags
              #(reduce (fn [acc s]
                         (conj acc (keyword (subs s 1))))
                       #{}
                       %)) (clojure.set/rename-keys {:id :public-key
                                                     :photoPath :photo-path
                                                     :deviceInfo :device-info
                                                     :tributeToTalk :tribute-to-talk
                                                     :systemTags :system-tags
                                                     :lastUpdated :last-updated})))

(defn ->rpc [contact]
  (-> contact
      serialize-device-info
      (update :tribute-to-talk core/serialize)
      (update :system-tags #(mapv str %))
      (clojure.set/rename-keys {:public-key :id
                                :photo-path :photoPath
                                :device-info :deviceInfo
                                :tribute-to-talk :tributeToTalk
                                :system-tags :systemTags
                                :last-updated :lastUpdated})))

(defn save-contact-rpc [{:keys [public-key] :as contact}]
  (json-rpc/call {:method "shhext_saveContact"
                  :params [(->rpc contact)]
                  :on-success #(log/debug "saved contact" public-key "successfuly")
                  :on-failure #(log/error "failed to save contact" public-key %)}))

(defn fetch-contacts-rpc [on-success]
  (json-rpc/call {:method "shhext_contacts"
                  :params []
                  :on-success #(on-success (map <-rpc %))
                  :on-failure #(log/error "failed to fetch contacts" %)}))

(defn save-contact-tx
  "Returns tx function for saving contact"
  [{:keys [public-key] :as contact}]
  (save-contact-rpc contact))

(defn- get-messages-by-messages-ids
  [message-ids]
  (when (not-empty message-ids)
    (-> @core/account-realm
        (.objects "message")
        (.filtered (str "(" (core/in-query "message-id" message-ids) ")")))))

(defn block-user-tx
  "Returns tx function for deleting user messages"
  [{:keys [public-key] :as contact} messages-ids]
  (fn [realm]
    (data-store.chats/delete-chat-rpc public-key data-store.chats/one-to-one-chat-type)
    (save-contact-rpc contact)
    (when-let [user-messages
               (get-messages-by-messages-ids messages-ids)]
      (core/delete realm user-messages))))

