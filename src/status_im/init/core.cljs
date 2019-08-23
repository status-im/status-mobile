(ns status-im.init.core
  (:require [re-frame.core :as re-frame]
            [status-im.biometric-auth.core :as biometric-auth]
            [status-im.data-store.core :as data-store]
            [status-im.multiaccounts.login.core :as multiaccounts.login]
            [status-im.native-module.core :as status]
            [status-im.notifications.core :as notifications]
            [status-im.react-native.js-dependencies :as rn-dependencies]
            [status-im.ui.screens.db :refer [app-db]]
            [status-im.ui.screens.navigation :as navigation]
            [status-im.utils.fx :as fx]
            [status-im.utils.platform :as platform]))

(defn restore-native-settings! []
  (when platform/desktop?
    (.getValue rn-dependencies/desktop-config "logging_enabled"
               #(re-frame/dispatch [:set-in [:desktop/desktop :logging-enabled]
                                    (if (boolean? %)
                                      %
                                      (cljs.reader/read-string %))]))))

(fx/defn initialize-app-db
  "Initialize db to initial state"
  [{{:keys [view-id hardwallet initial-props desktop/desktop
            device-UUID supported-biometric-auth push-notifications/stored network/type]} :db}]
  {:db (assoc app-db
              :initial-props initial-props
              :desktop/desktop (merge desktop (:desktop/desktop app-db))
              :network/type type
              :hardwallet hardwallet
              :device-UUID device-UUID
              :supported-biometric-auth supported-biometric-auth
              :view-id view-id
              :push-notifications/stored stored)})

(fx/defn set-device-uuid
  [{:keys [db]} device-uuid]
  {:db (assoc db :device-UUID device-uuid)})

(fx/defn set-supported-biometric-auth
  {:events [:init.callback/get-supported-biometric-auth-success]}
  [{:keys [db]} supported-biometric-auth]
  {:db (assoc db :supported-biometric-auth supported-biometric-auth)})

(fx/defn initialize-views
  [cofx]
  (let [{{:multiaccounts/keys [multiaccounts] :as db} :db} cofx]
    (if (empty? multiaccounts)
      (navigation/navigate-to-cofx cofx :intro nil)
      (let [multiaccount-with-notification
            (when-not platform/desktop?
              (notifications/lookup-contact-pubkey-from-hash
               cofx
               (first (keys (:push-notifications/stored db)))))
            selection-fn
            (if (not-empty multiaccount-with-notification)
              #(filter (fn [multiaccount]
                         (= multiaccount-with-notification
                            (:public-key multiaccount)))
                       %)
              #(sort-by :last-sign-in > %))
            {:keys [address public-key photo-path name]} (first (selection-fn (vals multiaccounts)))]
        (multiaccounts.login/open-login cofx address photo-path name public-key)))))

(fx/defn initialize-multiaccounts
  {:events [::initialize-multiaccounts]}
  [{:keys [db] :as cofx} all-multiaccounts]
  (let [multiaccounts (reduce (fn [acc {:keys [address] :as multiaccount}]
                                (assoc acc address multiaccount))
                              {}
                              all-multiaccounts)]
    (fx/merge cofx
              {:db (assoc db :multiaccounts/multiaccounts multiaccounts)}
              (initialize-views))))

(fx/defn start-app [cofx]
  (fx/merge cofx
            {::get-device-UUID nil
             ::get-supported-biometric-auth nil
             ::init-keystore nil
             ::restore-native-settings nil
             ::open-multiaccounts #(re-frame/dispatch [::initialize-multiaccounts %])
             ::data-store/init-store nil
             :ui/listen-to-window-dimensions-change nil
             :notifications/init                    nil
             :network/listen-to-network-status      nil
             :network/listen-to-connection-status   nil
             :hardwallet/register-card-events       nil
             :hardwallet/check-nfc-support nil
             :hardwallet/check-nfc-enabled nil}
            (initialize-app-db)))

(re-frame/reg-fx
 ::restore-native-settings
 restore-native-settings!)

(re-frame/reg-fx
 ::open-multiaccounts
 (fn [callback]
   (status/open-accounts callback)))

(re-frame/reg-fx
 ::init-keystore
 (fn []
   (status/init-keystore)))

(re-frame/reg-fx
 ::get-device-UUID
 (fn []
   (status/get-device-UUID #(re-frame/dispatch [:init.callback/get-device-UUID-success %]))))

(re-frame/reg-fx
 ::get-supported-biometric-auth
 (fn []
   (biometric-auth/get-supported #(re-frame/dispatch [:init.callback/get-supported-biometric-auth-success %]))))
