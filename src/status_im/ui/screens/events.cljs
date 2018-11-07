(ns status-im.ui.screens.events
  (:require status-im.events
            status-im.dev-server.events
            status-im.ui.screens.add-new.events
            status-im.ui.screens.add-new.new-chat.events
            status-im.ui.screens.group.chat-settings.events
            status-im.ui.screens.group.events
            status-im.utils.universal-links.events
            status-im.web3.events
            status-im.ui.screens.add-new.new-chat.navigation
            status-im.ui.screens.profile.events
            status-im.ui.screens.extensions.add.events
            status-im.ui.screens.wallet.events
            status-im.ui.screens.wallet.collectibles.events
            status-im.ui.screens.wallet.send.events
            status-im.ui.screens.wallet.request.events
            status-im.ui.screens.wallet.settings.events
            status-im.ui.screens.wallet.transactions.events
            status-im.ui.screens.wallet.choose-recipient.events
            status-im.ui.screens.wallet.collectibles.cryptokitties.events
            status-im.ui.screens.wallet.collectibles.cryptostrikers.events
            status-im.ui.screens.wallet.collectibles.etheremon.events
            status-im.ui.screens.wallet.collectibles.superrare.events
            status-im.ui.screens.wallet.collectibles.kudos.events
            status-im.utils.keychain.events
            [re-frame.core :as re-frame]
            [status-im.hardwallet.core :as hardwallet]
            [status-im.native-module.core :as status]
            [status-im.mailserver.core :as mailserver]
            [status-im.ui.components.permissions :as permissions]
            [status-im.utils.dimensions :as dimensions]
            [status-im.utils.handlers :as handlers]
            [status-im.utils.http :as http]
            [status-im.utils.utils :as utils]
            [status-im.utils.fx :as fx]
            [status-im.utils.platform :as platform]
            [taoensso.timbre :as log]
            [clojure.string :as str]))

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
        on-error   #(re-frame/dispatch (failure-event-creator %))
        all-opts   (assoc opts
                          :valid-response? response-validator
                          :timeout-ms      timeout-ms)]
    (http/post url data on-success on-error all-opts)))

(re-frame/reg-fx
 :http-post
 http-post)

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
 :set-in
 (fn [{:keys [db]} [_ path v]]
   {:db (assoc-in db path v)}))

(fx/defn on-return-from-background [cofx]
  (fx/merge cofx
            (mailserver/process-next-messages-request)
            (hardwallet/return-back-from-nfc-settings)))

(defn app-state-change [state {:keys [db] :as cofx}]
  (let [app-coming-from-background? (= state "active")]
    (fx/merge cofx
              {::app-state-change-fx state
               :db                   (assoc db :app-state state)}
              #(when app-coming-from-background?
                 (on-return-from-background %)))))

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
 :fetch-desktop-version-success
 (fn [{:keys [db]} [_ result]]
   (when (and result (not (str/blank? result)) (or platform/isMacOs? platform/isNix?))
     (let [lines (str/split-lines result)
           var   (if platform/isMacOs? "DMG_URL=\"" "NIX_URL=\"")
           param (first (filter #(not= -1 (.indexOf % var)) lines))]
       (when param
         (let [url    (subs param (+ 9 (.indexOf param var)) (- (count param) 1))
               dt     (- (count url) (if platform/isMacOs? 12 17))
               commit (subs url (- dt 6) dt)]
           {:db (assoc-in db [:desktop/desktop :nightly-version] {:url url :commit commit})}))))))
