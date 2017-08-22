(ns status-im.ui.screens.accounts.recover.events
  (:require
    status-im.ui.screens.accounts.recover.navigation

    [re-frame.core :refer [reg-fx inject-cofx dispatch]]
    [status-im.components.status :as status]
    [status-im.utils.types :refer [json->clj]]
    [status-im.utils.identicon :refer [identicon]]
    [taoensso.timbre :as log]
    [clojure.string :as str]
    [status-im.utils.handlers :refer [register-handler-fx]]
    [status-im.utils.gfycat.core :refer [generate-gfy]]))

;;;; FX

(reg-fx
  ::recover-account-fx
  (fn [[passphrase password]]
    (status/recover-account
     (str/trim passphrase)
     password
     #(dispatch [:account-recovered %]))))

;;;; Handlers

(register-handler-fx
  :account-recovered
  [(inject-cofx :get-new-keypair!)]
  (fn [{:keys [db keypair]} [_ result]]
    (let [data (json->clj result)
          public-key (:pubkey data)
          address (:address data)
          {:keys [public private]} keypair
          account {:public-key          public-key
                   :address             address
                   :name                (generate-gfy public-key)
                   :photo-path          (identicon public-key)
                   :updates-public-key  public
                   :updates-private-key private
                   :signed-up?          true}]
      (log/debug "account-recovered")
      (when-not (str/blank? public-key)
        {:db         (update db :accounts/recover assoc :passphrase "" :password "")
         :dispatch-n [[:add-account account]
                      [:navigate-back]]}))))

(register-handler-fx
  :recover-account
  (fn [_ [_ passphrase password]]
    {::recover-account-fx [passphrase password]}))
