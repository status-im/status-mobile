(ns legacy.status-im.events
  (:require
    clojure.set
    legacy.status-im.backup.core
    legacy.status-im.bootnodes.core
    legacy.status-im.browser.core
    legacy.status-im.browser.permissions
    legacy.status-im.chat.models.loading
    legacy.status-im.contact.block
    legacy.status-im.currency.core
    legacy.status-im.ethereum.subscriptions
    legacy.status-im.fleet.core
    [legacy.status-im.keycard.core :as keycard]
    legacy.status-im.log-level.core
    legacy.status-im.mailserver.constants
    [legacy.status-im.mailserver.core :as mailserver]
    legacy.status-im.multiaccounts.login.core
    legacy.status-im.multiaccounts.logout.core
    [legacy.status-im.multiaccounts.model :as multiaccounts.model]
    legacy.status-im.multiaccounts.update.core
    legacy.status-im.network.net-info
    legacy.status-im.pairing.core
    legacy.status-im.profile.core
    legacy.status-im.search.core
    legacy.status-im.stickers.core
    legacy.status-im.ui.components.invite.events
    [legacy.status-im.ui.components.react :as react]
    legacy.status-im.ui.screens.notifications-settings.events
    legacy.status-im.ui.screens.privacy-and-security-settings.events
    [legacy.status-im.utils.dimensions :as dimensions]
    legacy.status-im.utils.logging.core
    [legacy.status-im.utils.utils :as utils]
    legacy.status-im.visibility-status-popover.core
    legacy.status-im.visibility-status-updates.core
    legacy.status-im.waku.core
    legacy.status-im.wallet.accounts.core
    legacy.status-im.wallet.choose-recipient.core
    [legacy.status-im.wallet.core :as wallet]
    legacy.status-im.wallet.custom-tokens.core
    [native-module.core :as native-module]
    [re-frame.core :as re-frame]
    [react-native.core :as rn]
    [react-native.permissions :as permissions]
    [react-native.platform :as platform]
    [status-im2.common.biometric.events :as biometric]
    status-im2.common.standard-authentication.events
    [status-im2.common.theme.core :as theme]
    [status-im2.common.universal-links :as universal-links]
    [status-im2.constants :as constants]
    status-im2.contexts.chat.home.events
    status-im2.contexts.communities.home.events
    status-im2.contexts.onboarding.events
    status-im2.contexts.shell.activity-center.events
    status-im2.contexts.shell.activity-center.notification.contact-requests.events
    status-im2.contexts.shell.jump-to.events
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(re-frame/reg-fx
 :dismiss-keyboard
 (fn []
   (react/dismiss-keyboard!)))

(re-frame/reg-fx
 :request-permissions-fx
 (fn [options]
   (permissions/request-permissions options)))

(re-frame/reg-fx
 :ui/show-error
 (fn [content]
   (utils/show-popup "Error" content)))

(re-frame/reg-fx
 :ui/show-confirmation
 (fn [options]
   (utils/show-confirmation options)))

(re-frame/reg-fx
 :ui/close-application
 (fn [_]
   (native-module/close-application)))

(re-frame/reg-fx
 ::app-state-change-fx
 (fn [state]
   (when (and platform/ios? (= state "active"))
     ;; Change the app theme if the ios device theme was updated when the app was in the background
     ;; https://github.com/status-im/status-mobile/issues/15708
     (theme/change-device-theme (rn/get-color-scheme)))
   (native-module/app-state-change state)))

(re-frame/reg-fx
 :ui/listen-to-window-dimensions-change
 (fn []
   (dimensions/add-event-listener)))

(rf/defn dismiss-keyboard
  {:events [:dismiss-keyboard]}
  [_]
  {:dismiss-keyboard nil})

(rf/defn gfycat-generated
  {:events [:gfycat-generated]}
  [{:keys [db]} path gfycat]
  {:db (assoc-in db path gfycat)})

(rf/defn system-theme-mode-changed
  {:events [:system-theme-mode-changed]}
  [{:keys [db] :as cofx} _]
  (let [current-theme-type (get-in cofx [:db :profile/profile :appearance])]
    (when (and (multiaccounts.model/logged-in? db)
               (= current-theme-type status-im2.constants/theme-type-system))
      {:profile.settings/switch-theme-fx
       [(get-in db [:profile/profile :appearance])
        (:view-id db) true]})))

(defn- on-biometric-auth-fail
  [{:keys [code]}]
  (if (= code "USER_FALLBACK")
    (re-frame/dispatch [:multiaccounts.logout.ui/logout-confirmed])
    (utils/show-confirmation
     {:title               (i18n/label :t/biometric-auth-confirm-title)
      :content             (i18n/label :t/biometric-auth-confirm-message)
      :confirm-button-text (i18n/label :t/biometric-auth-confirm-try-again)
      :cancel-button-text  (i18n/label :t/biometric-auth-confirm-logout)
      :on-accept           #(biometric/authenticate nil {:on-fail on-biometric-auth-fail})
      :on-cancel           #(re-frame/dispatch [:multiaccounts.logout.ui/logout-confirmed])})))

(rf/defn on-return-from-background
  [{:keys [db now] :as cofx}]
  (let [new-account?            (get db :onboarding/new-account?)
        app-in-background-since (get db :app-in-background-since)
        signed-up?              (get-in db [:profile/profile :signed-up?])
        requires-bio-auth       (and
                                 signed-up?
                                 (= (:auth-method db) "biometric")
                                 (some? app-in-background-since)
                                 (>= (- now app-in-background-since)
                                     constants/ms-in-bg-for-require-bioauth))]
    (rf/merge cofx
              {:db (dissoc db :app-in-background-since)}
              (mailserver/process-next-messages-request)
              (wallet/restart-wallet-service-after-background app-in-background-since)
              (when-not new-account?
                (universal-links/process-stored-event))
              #(when-let [chat-id (:current-chat-id db)]
                 {:dispatch [:chat/mark-all-as-read chat-id]})
              #(when requires-bio-auth
                 (biometric/authenticate % {:on-fail on-biometric-auth-fail})))))

(rf/defn on-going-in-background
  [{:keys [db now]}]
  {:db (-> db
           (assoc :app-in-background-since now)
           (dissoc :universal-links/handling))})
   ;; event not implemented
   ;; :dispatch-n [[:audio-recorder/on-background] [:audio-message/on-background]]


(rf/defn app-state-change
  {:events [:app-state-change]}
  [{:keys [db] :as cofx} state]
  (let [app-coming-from-background? (= state "active")
        app-going-in-background?    (= state "background")]
    (rf/merge cofx
              {::app-state-change-fx state
               :db                   (assoc db :app-state state)}
              #(when app-coming-from-background?
                 (on-return-from-background %))
              #(when app-going-in-background?
                 (on-going-in-background %)))))

(rf/defn request-permissions
  {:events [:request-permissions]}
  [_ options]
  {:request-permissions-fx options})

(rf/defn update-window-dimensions
  {:events [:update-window-dimensions]}
  [{:keys [db]} dimensions]
  {:db (assoc db :dimensions/window (dimensions/window dimensions))})

(rf/defn on-will-focus
  {:events [:screens/on-will-focus]}
  [{:keys [db] :as cofx} view-id]
  (rf/merge cofx
            (cond
              (= :chat view-id)
              {:effects.async-storage/set {:chat-id (get-in cofx [:db :current-chat-id])
                                           :key-uid (get-in cofx [:db :profile/profile :key-uid])}
               :db                        (assoc db :screens/was-focused-once? true)}

              (not (get db :screens/was-focused-once?))
              {:db (assoc db :screens/was-focused-once? true)})
            #(case view-id
               :keycard-settings              (keycard/settings-screen-did-load %)
               :reset-card                    (keycard/reset-card-screen-did-load %)
               :enter-pin-settings            (keycard/enter-pin-screen-did-load %)
               :keycard-login-pin             (keycard/login-pin-screen-did-load %)
               :add-new-account-pin           (keycard/enter-pin-screen-did-load %)
               :keycard-authentication-method (keycard/authentication-method-screen-did-load %)
               :multiaccounts                 (keycard/multiaccounts-screen-did-load %)
               :wallet-legacy                 (wallet/wallet-will-focus %)
               nil)))

;;TODO :replace by named events
(rf/defn set-event
  {:events [:set]}
  [{:keys [db]} k v]
  {:db (assoc db k v)})

;;TODO :replace by named events
(rf/defn set-once-event
  {:events [:set-once]}
  [{:keys [db]} k v]
  (when-not (get db k)
    {:db (assoc db k v)}))

;;TODO :replace by named events
(rf/defn set-in-event
  {:events [:set-in]}
  [{:keys [db]} path v]
  {:db (assoc-in db path v)})

(defn on-ramp<-rpc
  [on-ramp]
  (clojure.set/rename-keys on-ramp
                           {:logoUrl :logo-url
                            :siteUrl :site-url}))

(rf/defn crypto-loaded-event
  {:events [::crypto-loaded]}
  [{:keys [db]} on-ramps]
  {:db (assoc
        db
        :buy-crypto/on-ramps
        (map on-ramp<-rpc on-ramps))})

(rf/defn buy-crypto-ui-loaded
  {:events [:buy-crypto.ui/loaded]}
  [_]
  {:json-rpc/call [{:method     "wallet_getCryptoOnRamps"
                    :params     []
                    :on-success (fn [on-ramps]
                                  (re-frame/dispatch [::crypto-loaded on-ramps]))}]})

(re-frame/reg-event-fx :buy-crypto.ui/open-screen
 (fn []
   {:fx [[:dispatch [:wallet-legacy/keep-watching]]
         [:dispatch [:open-modal :buy-crypto nil]]]}))
