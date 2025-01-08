(ns status-im.contexts.wallet.wallet-connect.utils.data-store
  (:require
    [status-im.contexts.wallet.common.utils :as wallet-utils]
    [status-im.contexts.wallet.common.utils.networks :as network-utils]
    [status-im.contexts.wallet.wallet-connect.utils.request :as session-request]
    utils.string
    [utils.transforms :as transforms]))

(defn extract-native-call-signature
  [data]
  (-> data transforms/json->clj :result))

(defn get-db-current-request-event
  [db]
  (get-in db [:wallet-connect/current-request :event]))

(defn get-current-request-dapp
  [event sessions]
  (let [dapp-url (session-request/url event)]
    (->> sessions
         (filter (fn [session]
                   (= (utils.string/remove-trailing-slash dapp-url)
                      (utils.string/remove-trailing-slash (get session :url)))))
         first)))

(defn get-dapp-redirect-url
  [session]
  (get-in session [:peer :metadata :redirect :native]))

(defn get-dapp-name
  [session]
  (get-in session [:peer :metadata :name]))

(defn get-db-current-request-params
  [db]
  (-> db
      get-db-current-request-event
      session-request/params))

(defn get-total-connected-dapps
  [db]
  (-> db
      :wallet-connect/sessions
      count
      inc))

(defn get-session-by-topic
  [db topic]
  (->> db
       :wallet-connect/sessions
       (filter #(= (:topic %) topic))
       first))

(defn get-account-by-session
  [db session]
  (let [accounts                (get-in db [:wallet :accounts])
        session-account-address (first (:accounts session))
        [_ address]             (network-utils/split-network-full-address session-account-address)]
    (wallet-utils/get-account-by-address (vals accounts) address)))
