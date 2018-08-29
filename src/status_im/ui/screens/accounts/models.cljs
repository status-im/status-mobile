(ns status-im.ui.screens.accounts.models
  (:require [clojure.string :as str]
            [re-frame.core :as re-frame]
            [status-im.constants :as constants]
            [status-im.data-store.accounts :as accounts-store]
            [status-im.i18n :as i18n]
            [status-im.native-module.core :as status]
            [status-im.ui.screens.accounts.login.models :as login.models]
            [status-im.ui.screens.accounts.statuses :as statuses]
            [status-im.ui.screens.accounts.utils :as accounts.utils]
            [status-im.ui.screens.navigation :as navigation]
            [status-im.ui.screens.wallet.settings.models :as wallet.settings.models]
            [status-im.utils.config :as config]
            [status-im.utils.gfycat.core :as gfycat]
            [status-im.utils.handlers-macro :as handlers-macro]
            [status-im.utils.hex :as utils.hex]
            [status-im.utils.identicon :as identicon]
            [status-im.utils.signing-phrase.core :as signing-phrase]
            [status-im.utils.types :as types]
            [status-im.utils.utils :as utils]
            [taoensso.timbre :as log]))

;;;; COFX

(defn get-signing-phrase [cofx]
  (assoc cofx :signing-phrase (signing-phrase/generate)))

(defn get-status [cofx]
  (assoc cofx :status (rand-nth statuses/data)))

;;;; FX

(defn create-account! [password]
  (status/create-account
   password
   #(re-frame/dispatch [:account-created (types/json->clj %) password])))

;;;; Handlers

(defn create-account [{{:accounts/keys [create] :as db} :db}]
  {:db              (update db :accounts/create assoc :step :account-creating :error nil)
   :create-account (:password create)})

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

(defn on-account-created [{:keys [pubkey address mnemonic]} password seed-backed-up {:keys [signing-phrase status db] :as cofx}]
  (let [normalized-address (utils.hex/normalize-hex address)
        account            {:public-key      pubkey
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
    (when-not (str/blank? pubkey)
      (handlers-macro/merge-fx cofx
                               {:db (assoc db :accounts/login {:address normalized-address
                                                               :password password
                                                               :processing true})}
                               (add-account account)
                               (login.models/user-login)))))

(defn update-settings
  ([settings cofx] (update-settings settings nil cofx))
  ([settings success-event {{:keys [account/account] :as db} :db :as cofx}]
   (let [new-account (assoc account :settings settings)]
     {:db                 (assoc db :account/account new-account)
      :data-store/base-tx [{:transaction   (accounts-store/save-account-tx new-account)
                            :success-event success-event}]})))

(defn account-set-name [{{:accounts/keys [create] :as db} :db :as cofx}]
  (handlers-macro/merge-fx cofx
                           {:db                                  (assoc db :accounts/create {:show-welcome? true})
                            :notifications/request-notifications nil
                            :dispatch                            [:navigate-to-clean :home]}
                           (accounts.utils/account-update {:name (:name create)})))

(defn account-set-input-text [input-key text {db :db}]
  {:db (update db :accounts/create merge {input-key text :error nil})})

(defn show-mainnet-is-default-alert [{:keys [db]}]
  (let [enter-name-screen? (= :enter-name (get-in db [:accounts/create :step]))
        shown? (get-in db [:account/account :mainnet-warning-shown?])]
    (when (and config/mainnet-warning-enabled?
               (not shown?)
               (not enter-name-screen?))
      (utils/show-popup
       (i18n/label :mainnet-is-default-alert-title)
       (i18n/label :mainnet-is-default-alert-text)
       #(re-frame/dispatch [:update-mainnet-warning-shown])))))

(defn reset-account-creation [{db :db}]
  {:db (update db :accounts/create assoc :step :enter-password :password nil :password-confirm nil :error nil)})

(defn- chat-send? [transaction]
  (and (seq transaction)
       (not (:in-progress? transaction))
       (:from-chat? transaction)))

(defn continue-after-wallet-onboarding [db modal? cofx]
  (let [transaction (get-in db [:wallet :send-transaction])]
    (cond modal? {:dispatch [:navigate-to-modal :wallet-send-transaction-modal]}
          (chat-send? transaction) {:db       (navigation/navigate-back db)
                                    :dispatch [:navigate-to :wallet-send-transaction-chat]}
          :else {:db (navigation/navigate-back db)})))

(defn wallet-set-up-passed [modal? {:keys [db] :as cofx}]
  (handlers-macro/merge-fx
   cofx
   (continue-after-wallet-onboarding db modal?)
   (wallet.settings.models/wallet-autoconfig-tokens)
   (accounts.utils/account-update {:wallet-set-up-passed? true})))
