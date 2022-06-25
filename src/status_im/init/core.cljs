(ns status-im.init.core
  (:require [clojure.string :as string]
            [re-frame.core :as re-frame]
            [status-im.multiaccounts.login.core :as multiaccounts.login]
            [status-im.native-module.core :as status]
            [status-im.network.net-info :as network]
            [status-im.db :refer [app-db]]
            [status-im.utils.fx :as fx]
            [status-im.theme.core :as theme]
            [status-im.utils.theme :as utils.theme]
            [status-im.utils.keychain.core :as keychain]
            [status-im.navigation :as navigation]
            [quo.design-system.colors :as colors]
            [status-im.utils.image-ring :as image-ring]))

(fx/defn initialize-app-db
  "Initialize db to initial state"
  [{{:keys [keycard supported-biometric-auth app-active-since goto-key-storage?]
     :network/keys [type] :keycard/keys [banner-hidden]} :db
    now :now}]
  {:db (assoc app-db
              :network/type type
              :keycard/banner-hidden banner-hidden
              :keycard (dissoc keycard :secrets :pin :application-info)
              :supported-biometric-auth supported-biometric-auth
              :app-active-since (or app-active-since now)
              :goto-key-storage? goto-key-storage?
              :multiaccounts/loading true)})

(fx/defn add-ring-to-identity-images
  {:events [::add-ring-to-identity-images]}
  [{:keys [db]} pics]
  {:db (assoc-in db [:multiaccounts/login :images] pics)})

(fx/defn draw-identity-ring-for-open-login
  [{:keys [db] :as cofx} color-hash pics]
  (let [current-theme (colors/current-theme)
        draw-ring-params (image-ring/to-draw-ring-params pics color-hash current-theme)
        callback (fn [ring-uris] (let [pics (map-indexed (fn [index item]
                                                           (assoc item :uri (get ring-uris index))) pics)]
                                   (re-frame/dispatch [::add-ring-to-identity-images pics])))]
    (status/draw-identity-image-ring draw-ring-params callback)))

(fx/defn initialize-views
  {:events [::initialize-view]}
  [cofx]
  (let [{{:multiaccounts/keys [multiaccounts]} :db} cofx]
    (if (and (seq multiaccounts))
      ;; We specifically pass a bunch of fields instead of the whole multiaccount
      ;; as we want store some fields in multiaccount that are not here
      (let [multiaccount (first (sort-by :timestamp > (vals multiaccounts)))
            selected-multiaccount (select-keys
                                   multiaccount
                                   [:key-uid :name :public-key :identicon :images])
            color-hash (:colorHash multiaccount)
            images (:images multiaccount)]
        (fx/merge cofx
                  (multiaccounts.login/open-login (select-keys
                                                   multiaccount
                                                   [:key-uid :name :public-key :identicon :images]))
                  (draw-identity-ring-for-open-login color-hash images)
                  (keychain/get-auth-method (:key-uid multiaccount))))
      (navigation/init-root cofx :intro))))

(fx/defn update-account-images
  {:events [::update-account-images]}
  [{:keys [db]} key-uid ring-uris]
  (let [accounts (:multiaccounts/multiaccounts db)
        images (:images (get accounts key-uid))
        ring-images (map-indexed (fn [index item]
                                   (assoc item :uri (get ring-uris index))) images)]
    {:db (assoc-in db [:multiaccounts/multiaccounts key-uid :images] ring-images)}))

(fx/defn draw-identity-ring-for-accounts
  {:events [::draw-identity-ring-for-accounts]}
  [cofx multiaccounts]
  (let [theme (colors/current-theme)
        draw-ring-params (reduce (fn [all-draw-ring-params account-entry]
                                   (let [account (val account-entry)
                                         images (:images account)
                                         color-hash (:colorHash account)
                                         draw-ring-params (image-ring/to-draw-ring-params images color-hash theme)]
                                     (apply conj all-draw-ring-params draw-ring-params))) [] multiaccounts)
        callback (fn [ring-uris] (reduce (fn [state account-entry]
                                           (let [key-uid (key account-entry)
                                                 account (val account-entry)
                                                 images-count (count (:images account))
                                                 start-index (:start-index state)
                                                 end-index (+ start-index images-count)
                                                 all-ring-uris (:uris state)
                                                 account-ring-uris (subvec all-ring-uris start-index end-index)]
                                             (re-frame/dispatch [::update-account-images key-uid account-ring-uris])
                                             (assoc state :start-index end-index)))
                                         {:uris ring-uris
                                          :start-index 0}
                                         multiaccounts))]
    (status/draw-identity-image-ring draw-ring-params callback)))

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
                            [::draw-identity-ring-for-accounts multiaccounts]]})))

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
