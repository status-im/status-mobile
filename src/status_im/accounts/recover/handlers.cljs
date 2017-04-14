(ns status-im.accounts.recover.handlers
  (:require [re-frame.core :refer [register-handler after dispatch dispatch-sync]]
            [status-im.components.status :as status]
            [status-im.utils.types :refer [json->clj]]
            [status-im.utils.identicon :refer [identicon]]
            [taoensso.timbre :as log]
            [clojure.string :as str]
            [status-im.utils.handlers :as u]
            [status-im.utils.gfycat.core :refer [generate-gfy]]
            [status-im.protocol.core :as protocol]
            [status-im.navigation.handlers :as nav]))

(defn account-recovered [result]
  (let [_          (log/debug result)
        data       (json->clj result)
        public-key (:pubkey data)
        address    (:address data)
        {:keys [public private]} (protocol/new-keypair!)
        account    {:public-key          public-key
                    :address             address
                    :name                (generate-gfy)
                    :photo-path          (identicon public-key)
                    :updates-public-key  public
                    :updates-private-key private
                    :signed-up?          true}]
    (log/debug "account-recovered")
    (when-not (str/blank? public-key)
      (dispatch [:set-in [:recover :passphrase] ""])
      (dispatch [:set-in [:recover :password] ""])
      (dispatch [:add-account account])
      (dispatch [:navigate-back]))))

(defn recover-account
  [_ [_ passphrase password]]
  (status/recover-account
    passphrase
    password
    account-recovered))

(register-handler :recover-account (u/side-effect! recover-account))

(defmethod nav/preload-data! :recover
  [db]
  (update db :recover dissoc :password :passphrase))
