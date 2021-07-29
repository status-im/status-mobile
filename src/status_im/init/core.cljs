(ns status-im.init.core
  (:require [clojure.string :as string]
            [re-frame.core :as re-frame]
            [status-im.multiaccounts.login.core :as multiaccounts.login]
            [status-im.anon-metrics.core :as anon-metrics]
            [status-im.native-module.core :as status]
            [status-im.network.net-info :as network]
            [status-im.db :refer [app-db]]
            [status-im.utils.fx :as fx]
            [status-im.theme.core :as theme]
            [status-im.utils.theme :as utils.theme]
            [status-im.utils.keychain.core :as keychain]
            [status-im.navigation :as navigation]))

(fx/defn initialize-app-db
  "Initialize db to initial state"
  [{{:keys [keycard supported-biometric-auth app-active-since goto-key-storage?]
     :network/keys [type] :keycard/keys [banner-hidden]} :db
    now :now}]
  {:db (assoc app-db
              :network/type type
              :keycard/banner-hidden banner-hidden
              :keycard (dissoc keycard :secrets)
              :supported-biometric-auth supported-biometric-auth
              :app-active-since (or app-active-since now)
              :goto-key-storage? goto-key-storage?
              :multiaccounts/loading true)})

(fx/defn initialize-views
  {:events [::initialize-view]}
  [cofx]
  (let [{{:multiaccounts/keys [multiaccounts]} :db} cofx]
    (if (and (seq multiaccounts))
      ;; We specifically pass a bunch of fields instead of the whole multiaccount
      ;; as we want store some fields in multiaccount that are not here
      (let [multiaccount (first (sort-by :timestamp > (vals multiaccounts)))]
        (fx/merge cofx
                  (multiaccounts.login/open-login (select-keys
                                                   multiaccount
                                                   [:key-uid :name :public-key :identicon :images]))
                  (keychain/get-auth-method (:key-uid multiaccount))))
      (navigation/init-root cofx :intro))))

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
              {:db       (-> db
                             (assoc :multiaccounts/multiaccounts multiaccounts)
                             (assoc :multiaccounts/logout? logout?)
                             (assoc :multiaccounts/loading false))
               :dispatch-n [[::initialize-view]
                            [:get-opted-in-to-new-terms-of-service]
                            [::anon-metrics/fetch-opt-in-screen-displayed?]]})))

(fx/defn start-app
  {:events [:init/app-started]}
  [cofx]
  (fx/merge cofx
            {:get-supported-biometric-auth          nil
             ::init-theme                           nil
             ::open-multiaccounts                   #(do
                                                       (re-frame/dispatch [::initialize-multiaccounts % {:logout? false}])
                                                       (re-frame/dispatch [:get-keycard-banner-preference]))
             :ui/listen-to-window-dimensions-change nil
             ::network/listen-to-network-info       nil
             :keycard/register-card-events          nil
             :keycard/check-nfc-support             nil
             :keycard/check-nfc-enabled             nil
             :keycard/retrieve-pairings             nil}
            (initialize-app-db)))

(re-frame/reg-fx
 ::open-multiaccounts
 (fn [callback]
   (status/open-accounts callback)))

(re-frame/reg-fx
 ::init-theme
 (fn []
   (utils.theme/add-mode-change-listener #(re-frame/dispatch [:system-theme-mode-changed %]))
   (theme/change-theme (if (utils.theme/is-dark-mode) :dark :light))))
