(ns status-im.wallet.accounts.core
  (:require [re-frame.core :as re-frame]
            [status-im.constants :as constants]
            [status-im.ethereum.core :as ethereum]
            [status-im.ethereum.eip55 :as eip55]
            [status-im.ethereum.json-rpc :as json-rpc]
            [status-im.i18n :as i18n]
            [status-im.multiaccounts.update.core :as multiaccounts.update]
            [status-im.native-module.core :as status]
            [status-im.ui.components.colors :as colors]
            [status-im.ui.components.list-selection :as list-selection]
            [status-im.ui.screens.navigation :as navigation]
            [status-im.utils.fx :as fx]
            [status-im.utils.types :as types]
            [status-im.wallet.core :as wallet]))

(re-frame/reg-fx
 :list.selection/open-share
 (fn [obj]
   (list-selection/open-share obj)))

(re-frame/reg-fx
 ::generate-account
 (fn [{:keys [derivation-info hashed-password path-num]}]
   (let [{:keys [address path]} derivation-info]
     (status/multiaccount-load-account
      address
      hashed-password
      (fn [value]
        (let [{:keys [id error]} (types/json->clj value)]
          (if error
            (re-frame/dispatch [::generate-new-account-error])
            (status/multiaccount-derive-addresses
             id
             [path]
             (fn [result]
               (status/multiaccount-store-derived
                id
                [path]
                hashed-password
                (fn [result]
                  (let [{:keys [public-key address]}
                        (get (types/json->clj result) (keyword path))]
                    (re-frame/dispatch [::account-generated
                                        {:name (str "Account " path-num)
                                         :address address
                                         :public-key public-key
                                         :path (str constants/path-wallet-root "/" path-num)
                                         :color (rand-nth colors/account-colors)}])))))))))))))

(fx/defn set-symbol-request
  {:events [:wallet.accounts/share]}
  [_ address]
  {:list.selection/open-share {:message (eip55/address->checksum address)}})

(fx/defn generate-new-account
  {:events [:wallet.accounts/generate-new-account]}
  [{:keys [db]} password]
  (let [wallet-root-address (get-in db [:multiaccount :wallet-root-address])
        path-num (inc (get-in db [:multiaccount :latest-derived-path]))]
    (when-not (get-in db [:generate-account :step])
      {:db (assoc-in db [:generate-account :step] :generating)
       ::generate-account {:derivation-info  (if wallet-root-address
                                               ;; Use the walllet-root-address for stored on disk keys
                                               ;; This needs to be the RELATIVE path to the key used to derive
                                               {:path (str "m/" path-num)
                                                :address wallet-root-address}
                                               ;; Fallback on the master account for keycards, use the absolute path
                                               {:path (str constants/path-wallet-root "/" path-num)
                                                :address  (get-in db [:multiaccount :address])})
                           :path-num          path-num
                           :hashed-password   (ethereum/sha3 password)}})))

(fx/defn generate-new-account-error
  {:events [::generate-new-account-error]}
  [{:keys [db]} password]
  {:db (assoc db
              :generate-account
              {:error (i18n/label :t/add-account-incorrect-password)})})

(fx/defn account-generated
  {:events [::account-generated]}
  [{:keys [db] :as cofx} account]
  (fx/merge cofx
            {:db (assoc db :generate-account {:account account
                                              :step :generated})}
            (navigation/navigate-to-cofx :account-added nil)))

(fx/defn save-account
  {:events [:wallet.accounts/save-generated-account]}
  [{:keys [db] :as cofx}]
  (let [{:keys [accounts latest-derived-path]} (:multiaccount db)
        {:keys [account]} (:generate-account db)]
    (when account
      (fx/merge cofx
                {::json-rpc/call [{:method "accounts_saveAccounts"
                                   :params [[account]]
                                   :on-success #()}]
                 :db (dissoc db :generate-account)}
                (multiaccounts.update/multiaccount-update {:accounts (conj accounts account)
                                                           :latest-derived-path (inc latest-derived-path)} nil)
                (wallet/update-balances nil)
                (navigation/navigate-to-cofx :wallet nil)))))
