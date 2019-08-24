(ns status-im.wallet.accounts.core
  (:require [re-frame.core :as re-frame]
            [status-im.constants :as constants]
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
 :wallet.accounts/generate-account
 (fn [{:keys [address password path-num]}]
   (status/multiaccount-load-account
    address
    password
    (fn [value]
      (let [{:keys [id error]} (types/json->clj value)]
        (if error
          (re-frame/dispatch [:set-in [:generate-account :error] (i18n/label :t/add-account-incorrect-password)])
          (let [path (str constants/path-root "/" path-num)]
            (status/multiaccount-derive-addresses
             id
             [path]
             (fn [result]
               (status/multiaccount-store-derived
                id
                [path]
                password
                (fn [result]
                  (let [{:keys [public-key address]}
                        (get (types/json->clj result) (keyword path))]
                    (re-frame/dispatch [:wallet.accounts/account-generated
                                        {:name (str "Account " path-num)
                                         :address address
                                         :public-key public-key
                                         :path path
                                         :color (rand-nth colors/account-colors)}])))))))))))))

(fx/defn set-symbol-request
  {:events [:wallet.accounts/share]}
  [_ address]
  {:list.selection/open-share {:message (eip55/address->checksum address)}})

(fx/defn generate-new-account
  {:events [:wallet.accounts/generate-new-account]}
  [{:keys [db]} password]
  {:wallet.accounts/generate-account {:address  (get-in db [:multiaccount :address])
                                      :path-num (inc (get-in db [:multiaccount :latest-derived-path]))
                                      :password password}})

(fx/defn account-generated
  {:events [:wallet.accounts/account-generated]}
  [{:keys [db] :as cofx} account]
  (fx/merge cofx
            {:db (assoc db :generate-account {:account account})}
            (navigation/navigate-to-cofx :account-added nil)))

(fx/defn save-account
  {:events [:wallet.accounts/save-generated-account]}
  [{:keys [db] :as cofx}]
  (let [{:keys [accounts latest-derived-path]} (:multiaccount db)
        {:keys [account]} (:generate-account db)]
    (fx/merge cofx
              {::json-rpc/call [{:method "accounts_saveAccounts"
                                 :params [[account]]
                                 :on-success #()}]
               :db (dissoc db :generate-account)}
              (multiaccounts.update/multiaccount-update {:accounts (conj accounts account)
                                                         :latest-derived-path (inc latest-derived-path)} nil)
              (wallet/update-balances nil)
              (navigation/navigate-to-cofx :wallet nil))))
