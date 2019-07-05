(ns status-im.init.core
  (:require [re-frame.core :as re-frame]
            [status-im.multiaccounts.login.core :as multiaccounts.login]
            [status-im.multiaccounts.update.core :as multiaccounts.update]
            [status-im.browser.core :as browser]
            [status-im.chat.models :as chat-model]
            [status-im.contact.core :as contact]
            [status-im.ethereum.core :as ethereum]
            [status-im.extensions.module :as extensions.module]
            [status-im.i18n :as i18n]
            [status-im.models.dev-server :as models.dev-server]
            [status-im.native-module.core :as status]
            [status-im.notifications.core :as notifications]
            [status-im.pairing.core :as pairing]
            [status-im.react-native.js-dependencies :as rn-dependencies]
            [status-im.stickers.core :as stickers]
            [status-im.ui.screens.db :refer [app-db]]
            [status-im.ui.screens.navigation :as navigation]
            [status-im.utils.fx :as fx]
            [status-im.utils.keychain.core :as keychain]
            [status-im.utils.platform :as platform]
            [status-im.biometric-auth.core :as biometric-auth]
            [taoensso.timbre :as log]))

(defn restore-native-settings! []
  (when platform/desktop?
    (.getValue rn-dependencies/desktop-config "logging_enabled"
               #(re-frame/dispatch [:set-in [:desktop/desktop :logging-enabled]
                                    (if (boolean? %1)
                                      %1 (cljs.reader/read-string %1))]))))

(fx/defn initialize-app-db
  "Initialize db to initial state"
  [{{:keys      [view-id hardwallet
                 initial-props desktop/desktop
                 network-status network peers-count peers-summary device-UUID
                 supported-biometric-auth push-notifications/stored network/type]
     :node/keys [status]
     :or        {network (get app-db :network)}} :db}]
  {:db (assoc app-db
              :contacts/contacts {}
              :initial-props initial-props
              :desktop/desktop (merge desktop (:desktop/desktop app-db))
              :network-status network-status
              :peers-count (or peers-count 0)
              :peers-summary (or peers-summary [])
              :node/status status
              :network network
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

(fx/defn load-multiaccounts [{:keys [db all-multiaccounts]}]
  (let [multiaccounts (->> all-multiaccounts
                           (map (fn [{:keys [address] :as multiaccount}]
                                  [address multiaccount]))
                           (into {}))]
    {:db (assoc db :multiaccounts/multiaccounts multiaccounts)}))

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
            {:keys [address public-key photo-path name accounts]} (first (selection-fn (vals multiaccounts)))]
        (multiaccounts.login/open-login cofx address photo-path name public-key accounts)))))

(fx/defn start-app [cofx]
  (fx/merge cofx
            {:init/get-device-UUID                  nil
             :init/get-supported-biometric-auth     nil
             :init/restore-native-settings          nil
             :ui/listen-to-window-dimensions-change nil
             :notifications/init                    nil
             :network/listen-to-network-status      nil
             :network/listen-to-connection-status   nil
             :hardwallet/register-card-events       nil
             :hardwallet/check-nfc-support nil
             :hardwallet/check-nfc-enabled nil}
            (initialize-app-db)
            (load-multiaccounts)
            (initialize-views)))

(fx/defn initialize-multiaccount-db [{:keys [db web3]} address]
  (let [{:universal-links/keys [url]
         :keys                 [multiaccounts/multiaccounts multiaccounts/create networks/networks network
                                network-status peers-count peers-summary view-id navigation-stack
                                mailserver/mailservers
                                intro-wizard
                                desktop/desktop hardwallet custom-fleets supported-biometric-auth
                                device-UUID semaphores multiaccounts/login]
         :node/keys            [status on-ready]
         :or                   {network (get app-db :network)}} db
        current-multiaccount (get multiaccounts address)
        multiaccount-network-id (get current-multiaccount :network network)
        multiaccount-network (get-in current-multiaccount [:networks multiaccount-network-id])]
    {:db (cond-> (assoc app-db
                        :view-id view-id
                        :navigation-stack navigation-stack
                        :node/status status
                        :intro-wizard intro-wizard
                        :node/on-ready on-ready
                        :multiaccounts/create create
                        :desktop/desktop (merge desktop (:desktop/desktop app-db))
                        :networks/networks networks
                        :multiaccount current-multiaccount
                        :multiaccounts/login login
                        :multiaccounts/multiaccounts multiaccounts
                        :mailserver/mailservers mailservers
                        :network-status network-status
                        :network multiaccount-network-id
                        :network/type (:network/type db)
                        :chain (ethereum/network->chain-name multiaccount-network)
                        :universal-links/url url
                        :custom-fleets custom-fleets
                        :peers-summary peers-summary
                        :peers-count peers-count
                        :device-UUID device-UUID
                        :supported-biometric-auth supported-biometric-auth
                        :semaphores semaphores
                        :hardwallet hardwallet
                        :web3 web3)
           (= view-id :create-multiaccount)
           (assoc-in [:multiaccounts/create :step] :enter-name))}))

(defn login-only-events [{:keys [db] :as cofx} address stored-pns]
  (fx/merge cofx
            (when-not (:intro-wizard db)
              (cond-> {:notifications/request-notifications-permissions nil}
                platform/ios?
                ;; on ios navigation state might be not initialized yet when
                ;; navigate-to call happens.
                ;; That's why it should be delayed a bit.
                ;; TODO(rasom): revisit this later and find better solution
                (assoc :dispatch-later
                       [{:ms       1
                         :dispatch [:navigate-to :home]}])))
            (when-not (or (:intro-wizard db) platform/ios?)
              (navigation/navigate-to-cofx :home nil))
            (notifications/process-stored-event address stored-pns)
            (when platform/desktop?
              (chat-model/update-dock-badge-label))))

(defn dev-mode? [cofx]
  (get-in cofx [:db :multiaccount :dev-mode?]))

(defn creating-multiaccount? [cofx]
  (= (get-in cofx [:db :view-id])
     :create-multiaccount))

(defn- keycard-setup? [cofx]
  (boolean (get-in cofx [:db :hardwallet :flow])))

(fx/defn initialize-multiaccount [{:keys [db] :as cofx} address]
  (let [stored-pns (:push-notifications/stored db)]
    (fx/merge cofx
              {:notifications/get-fcm-token nil}
              (initialize-multiaccount-db address)
              (contact/load-contacts)
              #(when (dev-mode? %)
                 (models.dev-server/start))
              (extensions.module/initialize)
              (stickers/init-stickers-packs)
              (multiaccounts.update/update-sign-in-time)
              #(when-not (or (creating-multiaccount? %)
                             (keycard-setup? %))
                 (login-only-events % address stored-pns)))))

(re-frame/reg-fx
 :init/restore-native-settings
 restore-native-settings!)

(re-frame/reg-fx
 :init/get-device-UUID
 (fn []
   (status/get-device-UUID #(re-frame/dispatch [:init.callback/get-device-UUID-success %]))))

(re-frame/reg-fx
 :init/get-supported-biometric-auth
 (fn []
   (biometric-auth/get-supported #(re-frame/dispatch [:init.callback/get-supported-biometric-auth-success %]))))
