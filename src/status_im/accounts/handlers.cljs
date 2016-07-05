(ns status-im.accounts.handlers
  (:require [status-im.models.accounts :as accounts]
            [re-frame.core :refer [register-handler after dispatch debug]]
            [status-im.utils.logging :as log]
            [status-im.components.react :refer [geth]]
            [status-im.utils.types :refer [json->clj]]
            [status-im.persistence.simple-kv-store :as kv]
            [status-im.protocol.state.storage :as storage]
            [status-im.utils.identicon :refer [identicon]]
            [status-im.db :refer [default-view]]
            [clojure.string :as str]))


(defn save-account [_ [_ account]]
  (accounts/save-accounts [account]))

(register-handler :add-account
  (-> (fn [db [_ {:keys [address] :as account}]]
          (update db :accounts assoc address account))
      ((after save-account))))

(defn save-password [password]
  (storage/put kv/kv-store :password password))

(defn account-created [result password]
  (let [data (json->clj result)
        public-key (:pubkey data)
        address (:address data)
        account {:public-key public-key
                 :address address
                 :name address
                 :photo-path (identicon address)}]
    (log/debug "Created account: " result)
    (when (not (str/blank? public-key))
      (do
        (save-password password)
        (dispatch [:add-account account])
        (dispatch [:login-account address password])))))

(register-handler :create-account
  (-> (fn [db [_ password]]
          (.createAccount geth password (fn [result] (account-created result password)))
        db)))

(register-handler :login-account
  (-> (fn [db [_ address password]]
        (.login geth address password (fn [result]
                                        (let [account (get-in db [:accounts address])]
                                          (log/debug "Logged in account: " address result)
                                          (dispatch [:set :login {}])
                                          (dispatch [:set :current-account account])
                                          (dispatch [:initialize-protocol account])
                                          (dispatch [:navigate-to-clean default-view]))))
        db)))

(defn load-accounts! [db _]
  (let [accounts (->> (accounts/get-accounts)
                      (map (fn [{:keys [address] :as account}]
                             [address account]))
                      (into {}))]
    (assoc db :accounts accounts)))

(register-handler :load-accounts load-accounts!)