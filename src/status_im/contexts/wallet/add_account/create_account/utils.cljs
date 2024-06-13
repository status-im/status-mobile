(ns status-im.contexts.wallet.add-account.create-account.utils
  (:require [status-im.constants :as constants]))

(defn first-derived-account
  [account-data keypair-type]
  (if (= keypair-type :seed)
    (some-> account-data :derived first val)
      account-data))

(defn prepare-new-account
  [{:keys [keypair-name keypair-type]
    {:keys [keyUid address] :as account} :account-data
    {:keys [account-name color emoji]}   :account-preferences}]
  (let [account-to-create (first-derived-account account keypair-type)
        account-config    {:address    (:address account-to-create)
                           :key-uid    keyUid
                           :wallet     false
                           :chat       false
                           :type       keypair-type
                           :path       constants/path-default-wallet
                           :public-key (:publicKey account-to-create)
                           :name       account-name
                           :emoji      emoji
                           :colorID    color
                           :hidden     false}]
    {:key-uid                    keyUid
     :name                       keypair-name
     :type                       keypair-type
     :derived-from               address
     :last-used-derivation-index 0
     :accounts                   [account-config]}))
