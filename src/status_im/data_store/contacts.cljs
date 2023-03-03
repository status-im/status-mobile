(ns status-im.data-store.contacts
  (:require [clojure.set :as set]
            [utils.re-frame :as rf]
            [taoensso.timbre :as log]))

(defn <-rpc
  [contact]
  (-> contact
      (set/rename-keys
       {:id                     :public-key
        :ensVerifiedAt          :ens-verified-at
        :compressedKey          :compressed-key
        :displayName            :display-name
        :ensVerified            :ens-verified
        :ensVerificationRetries :ens-verification-retries
        :hasAddedUs             :has-added-us
        :contactRequestState    :contact-request-state
        :lastENSClockValue      :last-ens-clock-value
        :lastUpdated            :last-updated
        :localNickname          :nickname})
      (assoc :mutual?
             (and (:added contact)
                  (:hasAddedUs contact)))))

(rf/defn fetch-contacts-rpc
  [_ on-success]
  {:json-rpc/call [{:method     "wakuext_contacts"
                    :params     []
                    :on-success #(on-success (map <-rpc %))
                    :on-error   #(log/error "failed to fetch contacts" %)}]})

(rf/defn add
  [_ public-key nickname ens-name on-success]
  {:json-rpc/call [{:method      "wakuext_addContact"
                    :params      [{:id public-key :nickname nickname :ensName ens-name}]
                    :js-response true
                    :on-success  #(do
                                    (log/info "saved contact" public-key "successfuly")
                                    (when on-success
                                      (on-success %)))
                    :on-error    #(log/error "failed to add contact" public-key %)}]})

(rf/defn set-nickname
  [_ public-key nickname on-success]
  {:json-rpc/call [{:method      "wakuext_setContactLocalNickname"
                    :params      [{:id public-key :nickname nickname}]
                    :js-response true
                    :on-success  #(do
                                    (log/debug "set contact nickname" public-key "successfuly" nickname)
                                    (when on-success
                                      (on-success %)))
                    :on-error    #(log/error "failed to set contact nickname "
                                             public-key
                                             nickname
                                             %)}]})

(rf/defn block
  [_ contact-id on-success]
  {:json-rpc/call [{:method      "wakuext_blockContact"
                    :params      [contact-id]
                    :js-response true
                    :on-success  on-success
                    :on-error    #(log/error "failed to block contact" % contact-id)}]})

(rf/defn unblock
  [_ contact-id on-success]
  {:json-rpc/call [{:method      "wakuext_unblockContact"
                    :params      [contact-id]
                    :on-success  on-success
                    :js-response true
                    :on-error    #(log/error "failed to unblock contact" % contact-id)}]})
