(ns status-im.wallet.accounts.core
  (:require
   [clojure.string :as string]
   [quo.design-system.colors :as colors]
   [re-frame.core :as re-frame]
   [status-im2.constants :as constants]
   [status-im.ens.core :as ens.core]
   [status-im.ethereum.core :as ethereum]
   [status-im.ethereum.eip55 :as eip55]
   [status-im.ethereum.eip681 :as eip681]
   [status-im.ethereum.mnemonic :as mnemonic]
   [status-im.ethereum.stateofus :as stateofus]
   [utils.i18n :as i18n]
   [status-im.multiaccounts.core :as multiaccounts]
   [status-im.multiaccounts.key-storage.core :as key-storage]
   [status-im.multiaccounts.update.core :as multiaccounts.update]
   [status-im.native-module.core :as status]
   [status-im.ui.components.list-selection :as list-selection]
   [utils.re-frame :as rf]
   [status-im.utils.hex :as hex]
   [status-im.utils.mobile-sync :as utils.mobile-sync]
   [status-im.utils.types :as types]
   [status-im.wallet.core :as wallet]
   [status-im.wallet.prices :as prices]
   [status-im2.navigation.events :as navigation]
   [taoensso.timbre :as log]
   [utils.security.core :as security]))

(rf/defn start-adding-new-account
  {:events [:wallet.accounts/start-adding-new-account]}
  [{:keys [db] :as cofx} {:keys [type] :as add-account}]
  (let [{:keys [latest-derived-path]} (:multiaccount db)
        path-num                      (inc latest-derived-path)
        account                       (merge
                                       {:color (rand-nth colors/account-colors)}
                                       (when (= type :generate)
                                         {:name (str "Account " path-num)}))]
    (rf/merge cofx
              {:db (assoc db :add-account (assoc add-account :account account))}
              (navigation/navigate-to-cofx :add-new-account nil))))

(rf/defn new-account-error
  {:events [::new-account-error]}
  [{:keys [db]} error-key error]
  {:db (update db
               :add-account
               merge
               {error-key error
                :step     nil})})

(defn account-stored
  [path type]
  (fn [result]
    (let [{:keys [error publicKey address]} (types/json->clj result)]
      (if error
        (re-frame/dispatch [::new-account-error :account-error error])
        (re-frame/dispatch [:wallet.accounts/account-stored
                            {:address    address
                             :public-key publicKey
                             :type       type
                             :path       path}])))))

(defn normalize-path
  [path]
  (if (string/starts-with? path "m/")
    (str constants/path-wallet-root
         "/"
         (last (string/split path "/")))
    path))

(defn derive-and-store-account
  [key-uid path hashed-password type accounts]
  (fn [value]
    (let [{:keys [id error]} (types/json->clj value)]
      (if error
        (re-frame/dispatch [::new-account-error :password-error error])
        (status/multiaccount-derive-addresses
         id
         [path]
         (fn [derived]
           (let [derived-address (get-in (types/json->clj derived) [(keyword path) :address])]
             (if (some #(= derived-address (get % :address)) accounts)
               (re-frame/dispatch [::new-account-error :account-error
                                   (i18n/label :t/account-exists-title)])
               (status/multiaccount-store-derived
                id
                key-uid
                [path]
                hashed-password
                (fn [result]
                  (let [{:keys [error] :as result}  (types/json->clj result)
                        {:keys [publicKey address]} (get result (keyword path))]
                    (if error
                      (re-frame/dispatch [::new-account-error :account-error error])
                      (re-frame/dispatch
                       [:wallet.accounts/account-stored
                        {:address    address
                         :public-key publicKey
                         :type       type
                         :path       (normalize-path path)}])))))))))))))

(def pass-error
  "cannot retrieve a valid key for a given account: could not decrypt key with given password")

(defn store-account
  [key-uid path hashed-password type]
  (fn [value]
    (let [{:keys [id error]} (types/json->clj value)]
      (if error
        (re-frame/dispatch [::new-account-error
                            (if (= error pass-error) :password-error :account-error)
                            error])
        (status/multiaccount-store-account
         id
         key-uid
         hashed-password
         (account-stored path type))))))

(re-frame/reg-fx
 ::verify-password
 (fn [{:keys [address hashed-password]}]
   (status/verify
    address
    hashed-password
    #(re-frame/dispatch [:wallet.accounts/add-new-account-password-verifyied % hashed-password]))))

(re-frame/reg-fx
 ::generate-account
 (fn [{:keys [derivation-info hashed-password accounts key-uid]}]
   (let [{:keys [address path]} derivation-info]
     (status/multiaccount-load-account
      address
      hashed-password
      (derive-and-store-account key-uid path hashed-password :generated accounts)))))

(re-frame/reg-fx
 ::import-account-seed
 (fn [{:keys [passphrase hashed-password accounts key-uid]}]
   (status/multiaccount-import-mnemonic
    (mnemonic/sanitize-passphrase (security/unmask passphrase))
    ""
    (derive-and-store-account key-uid constants/path-default-wallet hashed-password :seed accounts))))

(re-frame/reg-fx
 ::import-account-private-key
 (fn [{:keys [private-key hashed-password key-uid]}]
   (status/multiaccount-import-private-key
    (string/trim (security/unmask private-key))
    (store-account key-uid constants/path-default-wallet hashed-password :key))))

(rf/defn generate-new-account
  [{:keys [db]} hashed-password]
  (let [{:keys [key-uid wallet-root-address]}
        (get db :multiaccount)
        path-num                              (inc (get-in db [:multiaccount :latest-derived-path]))
        accounts                              (:multiaccount/accounts db)]
    {:db                (assoc-in db [:add-account :step] :generating)
     ::generate-account {:derivation-info {:path    (str "m/" path-num)
                                           :address wallet-root-address}
                         :hashed-password hashed-password
                         :accounts        accounts
                         :key-uid         key-uid}}))

(rf/defn import-new-account-seed
  [{:keys [db]} passphrase hashed-password]
  {:db                               (assoc-in db [:add-account :step] :generating)
   ::multiaccounts/validate-mnemonic [(security/safe-unmask-data passphrase)
                                      #(re-frame/dispatch [:wallet.accounts/seed-validated
                                                           % passphrase hashed-password])]})

(rf/defn new-account-seed-validated
  {:events [:wallet.accounts/seed-validated]}
  [{:keys [db] :as cofx} phrase-warnings passphrase hashed-password]
  (let [error             (:error (types/json->clj phrase-warnings))
        {:keys [key-uid]} (:multiaccount db)]
    (if-not (string/blank? error)
      (new-account-error cofx :account-error error)
      (let [accounts (:multiaccount/accounts db)]
        {::import-account-seed {:passphrase      passphrase
                                :hashed-password hashed-password
                                :accounts        accounts
                                :key-uid         key-uid}}))))

(rf/defn import-new-account-private-key
  [{:keys [db]} private-key hashed-password]
  (let [{:keys [key-uid]} (:multiaccount db)]
    {:db                          (assoc-in db [:add-account :step] :generating)
     ::import-account-private-key {:private-key     private-key
                                   :hashed-password hashed-password
                                   :key-uid         key-uid}}))

(rf/defn save-new-account
  [{:keys [db] :as cofx}]
  (let [{:keys [latest-derived-path]} (:multiaccount db)
        {:keys [account type]}        (:add-account db)
        accounts                      (:multiaccount/accounts db)
        new-accounts                  (conj accounts account)]
    (when account
      (rf/merge cofx
                {:json-rpc/call [{:method     "accounts_saveAccounts"
                                  :params     [[account]]
                                  :on-success #(re-frame/dispatch [::wallet/restart])}]
                 :db            (-> db
                                    (assoc :multiaccount/accounts new-accounts)
                                    (dissoc :add-account))}
                (when (= type :generate)
                  (multiaccounts.update/multiaccount-update
                   :latest-derived-path
                   (inc latest-derived-path)
                   {}))))))

(rf/defn account-generated
  {:events [:wallet.accounts/account-stored]}
  [{:keys [db] :as cofx} {:keys [address] :as account}]
  (let [accounts (:multiaccount/accounts db)]
    (if (some #(when (= (:address %) address) %) accounts)
      (new-account-error cofx :account-error (i18n/label :t/account-exists-title))
      (rf/merge cofx
                {:db (update-in db [:add-account :account] merge account)}
                (save-new-account)
                (if (utils.mobile-sync/syncing-allowed? cofx)
                  (wallet/set-max-block address 0)
                  (wallet/update-balances nil true))
                (wallet/fetch-collectibles-collection)
                (prices/update-prices)
                (navigation/navigate-back)))))

(rf/defn add-watch-account
  [{:keys [db] :as cofx}]
  (let [address (get-in db [:add-account :address])]
    (account-generated cofx
                       {:address (eip55/address->checksum (ethereum/normalized-hex address))
                        :type    :watch})))

(rf/defn add-new-account-password-verifyied
  {:events [:wallet.accounts/add-new-account-password-verifyied]}
  [{:keys [db] :as cofx} result hashed-password]
  (let [{:keys [error]} (types/json->clj result)]
    (if (not (string/blank? error))
      (new-account-error cofx :password-error error)
      (let [{:keys [type seed private-key]} (:add-account db)]
        (case type
          :seed
          (import-new-account-seed cofx seed hashed-password)
          :key
          (import-new-account-private-key cofx private-key hashed-password)
          nil)))))

(rf/defn add-new-account-verify-password
  [{:keys [db]} hashed-password]
  {:db               (assoc-in db [:add-account :step] :generating)
   ::verify-password {:address         (get-in db [:multiaccount :wallet-root-address])
                      :hashed-password hashed-password}})

(rf/defn set-account-to-watch
  {:events [:wallet.accounts/set-account-to-watch]}
  [{:keys [db]} account]
  (let [name? (and (>= (count account) 3)
                   (not (hex/valid-hex? account)))]
    (log/debug "[wallet] set-account-to-watch" account
               "name?"                         name?)
    (cond-> {:db (assoc-in db [:add-account :address] account)}
      name?
      (assoc ::ens.core/resolve-address
             [(ethereum/chain-id db)
              (stateofus/ens-name-parse account)
              #(re-frame/dispatch
                [:wallet.accounts/set-account-to-watch %])]))))

(rf/defn add-new-account
  {:events [:wallet.accounts/add-new-account]}
  [{:keys [db] :as cofx} hashed-password]
  (let [{:keys [type step]} (:add-account db)]
    (log/debug "[wallet] add-new-account"
               "type" type
               "step" step)
    (when-not step
      (case type
        :watch
        (add-watch-account cofx)
        :generate
        (generate-new-account cofx hashed-password)
        (:seed :key)
        (add-new-account-verify-password cofx hashed-password)
        nil))))

(rf/defn save-account
  {:events [:wallet.accounts/save-account]}
  [{:keys [db]} account {:keys [name color hidden]}]
  (let [accounts     (:multiaccount/accounts db)
        new-account  (cond-> account
                       name                (assoc :name name)
                       color               (assoc :color color)
                       (not (nil? hidden)) (assoc :hidden hidden))
        new-accounts (replace {account new-account} accounts)]
    {:json-rpc/call [{:method     "accounts_saveAccounts"
                      :params     [[new-account]]
                      :on-success #()}]
     :db            (assoc db :multiaccount/accounts new-accounts)}))

(rf/defn delete-account
  {:events [:wallet.accounts/delete-account]}
  [{:keys [db] :as cofx} account]
  (let [accounts        (:multiaccount/accounts db)
        new-accounts    (vec (remove #(= account %) accounts))
        deleted-address (:address account)]
    (rf/merge cofx
              {:json-rpc/call [{:method     "accounts_deleteAccount"
                                :params     [(:address account)]
                                :on-success #()}]
               :db            (-> db
                                  (assoc :multiaccount/accounts new-accounts)
                                  (update-in [:wallet :accounts] dissoc deleted-address))}
              (navigation/pop-to-root-tab :wallet-stack))))

(rf/defn delete-account-key
  {:events [:wallet.accounts/delete-key]}
  [{:keys [db] :as cofx} account password on-error]
  (let [deleted-address (:address account)
        dapps-address   (get-in cofx [:db :multiaccount :dapps-address])]
    (if (= (string/lower-case dapps-address) (string/lower-case deleted-address))
      {:utils/show-popup {:title   (i18n/label :t/warning)
                          :content (i18n/label :t/account-is-used)}}
      {::key-storage/delete-imported-key
       {:key-uid    (get-in db [:multiaccount :key-uid])
        :address    (:address account)
        :password   password
        :on-success #(do
                       (re-frame/dispatch [:hide-popover])
                       (re-frame/dispatch [:wallet.accounts/delete-account account]))
        :on-error   on-error}})))

(rf/defn view-only-qr-scanner-result
  {:events [:wallet.add-new/qr-scanner-result]}
  [{db :db :as cofx} data _]
  (let [address (:address (eip681/parse-uri data))]
    (rf/merge cofx
              (merge {:db (-> db
                              (assoc-in [:add-account :scanned-address] address)
                              (assoc-in [:add-account :address] address))}
                     (when-not address
                       {:utils/show-popup {:title   (i18n/label :t/error)
                                           :content (i18n/label :t/invalid-address-qr-code)}}))
              (navigation/navigate-back))))

(re-frame/reg-fx
 :list.selection/open-share
 (fn [obj]
   (list-selection/open-share obj)))

(rf/defn wallet-accounts-share
  {:events [:wallet.accounts/share]}
  [_ address]
  {:list.selection/open-share {:message (eip55/address->checksum address)}})

