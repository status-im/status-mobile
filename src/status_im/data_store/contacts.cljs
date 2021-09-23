(ns status-im.data-store.contacts
  (:require [clojure.set :as clojure.set]
            [status-im.ethereum.json-rpc :as json-rpc]
            [status-im.utils.fx :as fx]
            [taoensso.timbre :as log]))

(defn <-rpc [contact]
  (clojure.set/rename-keys contact {:id :public-key
                                    :ensVerifiedAt :ens-verified-at
                                    :ensVerified :ens-verified
                                    :ensVerificationRetries :ens-verification-retries
                                    :lastENSClockValue :last-ens-clock-value
                                    :lastUpdated :last-updated
                                    :localNickname :nickname}))

(defn ->rpc [contact]
  (clojure.set/rename-keys contact {:public-key :id
                                    :ens-verified :ensVerified
                                    :ens-verified-at :ensVerifiedAt
                                    :last-ens-clock-value :lastENSClockValue
                                    :ens-verification-retries :ensVerificationRetries
                                    :last-updated :lastUpdated
                                    :nickname :localNickname}))

(fx/defn fetch-contacts-rpc
  [_ on-success]
  {::json-rpc/call [{:method (json-rpc/call-ext-method "contacts")
                     :params []
                     :on-success #(on-success (map <-rpc %))
                     :on-failure #(log/error "failed to fetch contacts" %)}]})

(fx/defn save-contact
  [_ {:keys [public-key] :as contact} on-success]
  {::json-rpc/call [{:method (json-rpc/call-ext-method "saveContact")
                     :params [(->rpc contact)]
                     :on-success #(do
                                    (log/debug "saved contact" public-key "successfuly")
                                    (when on-success
                                      (on-success)))
                     :on-failure #(log/error "failed to save contact" public-key %)}]})

(fx/defn block [_ contact on-success]
  {::json-rpc/call [{:method (json-rpc/call-ext-method "blockContact")
                     :params [(->rpc contact)]
                     :on-success on-success
                     :on-failure #(log/error "failed to block contact" % contact)}]})
