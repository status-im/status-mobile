(ns status-im.accounts.core
  (:require [re-frame.core :as re-frame]
            [status-im.accounts.update.core :as accounts.update]
            [status-im.i18n :as i18n]
            [status-im.ui.screens.navigation :as navigation]
            [status-im.ui.screens.wallet.settings.models :as wallet.settings.models]
            [status-im.utils.config :as config]
            [status-im.utils.utils :as utils]
            [status-im.utils.fx :as fx]
            [status-im.utils.platform :as platform]))

(fx/defn show-mainnet-is-default-alert [{:keys [db]}]
  (let [enter-name-screen? (= :enter-name (get-in db [:accounts/create :step]))
        shown? (get-in db [:account/account :mainnet-warning-shown?])]
    (when (and platform/mobile?
               config/mainnet-warning-enabled?
               (not shown?)
               (not enter-name-screen?))
      (utils/show-popup
       (i18n/label :mainnet-is-default-alert-title)
       (i18n/label :mainnet-is-default-alert-text)
       #(re-frame/dispatch [:accounts.ui/mainnet-warning-shown])))))

(defn- chat-send? [transaction]
  (and (seq transaction)
       (not (:in-progress? transaction))
       (:from-chat? transaction)))

(fx/defn continue-after-wallet-onboarding [{:keys [db] :as cofx} modal?]
  (let [transaction (get-in db [:wallet :send-transaction])]
    (if modal?
      {:dispatch [:navigate-to-clean :wallet-send-transaction-modal]}
      (if-not (chat-send? transaction)
        (navigation/navigate-to-clean cofx :wallet nil)
        (navigation/navigate-to-cofx cofx :wallet-send-transaction-modal nil)))))

(fx/defn confirm-wallet-set-up
  [{:keys [db] :as cofx} modal?]
  (fx/merge cofx
            (continue-after-wallet-onboarding modal?)
            (wallet.settings.models/wallet-autoconfig-tokens)
            (accounts.update/account-update {:wallet-set-up-passed? true} {})))

(fx/defn switch-dev-mode [cofx dev-mode?]
  (merge (accounts.update/account-update cofx
                                         {:dev-mode? dev-mode?}
                                         {})
         (if dev-mode?
           {:dev-server/start nil}
           {:dev-server/stop nil})))

(fx/defn enable-notifications [cofx desktop-notifications?]
  (merge (accounts.update/account-update cofx
                                         {:desktop-notifications? desktop-notifications?}
                                         {})))

(fx/defn switch-web3-opt-in-mode [{:keys [db] :as cofx} opt-in]
  (let [settings (get-in db [:account/account :settings])]
    (accounts.update/update-settings cofx
                                     (assoc settings :web3-opt-in? opt-in)
                                     {})))
