(ns status-im.contexts.wallet.add-account.create-account.utils
  (:require [status-im.constants :as constants]))

(defn first-derived-account
  [account-data]
  (-> account-data :derived first val))

(defn prepare-new-account
  [{keypair-name                         :keypair-name
    {:keys [keyUid address] :as account} :account-data
    {:keys [account-name color emoji]}   :account-preferences}]
  (let [account-to-create (first-derived-account account)
        account-config    {:address    (:address account-to-create)
                           :key-uid    keyUid
                           :wallet     false
                           :chat       false
                           :type       :seed
                           :path       constants/path-default-wallet
                           :public-key (:publicKey account-to-create)
                           :name       account-name
                           :emoji      emoji
                           :colorID    color
                           :hidden     false}]
    {:key-uid                    keyUid
     :name                       keypair-name
     :type                       :seed
     :derived-from               address
     :last-used-derivation-index 0
     :accounts                   [account-config]}))
