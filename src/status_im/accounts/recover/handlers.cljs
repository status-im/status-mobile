(ns status-im.accounts.recover.handlers
  (:require [re-frame.core :refer [register-handler after dispatch dispatch-sync]]
            [status-im.components.geth :as geth]
            [status-im.utils.handlers :as u]
            [status-im.utils.types :refer [json->clj]]
            [status-im.utils.identicon :refer [identicon]]
            [status-im.chat.sign-up :as sign-up-service]
            [status-im.utils.logging :as log]
            [clojure.string :as str]))

(defn on-account-changed
  [error address new-account?]
  (dispatch [:navigate-to-clean :accounts]))

(defn account-recovered [result password]
  (let [_ (log/debug result)
        data       (json->clj result)
        public-key (:pubkey data)
        address    (:address data)
        account    {:public-key public-key
                    :address    address
                    :name       address
                    :photo-path (identicon public-key)}]
    (log/debug "account-recovered")
    (when (not (str/blank? public-key))
      (do
        (dispatch [:set-in [:recover :passphrase] ""])
        (dispatch [:set-in [:recover :password] ""])
        (dispatch [:add-account account])
        (dispatch [:navigate-back])))))

(defn recover-account
  [{:keys [recover] :as db} [_ passphrase password]]
  (geth/recover-account passphrase password (fn [result] (account-recovered result password)))
  db)

(register-handler :recover-account recover-account)