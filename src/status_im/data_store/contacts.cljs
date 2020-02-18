(ns status-im.data-store.contacts
  (:require [re-frame.core :as re-frame]
            [status-im.utils.fx :as fx]
            [status-im.utils.config :as config]
            [status-im.data-store.chats :as data-store.chats]
            [status-im.ethereum.json-rpc :as json-rpc]
            [taoensso.timbre :as log]
            [status-im.utils.types :as types]))

(defn deserialize-tribute-to-talk [t]
  (if (seq t)
    (types/deserialize t)
    {}))

(defn <-rpc [contact]
  (-> contact
      (update :tributeToTalk deserialize-tribute-to-talk)
      (update :systemTags
              #(reduce (fn [acc s]
                         (conj acc (keyword (subs s 1))))
                       #{}
                       %))
      (clojure.set/rename-keys {:id :public-key
                                :photoPath :photo-path
                                :tributeToTalk :tribute-to-talk
                                :ensVerifiedAt :ens-verified-at
                                :ensVerified :ens-verified
                                :ensVerificationRetries :ens-verification-retries
                                :lastENSClockValue :last-ens-clock-value
                                :systemTags :system-tags
                                :lastUpdated :last-updated})))

(defn ->rpc [contact]
  (-> contact
      (update :tribute-to-talk types/serialize)
      (update :system-tags #(mapv str %))
      (clojure.set/rename-keys {:public-key :id
                                :ens-verified :ensVerified
                                :ens-verified-at :ensVerifiedAt
                                :last-ens-clock-value :lastENSClockValue
                                :ens-verification-retries :ensVerificationRetries
                                :photo-path :photoPath
                                :tribute-to-talk :tributeToTalk
                                :system-tags :systemTags
                                :last-updated :lastUpdated})))

(fx/defn fetch-contacts-rpc
  [cofx on-success]
  {::json-rpc/call [{:method (json-rpc/call-ext-method "contacts")
                     :params []
                     :on-success #(on-success (map <-rpc %))
                     :on-failure #(log/error "failed to fetch contacts" %)}]})

(fx/defn save-contact
  [cofx {:keys [public-key] :as contact}]
  {::json-rpc/call [{:method (json-rpc/call-ext-method "saveContact")
                     :params [(->rpc contact)]
                     :on-success #(log/debug "saved contact" public-key "successfuly")
                     :on-failure #(log/error "failed to save contact" public-key %)}]})

(fx/defn block [cofx contact on-success]
  {::json-rpc/call [{:method (json-rpc/call-ext-method "blockContact")
                     :params [(->rpc contact)]
                     :on-success on-success
                     :on-failure #(log/error "failed to block contact" % contact)}]})
