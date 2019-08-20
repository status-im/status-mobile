(ns status-im.data-store.contacts
  (:require [re-frame.core :as re-frame]
            [status-im.utils.fx :as fx]
            [status-im.data-store.chats :as data-store.chats]
            [status-im.ethereum.json-rpc :as json-rpc]
            [status-im.utils.types :as types]
            [taoensso.timbre :as log]))

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
      (update :tributeToTalk types/deserialize)
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
      (update :tribute-to-talk types/serialize)
      (update :system-tags #(mapv str %))
      (clojure.set/rename-keys {:public-key :id
                                :photo-path :photoPath
                                :device-info :deviceInfo
                                :tribute-to-talk :tributeToTalk
                                :system-tags :systemTags
                                :last-updated :lastUpdated})))

(fx/defn fetch-contacts-rpc
  [cofx on-success]
  {::json-rpc/call [{:method "shhext_contacts"
                     :params []
                     :on-success #(on-success (map <-rpc %))
                     :on-failure #(log/error "failed to fetch contacts" %)}]})

(fx/defn save-contact
  [cofx {:keys [public-key] :as contact}]
  {::json-rpc/call [{:method "shhext_saveContact"
                     :params [(->rpc contact)]
                     :on-success #(log/debug "saved contact" public-key "successfuly")
                     :on-failure #(log/error "failed to save contact" public-key %)}]})

(fx/defn block [cofx contact on-success]
  {::json-rpc/call [{:method "shhext_blockContact"
                     :params [(->rpc contact)]
                     :on-success on-success
                     :on-failure #(log/error "failed to block contact" % contact)}]})
