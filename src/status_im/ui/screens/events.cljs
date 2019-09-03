(ns status-im.ui.screens.events
  (:require status-im.events
            status-im.ui.screens.add-new.events
            status-im.ui.screens.add-new.new-chat.events
            status-im.ui.screens.group.chat-settings.events
            status-im.ui.screens.group.events
            status-im.utils.universal-links.events
            status-im.ui.screens.add-new.new-chat.navigation
            status-im.ui.screens.profile.events
            status-im.ui.screens.wallet.navigation
            [re-frame.core :as re-frame]
            [status-im.chat.models :as chat]
            [status-im.hardwallet.core :as hardwallet]
            [status-im.mailserver.core :as mailserver]
            [status-im.multiaccounts.recover.core :as recovery]
            [status-im.native-module.core :as status]
            [status-im.ui.components.permissions :as permissions]
            [status-im.utils.dimensions :as dimensions]
            [status-im.utils.fx :as fx]
            [status-im.utils.handlers :as handlers]
            [status-im.utils.http :as http]
            [status-im.utils.utils :as utils]
            [status-im.i18n :as i18n]
            [status-im.constants :as const]
            [status-im.multiaccounts.biometric.core :as biometric]))

(defn- http-get [{:keys [url response-validator success-event-creator failure-event-creator timeout-ms]}]
  (let [on-success #(re-frame/dispatch (success-event-creator %))
        on-error   (when failure-event-creator #(re-frame/dispatch (failure-event-creator %)))
        opts       {:valid-response? response-validator
                    :timeout-ms      timeout-ms}]
    (http/get url on-success on-error opts)))

(re-frame/reg-fx
 :http-get
 http-get)

(defn- http-raw-get [{:keys [url success-event-creator failure-event-creator timeout-ms]}]
  (let [on-success #(when-let [event (success-event-creator %)] (re-frame/dispatch event))
        on-error   (when failure-event-creator #(re-frame/dispatch (failure-event-creator %)))
        opts       {:timeout-ms      timeout-ms}]
    (http/raw-get url on-success on-error opts)))

(re-frame/reg-fx
 :http-raw-get
 http-raw-get)

(re-frame/reg-fx
 :http-get-n
 (fn [calls]
   (doseq [call calls]
     (http-get call))))

(defn- http-post [{:keys [url data response-validator success-event-creator failure-event-creator timeout-ms opts]}]
  (let [on-success #(re-frame/dispatch (success-event-creator %))
        on-error   (when failure-event-creator #(re-frame/dispatch (failure-event-creator %)))
        all-opts   (assoc opts
                          :valid-response? response-validator
                          :timeout-ms      timeout-ms)]
    (http/post url data on-success on-error all-opts)))

(re-frame/reg-fx
 :http-post
 http-post)

(defn- http-raw-post [{:keys [url body response-validator success-event-creator failure-event-creator timeout-ms opts]}]
  (let [on-success #(re-frame/dispatch (success-event-creator %))
        on-error   (when failure-event-creator #(re-frame/dispatch (failure-event-creator %)))
        all-opts   (assoc opts
                          :valid-response? response-validator
                          :timeout-ms      timeout-ms)]
    (http/raw-post url body on-success on-error all-opts)))

(re-frame/reg-fx
 :http-raw-post
 http-raw-post)

(re-frame/reg-fx
 :request-permissions-fx
 (fn [options]
   (permissions/request-permissions options)))

(re-frame/reg-fx
 :ui/listen-to-window-dimensions-change
 (fn []
   (dimensions/add-event-listener)))

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
   (status/close-application)))

(re-frame/reg-fx
 ::app-state-change-fx
 (fn [state]
   (status/app-state-change state)))

(handlers/register-handler-fx
 :set
 (fn [{:keys [db]} [_ k v]]
   {:db (assoc db k v)}))

(handlers/register-handler-fx
 :set-once
 (fn [{:keys [db]} [_ k v]]
   (when-not (get db k)
     {:db (assoc db k v)})))

(handlers/register-handler-fx
 :set-in
 (fn [{:keys [db]} [_ path v]]
   {:db (assoc-in db path v)}))

(def authentication-options
  {:reason (i18n/label :t/biometric-auth-reason-login)})

(defn- on-biometric-auth-result [{:keys [bioauth-success bioauth-code bioauth-message]}]
  (when-not bioauth-success
    (if (= bioauth-code "USER_FALLBACK")
      (re-frame/dispatch [:multiaccounts.logout.ui/logout-confirmed])
      (utils/show-confirmation {:title (i18n/label :t/biometric-auth-confirm-title)
                                :content (or bioauth-message (i18n/label :t/biometric-auth-confirm-message))
                                :confirm-button-text (i18n/label :t/biometric-auth-confirm-try-again)
                                :cancel-button-text (i18n/label :t/biometric-auth-confirm-logout)
                                :on-accept #(biometric/authenticate on-biometric-auth-result authentication-options)
                                :on-cancel #(re-frame/dispatch [:multiaccounts.logout.ui/logout-confirmed])}))))

(fx/defn on-return-from-background [{:keys [db now] :as cofx}]
  (let [app-in-background-since (get db :app-in-background-since)
        signed-up? (get-in db [:multiaccount :signed-up?])
        biometric-auth? (= (:auth-method db) "biometric")
        requires-bio-auth (and
                           signed-up?
                           biometric-auth?
                           (some? app-in-background-since)
                           (>= (- now app-in-background-since)
                               const/ms-in-bg-for-require-bioauth))]
    (fx/merge cofx
              {:db (assoc db :app-in-background-since nil)}
              (mailserver/process-next-messages-request)
              (hardwallet/return-back-from-nfc-settings)
              #(when requires-bio-auth
                 (biometric/authenticate % on-biometric-auth-result authentication-options)))))

(fx/defn on-going-in-background [{:keys [db now] :as cofx}]
  (fx/merge cofx
            {:db (assoc db :app-in-background-since now)}))

(defn app-state-change [state {:keys [db] :as cofx}]
  (let [app-coming-from-background? (= state "active")
        app-going-in-background? (= state "background")]
    (fx/merge cofx
              {::app-state-change-fx state
               :db                   (assoc db :app-state state)}
              #(when app-coming-from-background?
                 (on-return-from-background %))
              #(when app-going-in-background?
                 (on-going-in-background %)))))

(handlers/register-handler-fx
 :app-state-change
 (fn [cofx [_ state]]
   (app-state-change state cofx)))

(handlers/register-handler-fx
 :request-permissions
 (fn [_ [_ options]]
   {:request-permissions-fx options}))

(handlers/register-handler-fx
 :set-swipe-position
 (fn [{:keys [db]} [_ type item-id value]]
   {:db (assoc-in db [:animations type item-id :delete-swiped] value)}))

(handlers/register-handler-fx
 :show-tab-bar
 (fn [{:keys [db]} _]
   {:db (assoc db :tab-bar-visible? true)}))

(handlers/register-handler-fx
 :hide-tab-bar
 (fn [{:keys [db]} _]
   {:db (assoc db :tab-bar-visible? false)}))

(handlers/register-handler-fx
 :update-window-dimensions
 (fn [{:keys [db]} [_ dimensions]]
   {:db (assoc db :dimensions/window (dimensions/window dimensions))}))

(handlers/register-handler-fx
 :set-two-pane-ui-enabled
 (fn [{:keys [db]} [_ enabled?]]
   {:db (assoc db :two-pane-ui-enabled? enabled?)}))

(handlers/register-handler-fx
 :screens/on-will-focus
 (fn [{:keys [db] :as cofx} [_ view-id]]
   (fx/merge cofx
             {:db (assoc db :view-id view-id)}
             #(case view-id
                :keycard-settings (hardwallet/settings-screen-did-load %)
                :reset-card (hardwallet/reset-card-screen-did-load %)
                :enter-pin-login (hardwallet/enter-pin-screen-did-load %)
                :enter-pin-sign (hardwallet/enter-pin-screen-did-load %)
                :enter-pin-settings (hardwallet/enter-pin-screen-did-load %)
                :enter-pin-modal (hardwallet/enter-pin-screen-did-load %)
                :keycard-login-pin (hardwallet/enter-pin-screen-did-load %)
                :hardwallet-connect (hardwallet/hardwallet-connect-screen-did-load %)
                :hardwallet-connect-sign (hardwallet/hardwallet-connect-screen-did-load %)
                :hardwallet-connect-settings (hardwallet/hardwallet-connect-screen-did-load %)
                :hardwallet-connect-modal (hardwallet/hardwallet-connect-screen-did-load %)
                :hardwallet-authentication-method (hardwallet/authentication-method-screen-did-load %)
                :multiaccounts (hardwallet/multiaccounts-screen-did-load %)
                nil))))
