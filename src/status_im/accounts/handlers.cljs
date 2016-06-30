(ns status-im.accounts.handlers
  (:require [status-im.models.accounts :as accounts]
            [re-frame.core :refer [register-handler after dispatch debug]]
            [status-im.utils.logging :as log]
            [status-im.components.react :refer [geth]]
            [status-im.utils.types :refer [json->clj]]
            [clojure.string :as str]))


(defn save-account [_ [_ account]]
  (accounts/save-accounts [account]))

(register-handler :add-account
  (-> (fn [db [_ {:keys [address] :as account}]]
          (update db :accounts assoc address account))
      ((after save-account))))

(defn account-created [result]
  (let [data (json->clj result)
        public-key (:pubkey data)
        address (:address data)
        account {:public-key public-key
                 :address address}]
    (log/debug "Created account: " result)
    (when (not (str/blank? public-key))
      (do
        (dispatch [:initialize-protocol account])
        (dispatch [:add-account account])))))

(register-handler :create-account
  (-> (fn [db [_ password]]
          (.createAccount geth password (fn [result] (account-created result)))
        db)))