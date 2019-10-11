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
            [status-im.utils.handlers]
            [status-im.utils.gfycat.core :as gfycat]
            [status-im.utils.identicon :as identicon]
            [status-im.utils.platform :as platform]
            [status-im.utils.utils :as utils]))

; Whether we should be strict about verifying ens, currently disabled as
; status-go can't be upgrade because of geth 1.9 incompatibility
(def only-verified-ens false)
(defn displayed-name
  "Use preferred name, name or alias in that order"
  [{:keys [name preferred-name alias public-key ens-verified]}]
  (let [ens-name (or preferred-name
                     name)]
    ;; Preferred name is our own
    ;; otherwise we make sure is verified
    (if (or preferred-name
            (and only-verified-ens
                 ens-verified
                 name))
      (let [username (stateofus/username ens-name)]
        (str "@" (or username ens-name)))
      (or alias (gfycat/generate-gfy public-key)))))

(defn displayed-photo
  "If a photo-path is set use it, otherwise fallback on identicon or generate"
  [{:keys [photo-path identicon public-key]}]
  (or photo-path
      identicon
      (identicon/identicon public-key)))

(re-frame/reg-fx
 ::chaos-mode-changed
 (fn [on]
   (native-module/chaos-mode-update on (constantly nil))))

(re-frame/reg-fx
 ::blank-preview-flag-changed
 (fn [flag]
   (native-module/set-blank-preview-flag flag)))

(defn- chat-send? [transaction]
  (and (seq transaction)
       (not (:in-progress? transaction))
       (:from-chat? transaction)))

(fx/defn confirm-wallet-set-up
  [cofx]
  (multiaccounts.update/multiaccount-update cofx
                                            {:wallet-set-up-passed? true} {}))

(fx/defn switch-dev-mode
  [cofx dev-mode?]
  (multiaccounts.update/multiaccount-update cofx
                                            {:dev-mode? dev-mode?}
                                            {}))

(fx/defn switch-chaos-mode
  [{:keys [db] :as cofx} chaos-mode?]
  (when (:multiaccount db)
    (fx/merge cofx
              {::chaos-mode-changed chaos-mode?}
              (multiaccounts.update/multiaccount-update {:chaos-mode? chaos-mode?}
                                                        {}))))

(fx/defn enable-notifications [cofx desktop-notifications?]
  (multiaccounts.update/multiaccount-update cofx
                                            {:desktop-notifications? desktop-notifications?}
                                            {}))

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
