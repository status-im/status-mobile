(ns status-im.multiaccounts.core
  (:require [re-frame.core :as re-frame]
            [status-im.ethereum.ens :as ens]
            [status-im.ethereum.stateofus :as stateofus]
            [status-im.i18n :as i18n]
            [status-im.multiaccounts.update.core :as multiaccounts.update]
            [status-im.native-module.core :as native-module]
            [status-im.notifications.core :as notifications]
            [status-im.utils.build :as build]
            [status-im.utils.config :as config]
            [status-im.utils.fx :as fx]
            [status-im.utils.handlers]
            [status-im.utils.gfycat.core :as gfycat]
            [status-im.utils.identicon :as identicon]
            [status-im.utils.platform :as platform]
            [status-im.utils.utils :as utils]))

(defn displayed-name
  "Use preferred name, name or alias in that order"
  [{:keys [name preferred-name alias public-key ens-verified]}]
  (let [ens-name (or preferred-name
                     name)]
    ;; Preferred name is our own otherwise we make sure it's verified
    (if (or preferred-name (and ens-verified name))
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

(fx/defn confirm-home-tooltip
  [cofx]
  (multiaccounts.update/multiaccount-update cofx
                                            {:hide-home-tooltip? true} {}))

(fx/defn switch-dev-mode
  [cofx dev-mode?]
  (multiaccounts.update/multiaccount-update cofx
                                            {:dev-mode? dev-mode?}
                                            {}))

(fx/defn switch-notifications
  {:events [:multiaccounts.ui/notifications-switched]}
  [cofx notifications-enabled?]
  (fx/merge cofx
            {(if notifications-enabled?
               ::notifications/enable
               ::notifications/disable) nil}
            (multiaccounts.update/multiaccount-update
             {:notifications-enabled? notifications-enabled?}
             {})))

(fx/defn switch-chaos-mode
  [{:keys [db] :as cofx} chaos-mode?]
  (when (:multiaccount db)
    (fx/merge cofx
              {::chaos-mode-changed chaos-mode?}
              (multiaccounts.update/multiaccount-update
               {:chaos-mode? chaos-mode?}
               {}))))

(fx/defn enable-notifications [cofx desktop-notifications?]
  (multiaccounts.update/multiaccount-update
   cofx
   {:desktop-notifications? desktop-notifications?}
   {}))

(fx/defn switch-preview-privacy-mode
  [{:keys [db] :as cofx} private?]
  (let [settings (get-in db [:multiaccount :settings])]
    (fx/merge cofx
              {::blank-preview-flag-changed private?}
              (multiaccounts.update/update-settings
               (assoc settings :preview-privacy? private?)
               {}))))

(fx/defn switch-preview-privacy-mode-flag
  [{:keys [db]}]
  (let [private? (get-in db [:multiaccount :settings :preview-privacy?])]
    {::blank-preview-flag-changed private?}))
