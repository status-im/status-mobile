(ns status-im.accounts.core
  (:require [re-frame.core :as re-frame]
            [status-im.accounts.update.core :as accounts.update]
            [status-im.i18n :as i18n]
            [status-im.ui.screens.navigation :as navigation]
            [status-im.ui.screens.wallet.settings.models :as wallet.settings.models]
            [status-im.utils.config :as config]
            [status-im.utils.handlers-macro :as handlers-macro]
            [status-im.utils.utils :as utils]))

(defn show-mainnet-is-default-alert [{:keys [db]}]
  (let [enter-name-screen? (= :enter-name (get-in db [:accounts/create :step]))
        shown? (get-in db [:account/account :mainnet-warning-shown?])]
    (when (and config/mainnet-warning-enabled?
               (not shown?)
               (not enter-name-screen?))
      (utils/show-popup
       (i18n/label :mainnet-is-default-alert-title)
       (i18n/label :mainnet-is-default-alert-text)
       #(re-frame/dispatch [:accounts.ui/update-mainnet-warning-shown])))))

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

(defn confirm-wallet-set-up [modal? {:keys [db] :as cofx}]
  (handlers-macro/merge-fx
   cofx
   (continue-after-wallet-onboarding db modal?)
   (wallet.settings.models/wallet-autoconfig-tokens)
   (accounts.update/account-update {:wallet-set-up-passed? true})))

(defn switch-dev-mode [dev-mode? cofx]
  (merge (accounts.update/account-update {:dev-mode? dev-mode?} cofx)
         (if dev-mode?
           {:dev-server/start nil}
           {:dev-server/stop nil})))

(defn switch-web3-opt-in-mode [opt-in {:keys [db] :as cofx}]
  (let [settings (get-in db [:account/account :settings])]
    (accounts.update/update-settings
     (assoc settings :web3-opt-in? opt-in)
     cofx)))
