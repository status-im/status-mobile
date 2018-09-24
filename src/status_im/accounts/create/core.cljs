(ns status-im.accounts.create.core
  (:require [clojure.string :as string]
            [re-frame.core :as re-frame]
            [status-im.accounts.login.core :as accounts.login]
            [status-im.accounts.statuses :as statuses]
            [status-im.accounts.update.core :as accounts.update]
            [status-im.constants :as constants]
            [status-im.data-store.accounts :as accounts-store]
            [status-im.i18n :as i18n]
            [status-im.hardwallet.core :as hardwallet]
            [status-im.native-module.core :as status]
            [status-im.ui.screens.navigation :as navigation]
            [status-im.utils.config :as config]
            [status-im.utils.random :as random]
            [status-im.utils.gfycat.core :as gfycat]
            [status-im.utils.handlers-macro :as handlers-macro]
            [status-im.utils.hex :as utils.hex]
            [status-im.utils.identicon :as identicon]
            [status-im.utils.signing-phrase.core :as signing-phrase]
            [status-im.utils.types :as types]
            [taoensso.timbre :as log]))

(defn get-signing-phrase [cofx]
  (assoc cofx :signing-phrase (signing-phrase/generate)))

(defn get-status [cofx]
  (assoc cofx :status (rand-nth statuses/data)))

(defn create-account! [password]
  (status/create-account
   password
   #(re-frame/dispatch [:accounts.create.callback/create-account-success (types/json->clj %) password])))

;;;; Handlers

(defn create-account [{{:accounts/keys [create] :as db} :db}]
  {:db (update db :accounts/create assoc :step :account-creating :error nil)
   :accounts.create/create-account (:password create)})

(defn- add-account
  "Takes db and new account, creates map of effects describing adding account to database and realm"
  [{:keys [address] :as account} cofx]
  (let [db (:db cofx)
        {:networks/keys [networks]} db
        enriched-account (assoc account
                                :network config/default-network
                                :networks networks
                                :address address)]
    {:db                 (assoc-in db [:accounts/accounts address] enriched-account)
     :data-store/base-tx [(accounts-store/save-account-tx enriched-account)]}))

(defn on-account-created [{:keys [pubkey address mnemonic]} password seed-backed-up {:keys [random-guid-generator
                                                                                            signing-phrase
                                                                                            status
                                                                                            db] :as cofx}]
  (let [normalized-address (utils.hex/normalize-hex address)
        account            {:public-key      pubkey
                            :installation-id (random-guid-generator)
                            :address         normalized-address
                            :name            (gfycat/generate-gfy pubkey)
                            :status          status
                            :signed-up?      true
                            :photo-path      (identicon/identicon pubkey)
                            :signing-phrase  signing-phrase
                            :seed-backed-up? seed-backed-up
                            :mnemonic        mnemonic
                            :settings        (constants/default-account-settings)}]
    (log/debug "account-created")
    (when-not (string/blank? pubkey)
      (handlers-macro/merge-fx cofx
                               {:db (assoc db :accounts/login {:address normalized-address
                                                               :password password
                                                               :processing true})}
                               (add-account account)
                               (accounts.login/user-login)))))

(defn reset-account-creation [{db :db}]
  {:db (update db :accounts/create assoc
               :step :enter-password
               :password nil
               :password-confirm nil
               :error nil)})

(defn account-set-input-text [input-key text {db :db}]
  {:db (update db :accounts/create merge {input-key text :error nil})})

(defn account-set-name [{{:accounts/keys [create] :as db} :db :as cofx}]
  (handlers-macro/merge-fx cofx
                           {:db                                  (assoc db :accounts/create {:show-welcome? true})
                            :notifications/request-notifications-permissions nil
                            :dispatch                            [:navigate-to :home]}
                           (accounts.update/account-update {:name (:name create)})))

(defn next-step [step password password-confirm {:keys [db] :as cofx}]
  (case step
    :enter-password {:db (assoc-in db [:accounts/create :step] :confirm-password)}
    :confirm-password (if (= password password-confirm)
                        (create-account cofx)
                        {:db (assoc-in db [:accounts/create :error] (i18n/label :t/password_error1))})
    :enter-name (account-set-name cofx)))

(defn step-back [step cofx]
  (case step
    :enter-password (navigation/navigate-back cofx)
    :confirm-password (reset-account-creation cofx)))

(defn navigate-to-create-account-screen [{:keys [db] :as cofx}]
  (handlers-macro/merge-fx cofx
                           {:db (update db :accounts/create
                                        #(-> %
                                             (assoc :step :enter-password)
                                             (dissoc :password :password-confirm :name :error)))}
                           (navigation/navigate-to-cofx :create-account nil)))

(defn navigate-to-authentication-method [{:keys [db] :as cofx}]
  (if (hardwallet/hardwallet-supported? db)
    (navigation/navigate-to-cofx :hardwallet-authentication-method nil cofx)
    (navigate-to-create-account-screen cofx)))

;;;; COFX

(re-frame/reg-cofx
 :accounts.create/get-signing-phrase
 (fn [cofx _]
   (get-signing-phrase cofx)))

(re-frame/reg-cofx
 :accounts.create/get-status
 (fn [cofx _]
   (get-status cofx)))

;;;; FX

(re-frame/reg-fx
 :accounts.create/create-account
 create-account!)
