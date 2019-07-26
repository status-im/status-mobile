(ns status-im.multiaccounts.core
  (:require [re-frame.core :as re-frame]
            [status-im.ethereum.ens :as ens]
            [status-im.ethereum.stateofus :as stateofus]
            [status-im.i18n :as i18n]
            [status-im.multiaccounts.update.core :as multiaccounts.update]
            [status-im.native-module.core :as native-module]
            [status-im.utils.build :as build]
            [status-im.utils.config :as config]
            [status-im.utils.fx :as fx]
            [status-im.utils.gfycat.core :as gfycat]
            [status-im.utils.platform :as platform]
            [status-im.utils.utils :as utils]
            [status-im.utils.handlers :as handlers]))

(defn displayed-name [account]
  (let [name (or (:preferred-name account) (:name account))]
    (if (ens/is-valid-eth-name? name)
      (let [username (stateofus/username name)]
        (str "@" (or username name)))
      (or name (gfycat/generate-gfy (:public-key account))))))

(re-frame/reg-fx
 ::chaos-mode-changed
 (fn [on]
   (native-module/chaos-mode-update on (constantly nil))))

(re-frame/reg-fx
 ::blank-preview-flag-changed
 (fn [flag]
   (native-module/set-blank-preview-flag flag)))

(fx/defn show-mainnet-is-default-alert [{:keys [db]}]
  (let [shown-version (get-in db [:multiaccount :mainnet-warning-shown-version])
        current-version build/version]
    (when (and platform/mobile?
               config/mainnet-warning-enabled?
               (not= shown-version current-version))
      (utils/show-popup
       (i18n/label :mainnet-is-default-alert-title)
       (i18n/label :mainnet-is-default-alert-text)
       #(re-frame/dispatch [:multiaccounts.ui/mainnet-warning-shown])))))

(defn- chat-send? [transaction]
  (and (seq transaction)
       (not (:in-progress? transaction))
       (:from-chat? transaction)))

(fx/defn confirm-wallet-set-up
  [cofx]
  (multiaccounts.update/multiaccount-update cofx {:wallet-set-up-passed? true} {}))

(fx/defn update-dev-server-state
  [_ dev-mode?]
  (if dev-mode?
    {:dev-server/start nil}
    {:dev-server/stop nil}))

(fx/defn update-extensions-state
  [{{:keys [multiaccount]} :db} dev-mode?]
  (let [extensions (-> multiaccount :extensions vals)]
    (if dev-mode?
      {:extensions/load {:extensions extensions
                         :follow-up  :extensions/add-to-registry}}
      {:dispatch [:extensions/disable-all-hooks extensions]})))

(fx/defn switch-dev-mode
  [cofx dev-mode?]
  (fx/merge cofx
            (update-dev-server-state dev-mode?)
            (update-extensions-state dev-mode?)
            (multiaccounts.update/multiaccount-update {:dev-mode? dev-mode?}
                                                      {})))

(fx/defn switch-chaos-mode
  [{:keys [db] :as cofx} chaos-mode?]
  (when (:multiaccount db)
    (let [settings (get-in db [:multiaccount :settings])]
      (fx/merge cofx
                {::chaos-mode-changed chaos-mode?}
                (multiaccounts.update/update-settings
                 (assoc settings :chaos-mode? chaos-mode?)
                 {})))))

(fx/defn switch-biometric-auth
  {:events [:multiaccounts.ui/switch-biometric-auth]}
  [{:keys [db] :as cofx} biometric-auth?]
  (when (:multiaccount db)
    (let [settings (get-in db [:multiaccount :settings])]
      (multiaccounts.update/update-settings cofx
                                            (assoc settings :biometric-auth? biometric-auth?)
                                            {}))))

(fx/defn enable-notifications [cofx desktop-notifications?]
  (multiaccounts.update/multiaccount-update cofx
                                            {:desktop-notifications? desktop-notifications?}
                                            {}))

(fx/defn toggle-device-to-device
  [{:keys [db] :as cofx} enabled?]
  (let [settings (get-in db [:multiaccount :settings])
        warning  {:utils/show-popup {:title (i18n/label :t/device-to-device-warning-title)
                                     :content (i18n/label :t/device-to-device-warning-content)}}]

    (fx/merge cofx
              (when enabled? warning)
              ;; Set to pfs? for backward compatibility
              (multiaccounts.update/update-settings (assoc settings :pfs? enabled?)
                                                    {}))))

(fx/defn toggle-datasync
  [{:keys [db] :as cofx} enabled?]
  (let [settings (get-in db [:multiaccount :settings])
        warning  {:utils/show-popup {:title (i18n/label :t/datasync-warning-title)
                                     :content (i18n/label :t/datasync-warning-content)}}]

    (fx/merge cofx
              (when enabled? warning)
              (multiaccounts.update/update-settings (assoc settings :datasync? enabled?)
                                                    {}))))

(fx/defn toggle-v1-messages
  [{:keys [db] :as cofx} enabled?]
  (let [settings (get-in db [:multiaccount :settings])
        warning  {:utils/show-popup {:title (i18n/label :t/v1-messages-warning-title)
                                     :content (i18n/label :t/v1-messages-warning-content)}}]

    (fx/merge cofx
              (when enabled? warning)
              (multiaccounts.update/update-settings (assoc settings :v1-messages? enabled?)
                                                    {}))))

(fx/defn toggle-disable-discovery-topic
  [{:keys [db] :as cofx} enabled?]
  (let [settings (get-in db [:multiaccount :settings])
        warning  {:utils/show-popup {:title (i18n/label :t/disable-discovery-topic-warning-title)
                                     :content (i18n/label :t/disable-discovery-topic-warning-content)}}]

    (fx/merge cofx
              (when enabled? warning)
              (multiaccounts.update/update-settings (assoc settings :disable-discovery-topic? enabled?)
                                                    {}))))

(fx/defn switch-web3-opt-in-mode
  [{:keys [db] :as cofx} opt-in]
  (let [settings (get-in db [:multiaccount :settings])]
    (multiaccounts.update/update-settings cofx
                                          (assoc settings :web3-opt-in? opt-in)
                                          {})))

(fx/defn switch-preview-privacy-mode
  [{:keys [db] :as cofx} private?]
  (let [settings (get-in db [:multiaccount :settings])]
    (fx/merge cofx
              {::blank-preview-flag-changed private?}
              (multiaccounts.update/update-settings
               (assoc settings :preview-privacy? private?)
               {}))))

(fx/defn update-recent-stickers [cofx stickers]
  (multiaccounts.update/multiaccount-update cofx
                                            {:recent-stickers stickers}
                                            {}))

(fx/defn update-stickers [cofx stickers]
  (multiaccounts.update/multiaccount-update cofx
                                            {:stickers stickers}
                                            {}))
