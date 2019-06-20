(ns status-im.accounts.core
  (:require [re-frame.core :as re-frame]
            [status-im.accounts.update.core :as accounts.update]
            [status-im.ethereum.stateofus :as stateofus]
            [status-im.i18n :as i18n]
            [status-im.native-module.core :as native-module]
            [status-im.ui.screens.navigation :as navigation]
            [status-im.utils.build :as build]
            [status-im.utils.config :as config]
            [status-im.utils.fx :as fx]
            [status-im.utils.gfycat.core :as gfycat]
            [status-im.utils.platform :as platform]
            [status-im.utils.utils :as utils]))

(defn displayed-name [{:keys [ens-name nickname public-key]}]
  (or nickname
      (when ens-name
        (let [username (stateofus/username ens-name)]
          (str "@" (or username ens-name))))
      (gfycat/generate-gfy public-key)))

(re-frame/reg-fx
 ::chaos-mode-changed
 (fn [on]
   (native-module/chaos-mode-update on (constantly nil))))

(re-frame/reg-fx
 ::blank-preview-flag-changed
 (fn [flag]
   (native-module/set-blank-preview-flag flag)))

(fx/defn show-mainnet-is-default-alert [{:keys [db]}]
  (let [shown-version (get-in db [:account/account :mainnet-warning-shown-version])
        current-version build/version]
    (when (and platform/mobile?
               config/mainnet-warning-enabled?
               (not= shown-version current-version))
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
        (navigation/navigate-to-cofx cofx :wallet nil)
        (navigation/navigate-to-cofx cofx :wallet-send-transaction-modal nil)))))

(fx/defn confirm-wallet-set-up
  [{:keys [db] :as cofx} modal?]
  (fx/merge cofx
            (continue-after-wallet-onboarding modal?)
            (accounts.update/account-update {:wallet-set-up-passed? true} {})))

(fx/defn update-dev-server-state
  [_ dev-mode?]
  (if dev-mode?
    {:dev-server/start nil}
    {:dev-server/stop nil}))

(fx/defn update-extensions-state
  [{{:account/keys [account]} :db} dev-mode?]
  (let [extensions (-> account :extensions vals)]
    (if dev-mode?
      {:extensions/load {:extensions extensions
                         :follow-up  :extensions/add-to-registry}}
      {:dispatch [:extensions/disable-all-hooks extensions]})))

(fx/defn switch-dev-mode [cofx dev-mode?]
  (fx/merge cofx
            (update-dev-server-state dev-mode?)
            (update-extensions-state dev-mode?)
            (accounts.update/account-update {:dev-mode? dev-mode?}
                                            {})))

(fx/defn switch-chaos-mode [{:keys [db] :as cofx} chaos-mode?]
  (when (:account/account db)
    (let [settings (get-in db [:account/account :settings])]
      (fx/merge cofx
                {::chaos-mode-changed chaos-mode?}
                (accounts.update/update-settings
                 (assoc settings :chaos-mode? chaos-mode?)
                 {})))))

(fx/defn switch-biometric-auth
  {:events [:accounts.ui/switch-biometric-auth]}
  [{:keys [db] :as cofx} biometric-auth?]
  (when (:account/account db)
    (let [settings (get-in db [:account/account :settings])]
      (accounts.update/update-settings cofx
                                       (assoc settings :biometric-auth? biometric-auth?)
                                       {}))))

(fx/defn enable-notifications [cofx desktop-notifications?]
  (accounts.update/account-update cofx
                                  {:desktop-notifications? desktop-notifications?}
                                  {}))

(fx/defn toggle-device-to-device [{:keys [db] :as cofx} enabled?]
  (let [settings (get-in db [:account/account :settings])
        warning  {:utils/show-popup {:title (i18n/label :t/device-to-device-warning-title)
                                     :content (i18n/label :t/device-to-device-warning-content)}}]

    (fx/merge cofx
              (when enabled? warning)
              ;; Set to pfs? for backward compatibility
              (accounts.update/update-settings (assoc settings :pfs? enabled?)
                                               {}))))

(fx/defn switch-web3-opt-in-mode [{:keys [db] :as cofx} opt-in]
  (let [settings (get-in db [:account/account :settings])]
    (accounts.update/update-settings cofx
                                     (assoc settings :web3-opt-in? opt-in)
                                     {})))

(fx/defn switch-preview-privacy-mode [{:keys [db] :as cofx} private?]
  (let [settings (get-in db [:account/account :settings])]
    (fx/merge cofx
              {::blank-preview-flag-changed private?}
              (accounts.update/update-settings
               (assoc settings :preview-privacy? private?)
               {}))))

(fx/defn update-recent-stickers [cofx stickers]
  (accounts.update/account-update cofx
                                  {:recent-stickers stickers}
                                  {}))

(fx/defn update-stickers [cofx stickers]
  (accounts.update/account-update cofx
                                  {:stickers stickers}
                                  {}))
