(ns status-im.init.core
  (:require [re-frame.core :as re-frame]
            [status-im.multiaccounts.login.core :as multiaccounts.login]
            [status-im.native-module.core :as status]
            [status-im.network.net-info :as network]
            [status-im.react-native.js-dependencies :as rn-dependencies]
            [status-im.ui.screens.db :refer [app-db]]
            [status-im.utils.fx :as fx]
            [status-im.utils.platform :as platform]
            [clojure.string :as string]
            [status-im.utils.theme :as theme]
            [status-im.ui.components.colors :as colors]))

(defn restore-native-settings! []
  (when platform/desktop?
    (.getValue rn-dependencies/desktop-config "logging_enabled"
               #(re-frame/dispatch [:set-in [:desktop/desktop :logging-enabled]
                                    (if (boolean? %)
                                      %
                                      (cljs.reader/read-string %))]))))

(fx/defn initialize-app-db
  "Initialize db to initial state"
  [{{:keys [hardwallet initial-props desktop/desktop
            supported-biometric-auth network/type app-active-since]} :db now :now}]
  {:db (assoc app-db
              :initial-props initial-props
              :desktop/desktop (merge desktop (:desktop/desktop app-db))
              :network/type type
              :hardwallet (dissoc hardwallet :secrets)
              :supported-biometric-auth supported-biometric-auth
              :app-active-since (or app-active-since now)
              :multiaccounts/loading true)})

(fx/defn initialize-views
  {:events [::initialize-view]}
  [cofx {:keys [logout?]}]
  (let [{{:multiaccounts/keys [multiaccounts] :as db} :db} cofx]
    (when (and (seq multiaccounts) (not logout?))
      (let [{:keys [key-uid public-key photo-path name]} (first (#(sort-by :last-sign-in > %) (vals multiaccounts)))]
        (multiaccounts.login/open-login cofx key-uid photo-path name public-key)))))

(fx/defn initialize-multiaccounts
  {:events [::initialize-multiaccounts]}
  [{:keys [db] :as cofx} all-multiaccounts {:keys [logout?]}]
  (let [multiaccounts (reduce (fn [acc {:keys [key-uid keycard-pairing]
                                        :as   multiaccount}]
                                (-> (assoc acc key-uid multiaccount)
                                    (assoc-in [key-uid :keycard-pairing]
                                              (when-not (string/blank? keycard-pairing)
                                                keycard-pairing))))
                              {}
                              all-multiaccounts)]
    (fx/merge cofx
              {:db             (-> db
                                   (assoc :multiaccounts/multiaccounts multiaccounts)
                                   (assoc :multiaccounts/logout? logout?)
                                   (assoc :multiaccounts/loading false))
               ;; NOTE: Try to dispatch later navigation because of that https://github.com/react-navigation/react-navigation/issues/6879
               :dispatch-later [{:dispatch [::initialize-view {:logout? logout?}]
                                 :ms       100}]})))

(fx/defn start-app [cofx]
  (fx/merge cofx
            {:get-supported-biometric-auth          nil
             ::init-theme                           nil
             ::init-keystore                        nil
             ::restore-native-settings              nil
             ::open-multiaccounts                   #(re-frame/dispatch [::initialize-multiaccounts % {:logout? false}])
             :ui/listen-to-window-dimensions-change nil
             ::network/listen-to-network-info       nil
             :hardwallet/register-card-events       nil
             :hardwallet/check-nfc-support          nil
             :hardwallet/check-nfc-enabled          nil
             :hardwallet/retrieve-pairings          nil}
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
 ::init-theme
 (fn []
   (theme/add-mode-change-listener #(re-frame/dispatch [:system-theme-mode-changed %]))
   (when (theme/is-dark-mode)
     (colors/set-theme :dark))))