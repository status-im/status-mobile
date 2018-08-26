(ns status-im.ui.screens.events
  (:require status-im.chat.events
            status-im.network.events
            [status-im.transport.handlers :as transport.handlers]
            status-im.protocol.handlers
            [status-im.models.protocol :as models.protocol]
            [status-im.models.account :as models.account]
            [status-im.ui.screens.accounts.models :as accounts.models]
            status-im.ui.screens.accounts.login.events
            [status-im.ui.screens.accounts.login.models :as login]
            status-im.ui.screens.accounts.recover.events
            [status-im.models.contacts :as models.contacts]
            status-im.ui.screens.add-new.new-chat.events
            status-im.ui.screens.group.chat-settings.events
            status-im.ui.screens.group.events
            [status-im.ui.screens.navigation :as navigation]
            [status-im.utils.dimensions :as dimensions]
            status-im.utils.universal-links.events
            status-im.init.events
            status-im.signals.events
            status-im.web3.events
            status-im.notifications.events
            status-im.ui.screens.add-new.new-chat.navigation
            status-im.ui.screens.network-settings.events
            status-im.ui.screens.profile.events
            status-im.ui.screens.qr-scanner.events
            status-im.ui.screens.wallet.events
            [status-im.models.wallet :as models.wallet]
            status-im.ui.screens.wallet.collectibles.events
            status-im.ui.screens.wallet.send.events
            status-im.ui.screens.wallet.settings.events
            status-im.ui.screens.wallet.transactions.events
            status-im.ui.screens.wallet.choose-recipient.events
            status-im.ui.screens.wallet.collectibles.cryptokitties.events
            status-im.ui.screens.wallet.collectibles.cryptostrikers.events
            status-im.ui.screens.wallet.collectibles.etheremon.events
            status-im.ui.screens.browser.events
            status-im.ui.screens.offline-messaging-settings.events
            status-im.ui.screens.privacy-policy.events
            status-im.ui.screens.bootnodes-settings.events
            status-im.ui.screens.currency-settings.events
            status-im.utils.keychain.events
            [re-frame.core :as re-frame]
            [status-im.native-module.core :as status]
            [status-im.ui.components.permissions :as permissions]
            [status-im.transport.core :as transport]
            [status-im.transport.inbox :as inbox]
            [status-im.ui.screens.db :refer [app-db]]
            [status-im.utils.datetime :as time]
            [status-im.utils.random :as random]
            [status-im.utils.handlers :as handlers]
            [status-im.utils.handlers-macro :as handlers-macro]
            [status-im.utils.http :as http]
            [status-im.utils.utils :as utils]
            [taoensso.timbre :as log]))

;;;; COFX

(re-frame/reg-cofx
 :now
 (fn [coeffects _]
   (assoc coeffects :now (time/timestamp))))

(re-frame/reg-cofx
 :random-id
 (fn [coeffects _]
   (assoc coeffects :random-id (random/id))))

(re-frame/reg-cofx
 :random-id-seq
 (fn [coeffects _]
   (assoc coeffects :random-id-seq (repeatedly random/id))))

;;;; FX

(defn- http-get [{:keys [url response-validator success-event-creator failure-event-creator timeout-ms]}]
  (let [on-success #(re-frame/dispatch (success-event-creator %))
        on-error   #(re-frame/dispatch (failure-event-creator %))
        opts       {:valid-response? response-validator
                    :timeout-ms      timeout-ms}]
    (http/get url on-success on-error opts)))

(re-frame/reg-fx
 :http-get
 http-get)

(re-frame/reg-fx
 :http-get-n
 (fn [calls]
   (doseq [call calls]
     (http-get call))))

(re-frame/reg-fx
 :request-permissions-fx
 (fn [options]
   (permissions/request-permissions options)))

(re-frame/reg-fx
 :ui/listen-to-window-dimensions-change
 (fn []
   (dimensions/add-event-listener)))

(re-frame/reg-fx
 :show-error
 (fn [content]
   (utils/show-popup "Error" content)))

(re-frame/reg-fx
 :show-confirmation
 (fn [{:keys [title content confirm-button-text on-accept on-cancel]}]
   (utils/show-confirmation title content confirm-button-text on-accept on-cancel)))

(re-frame/reg-fx
 :close-application
 (fn [_]
   (status/close-application)))

(re-frame/reg-fx
 ::app-state-change-fx
 (fn [state]
   (status/app-state-change state)))

;;;; Handlers

(handlers/register-handler-db
 :set
 (fn [db [_ k v]]
   (assoc db k v)))

(handlers/register-handler-db
 :set-in
 (fn [db [_ path v]]
   (assoc-in db path v)))

(defn logout
  [{:keys [db] :as cofx}]
  (let [{:transport/keys [chats]} db]
    (handlers-macro/merge-fx cofx
                             {:dispatch [:init/initialize-keychain]
                              :clear-user-password [(get-in db [:account/account :address])]}
                             (navigation/navigate-to-clean nil)
                             (transport/stop-whisper))))

(handlers/register-handler-fx
 :logout
 (fn [cofx _]
   (logout cofx)))

(defn app-state-change [state {:keys [db] :as cofx}]
  (let [app-coming-from-background? (= state "active")]
    (handlers-macro/merge-fx cofx
                             {::app-state-change-fx state
                              :db                   (assoc db :app-state state)}
                             (inbox/request-messages app-coming-from-background?))))

(handlers/register-handler-fx
 :app-state-change
 (fn [cofx [_ state]]
   (app-state-change state cofx)))

(handlers/register-handler-fx
 :request-permissions
 (fn [_ [_ options]]
   {:request-permissions-fx options}))

(handlers/register-handler-db
 :set-swipe-position
 [re-frame/trim-v]
 (fn [db [item-id value]]
   (assoc-in db [:chat-animations item-id :delete-swiped] value)))

(handlers/register-handler-db
 :show-tab-bar
 (fn [db _]
   (assoc db :tab-bar-visible? true)))

(handlers/register-handler-db
 :hide-tab-bar
 (fn [db _]
   (assoc db :tab-bar-visible? false)))

(handlers/register-handler-db
 :update-window-dimensions
 (fn [db [_ dimensions]]
   (assoc db :dimensions/window (dimensions/window dimensions))))
