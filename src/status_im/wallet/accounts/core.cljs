(ns status-im.wallet.accounts.core
  (:require [re-frame.core :as re-frame]
            [status-im.ethereum.core :as ethereum]
            [status-im.utils.fx :as fx]
            [status-im.ethereum.eip55 :as eip55]
            [status-im.ui.components.list-selection :as list-selection]
            [status-im.utils.handlers :as handlers]
            [status-im.native-module.core :as status]
            [status-im.utils.types :as types]
            [status-im.constants :as constants]
            [status-im.ui.components.colors :as colors]
            [status-im.ui.screens.navigation :as navigation]
            [status-im.multiaccounts.update.core :as multiaccounts.update]
            [status-im.wallet.core :as wallet]
            [status-im.i18n :as i18n]))

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
             #(re-frame/dispatch [:wallet.accounts/account-generated
                                  (merge
                                   (get (types/json->clj %) (keyword path))
                                   {:name  (str "Account " path-num)
                                    :color (rand-nth colors/account-colors)})])))))))))

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
  (let [new-account (get-in db [:generate-account :account])
        {:keys [accounts latest-derived-path]} (:multiaccount db)]
    (fx/merge cofx
              (multiaccounts.update/multiaccount-update {:accounts (conj accounts new-account)
                                                         :latest-derived-path (inc latest-derived-path)} nil)
              (wallet/update-balances nil)
              (navigation/navigate-to-cofx :wallet nil))))