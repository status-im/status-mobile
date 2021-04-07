(ns status-im.data-store.contacts
  (:require [clojure.set :as clojure.set]
            [status-im.ethereum.json-rpc :as json-rpc]
            [status-im.utils.fx :as fx]
            [taoensso.timbre :as log]))

(defn <-rpc [contact]
  (-> contact
      (update :systemTags
              #(reduce (fn [acc s]
                         (conj acc (keyword (subs s 1))))
                       #{}
                       %))
      (clojure.set/rename-keys {:id :public-key
                                :ensVerifiedAt :ens-verified-at
                                :ensVerified :ens-verified
                                :ensVerificationRetries :ens-verification-retries
                                :lastENSClockValue :last-ens-clock-value
                                :systemTags :system-tags
                                :lastUpdated :last-updated
                                :localNickname :nickname})))

(defn ->rpc [contact]
  (-> contact
      (update :system-tags #(mapv str %))
      (clojure.set/rename-keys {:public-key :id
                                :ens-verified :ensVerified
                                :ens-verified-at :ensVerifiedAt
                                :last-ens-clock-value :lastENSClockValue
                                :ens-verification-retries :ensVerificationRetries
                                :system-tags :systemTags
                                :last-updated :lastUpdated
                                :nickname :localNickname})))

(fx/defn fetch-contacts-rpc
  [cofx on-success]
  {::json-rpc/call [{:method (json-rpc/call-ext-method "contacts")
                     :params []
                     :on-success #(on-success (map <-rpc %))
                     :on-failure #(log/error "failed to fetch contacts" %)}]})

(fx/defn save-contact
  [cofx {:keys [public-key] :as contact} on-success]
  {::json-rpc/call [{:method (json-rpc/call-ext-method "saveContact")
                     :params [(->rpc contact)]
                     :on-success #(do
                                    (log/debug "saved contact" public-key "successfuly")
                                    (when on-success
                                      (on-success)))
                     :on-failure #(log/error "failed to save contact" public-key %)}]})

(fx/defn block [cofx contact on-success]
  {::json-rpc/call [{:method (json-rpc/call-ext-method "blockContact")
                     :params [(->rpc contact)]
                     :on-success on-success
                     :on-failure #(log/error "failed to block contact" % contact)}]})
