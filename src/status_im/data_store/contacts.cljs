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

(fx/defn fetch-contacts-rpc
  [_ on-success]
  {::json-rpc/call [{:method (json-rpc/call-ext-method "contacts")
                     :params []
                     :on-success #(on-success (map <-rpc %))
                     :on-failure #(log/error "failed to fetch contacts" %)}]})

(fx/defn add
  [_ public-key nickname ens-name on-success]
  {::json-rpc/call [{:method (json-rpc/call-ext-method "addContact")
                     :params [{:id public-key :nickname nickname :ensName ens-name}]
                     :js-response true
                     :on-success #(do
                                    (log/info "saved contact" public-key "successfuly")
                                    (when on-success
                                      (on-success %)))
                     :on-failure #(log/error "failed to add contact" public-key %)}]})

(fx/defn set-nickname
  [_ public-key nickname on-success]
  {::json-rpc/call [{:method (json-rpc/call-ext-method "setContactLocalNickname")
                     :params [{:id public-key :nickname nickname}]
                     :js-response true
                     :on-success #(do
                                    (log/debug "set contact nickname" public-key "successfuly" nickname)
                                    (when on-success
                                      (on-success %)))
                     :on-failure #(log/error "failed to set contact nickname " public-key nickname %)}]})

(fx/defn block [_ contact-id on-success]
  {::json-rpc/call [{:method (json-rpc/call-ext-method "blockContact")
                     :params [contact-id]
                     :js-response true
                     :on-success on-success
                     :on-failure #(log/error "failed to block contact" % contact-id)}]})

(fx/defn unblock [_ contact-id on-success]
  {::json-rpc/call [{:method (json-rpc/call-ext-method "unblockContact")
                     :params [contact-id]
                     :on-success on-success
                     :js-response true
                     :on-failure #(log/error "failed to unblock contact" % contact-id)}]})
