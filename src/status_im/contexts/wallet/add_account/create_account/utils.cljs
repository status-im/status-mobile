(ns status-im.contexts.wallet.add-account.create-account.utils
  (:require [clojure.string :as string]
            [status-im.constants :as constants]))

(defn first-derived-account
  [account-data keypair-type]
  (if (= keypair-type :seed)
    (some-> account-data
            :derived
            first
            val)
    account-data))

(defn get-account-details
  [keypair-type account account-to-create]
  (if (= keypair-type :key)
    {:key-uid         (:key-uid account)
     :public-key      (:public-key account)
     :account-address (:address account)}
    {:key-uid         (:keyUid account)
     :public-key      (:publicKey account-to-create)
     :account-address (:address account-to-create)}))

(defn get-account-config
  [{:keys [account-address key-uid keypair-type public-key account-name emoji color]}]
  {:address    account-address
   :key-uid    key-uid
   :wallet     false
   :chat       false
   :type       keypair-type
   :path       constants/path-default-wallet
   :public-key public-key
   :name       account-name
   :emoji      emoji
   :colorID    color
   :hidden     false})

(defn prepare-new-account
  [{:keys                              [keypair-name keypair-type]
    {:keys [address] :as account}      :account-data
    {:keys [account-name color emoji]} :account-preferences}]
  (let [account-to-create                            (first-derived-account account keypair-type)
        {:keys [key-uid public-key account-address]} (get-account-details keypair-type
                                                                          account
                                                                          account-to-create)
        account-config                               (get-account-config {:account-address
                                                                          account-address
                                                                          :key-uid key-uid
                                                                          :keypair-type keypair-type
                                                                          :public-key public-key
                                                                          :account-name account-name
                                                                          :emoji emoji
                                                                          :color color})]
    {:key-uid                    key-uid
     :name                       keypair-name
     :type                       keypair-type
     :derived-from               address
     :last-used-derivation-index 0
     :accounts                   [account-config]}))

(defn legacy-path?
  [s]
  (re-matches #"m/\d+" s))

(defn normalize-path
  [path]
  (if (legacy-path? path)
    (str constants/path-wallet-root "/" (last (string/split path "/")))
    path))
