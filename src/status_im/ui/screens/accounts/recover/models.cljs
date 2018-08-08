(ns status-im.ui.screens.accounts.recover.models
  (:require status-im.ui.screens.accounts.recover.navigation
            [clojure.string :as string]
            [re-frame.core :as re-frame]
            [status-im.native-module.core :as status]
            [status-im.ui.screens.accounts.models :as accounts.models]
            [status-im.utils.types :as types]
            [status-im.utils.identicon :as identicon]
            [status-im.utils.gfycat.core :as gfycat]
            [status-im.utils.security :as security]
            [status-im.utils.signing-phrase.core :as signing-phrase]
            [status-im.utils.hex :as utils.hex]
            [status-im.constants :as constants]))

;;;; FX

(defn recover-account-fx! [masked-passphrase password]
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
       (re-frame/dispatch [:account-recovered data password])))))

;;;; Handlers

(defn on-account-recovered [result password {:keys [db]}]
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
          (accounts.models/add-account account)
          (assoc :dispatch [:login-account address password])
          (assoc :dispatch-later [{:ms 2000 :dispatch [:account-recovered-navigate]}])))))

(defn account-recovered-navigate [{:keys [db]}]
  {:db         (assoc-in db [:accounts/recover :processing] false)
   :dispatch-n [[:navigate-to-clean :home]
                [:request-notifications]]})

(defn recover-account [masked-passphrase password {:keys [db]}]
  {:db (assoc-in db [:accounts/recover :processing] true)
   :recover-account-fx [masked-passphrase password]})
