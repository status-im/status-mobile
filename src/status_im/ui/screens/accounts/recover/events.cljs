(ns status-im.ui.screens.accounts.recover.events
  (:require
   status-im.ui.screens.accounts.recover.navigation
   [clojure.string :as string]
   [re-frame.core :as re-frame]
   [status-im.native-module.core :as status]
   [status-im.ui.screens.accounts.events :as accounts-events]
   [status-im.utils.types :as types]
   [status-im.utils.identicon :as identicon]
   [status-im.utils.handlers :as handlers]
   [status-im.utils.gfycat.core :as gfycat]
   [status-im.utils.security :as security]
   [status-im.utils.signing-phrase.core :as signing-phrase]
   [status-im.utils.hex :as utils.hex]
   [status-im.constants :as constants]))

;;;; FX

(re-frame/reg-fx
 ::recover-account-fx
 (fn [[masked-passphrase password]]
   (status/recover-account
    (security/unmask masked-passphrase)
    password
    (fn [result]
      ;; here we deserialize result, dissoc mnemonic and serialize the result again
      ;; because we want to have information about the result printed in logs, but
      ;; don't want secure data to be printed
      (let [data (-> (types/json->clj result)
                     (dissoc :mnemonic)
                     (types/clj->json))]
        (re-frame/dispatch [:account-recovered data password]))))))

;;;; Handlers

(handlers/register-handler-fx
 :account-recovered
 (fn [{:keys [db]} [_ result password]]
   (let [data       (types/json->clj result)
         public-key (:pubkey data)
         address    (-> data :address utils.hex/normalize-hex)
         phrase     (signing-phrase/generate)
         account    {:public-key            public-key
                     :address               address
                     :name                  (gfycat/generate-gfy public-key)
                     :photo-path            (identicon/identicon public-key)
                     :mnemonic              ""
                     :signed-up?            true
                     :signing-phrase        phrase
                     :settings              (constants/default-account-settings)
                     :wallet-set-up-passed? false
                     :seed-backed-up?       true}]
     (when-not (string/blank? public-key)
       (-> db
           (accounts-events/add-account account)
           (assoc :dispatch [:login-account address password])
           (assoc :dispatch-later [{:ms 2000 :dispatch [:navigate-to :usage-data [:account-finalized false]]}]))))))

(handlers/register-handler-fx
 :recover-account
 (fn [_ [_ masked-passphrase password]]
   {::recover-account-fx [masked-passphrase password]}))
