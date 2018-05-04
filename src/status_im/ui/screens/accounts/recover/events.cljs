(ns status-im.ui.screens.accounts.recover.events
  (:require
   status-im.ui.screens.accounts.recover.navigation
   [re-frame.core :as re-frame]
   [status-im.native-module.core :as status]
   [status-im.ui.screens.accounts.events :as accounts-events]
   [status-im.utils.types :as types]
   [status-im.utils.identicon :as identicon]
   [clojure.string :as string]
   [status-im.utils.handlers :as handlers]
   [status-im.utils.gfycat.core :as gfycat]
   [status-im.utils.signing-phrase.core :as signing-phrase]
   [status-im.utils.hex :as utils.hex]
   [status-im.constants :as constants]))

;;;; FX

(re-frame/reg-fx
  ::recover-account-fx
  (fn [[passphrase password]]
    (status/recover-account
     (string/trim passphrase)
     password
     #(re-frame/dispatch [:account-recovered % password]))))

;;;; Handlers

(handlers/register-handler-fx
  :account-recovered
  (fn [{:keys [db]} [_ result password]]
    (let [data       (types/json->clj result)
          public-key (:pubkey data)
          address    (-> data :address utils.hex/normalize-hex)
          phrase     (signing-phrase/generate)
          account    {:public-key     public-key
                      :address        address
                      :name           (gfycat/generate-gfy public-key)
                      :photo-path     (identicon/identicon public-key)
                      :mnemonic       ""
                      :signed-up?     true
                      :signing-phrase phrase
                      :settings       constants/default-account-settings}]
      (when-not (string/blank? public-key)
        (-> db
            (accounts-events/add-account account)
            (assoc :dispatch [:login-account address password])
            (assoc :dispatch-later [{:ms 2000 :dispatch [:navigate-to :usage-data]}]))))))

(handlers/register-handler-fx
  :recover-account
  (fn [_ [_ passphrase password]]
    {::recover-account-fx [passphrase password]}))
