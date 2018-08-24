(ns status-im.ui.screens.events
  #:ghostwheel.core{:check     true
                    :num-tests 10}
  (:require status-im.chat.events
            [status-im.models.chat :as chat]
            status-im.network.events
            [status-im.transport.handlers :as transport.handlers]
            status-im.protocol.handlers
            [status-im.models.protocol :as models.protocol]
            [status-im.models.account :as models.account]
            [status-im.ui.screens.accounts.models :as accounts.models]
            status-im.ui.screens.accounts.login.events
            status-im.ui.screens.accounts.recover.events
            [status-im.ui.screens.contacts.events :as contacts]
            [status-im.models.contacts :as models.contacts]
            status-im.ui.screens.add-new.new-chat.events
            status-im.ui.screens.group.chat-settings.events
            status-im.ui.screens.group.events
            [status-im.ui.screens.navigation :as navigation]
            [status-im.utils.universal-links.core :as universal-links]
            [status-im.utils.dimensions :as dimensions]
            status-im.utils.universal-links.events
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
            [status-im.models.transactions :as transactions]
            status-im.ui.screens.wallet.choose-recipient.events
            status-im.ui.screens.wallet.collectibles.cryptokitties.events
            status-im.ui.screens.wallet.collectibles.cryptostrikers.events
            status-im.ui.screens.wallet.collectibles.etheremon.events
            status-im.ui.screens.browser.events
            [status-im.models.browser :as browser]
            status-im.ui.screens.offline-messaging-settings.events
            status-im.ui.screens.privacy-policy.events
            status-im.ui.screens.bootnodes-settings.events
            status-im.ui.screens.currency-settings.events
            status-im.utils.keychain.events
            [re-frame.core :as re-frame]
            [status-im.native-module.core :as status]
            [status-im.ui.components.permissions :as permissions]
            [status-im.constants :as constants]
            [status-im.data-store.core :as data-store]
            [status-im.data-store.realm.core :as realm]
            [status-im.utils.keychain.core :as keychain]
            [status-im.i18n :as i18n]
            [status-im.js-dependencies :as dependencies]
            [status-im.ui.components.react :as react]
            [status-im.transport.core :as transport]
            [status-im.transport.inbox :as inbox]
            [status-im.ui.screens.db :refer [app-db]]
            [status-im.utils.datetime :as time]
            [status-im.utils.ethereum.core :as ethereum]
            [status-im.utils.random :as random]
            [status-im.utils.config :as config]
            [status-im.utils.notifications :as notifications]
            [status-im.utils.handlers :as handlers]
            [status-im.utils.handlers-macro :as handlers-macro]
            [status-im.utils.http :as http]
            [status-im.utils.instabug :as instabug]
            [status-im.utils.platform :as platform]
            [status-im.utils.types :as types]
            [status-im.utils.utils :as utils]
            [taoensso.timbre :as log]
            [cljs.spec.alpha :as s]
            [ghostwheel.core :as g
             :refer [>defn >defn- >fdef => | <- ?]]
            [cljs.test :as test]))

;;;; REVIEW: Ghostwheel example:

(s/def ::public-key string?)
(s/def ::something int?)
(s/def ::db (s/keys :req [::public-key]))
(s/def ::web3 (s/keys :req [::something]))
(s/def ::cofx (s/keys :req [::db ::web3]))
(s/def ::dispatch (s/and vector?
                         (s/cat :event-id keyword? :event-args (s/* any?))))
(s/def ::fx (s/keys :opt [::db ::dispatch]))

;; run `(test/run-tests)` in the repl to run all
;; auto-generated generative unit tests in this namespace
(>defn ghostwheel-example
  [s cofx]
  [string? ::cofx => ::fx]
  {:dispatch [:foobar 5]})

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
 :fetch-web3-node-version
 (fn [web3 _]
   (.. web3 -version (getNode (fn [err resp]
                                (when-not err
                                  (re-frame/dispatch [:fetch-web3-node-version-callback resp])))))
   nil))

;; Try to decrypt the database, move on if successful otherwise go back to
;; initial state
(re-frame/reg-fx
 ::init-store
 (fn [encryption-key]
   (.. (data-store/init encryption-key)
       (then #(re-frame/dispatch [:after-decryption]))
       (catch (fn [error]
                (log/warn "Could not decrypt database" error)
                (re-frame/dispatch [:initialize-app encryption-key :decryption-failed]))))))

(re-frame/reg-fx
 :initialize-geth-fx
 (fn [config]
   (status/start-node (types/clj->json config) config/fleet)))

(re-frame/reg-fx
 ::status-module-initialized-fx
 (fn [_]
   (status/module-initialized!)))

(re-frame/reg-fx
 :request-permissions-fx
 (fn [options]
   (permissions/request-permissions options)))

(re-frame/reg-fx
 ::request-notifications-fx
 (fn [_]
   (notifications/request-permissions)))

(re-frame/reg-fx
 ::testfairy-alert
 (fn [_]
   (when config/testfairy-enabled?
     (utils/show-popup
      (i18n/label :testfairy-title)
      (i18n/label :testfairy-message)))))

(re-frame/reg-fx
 ::init-device-UUID
 (fn []
   (status/get-device-UUID #(re-frame/dispatch [:set :device-UUID %]))))

(re-frame/reg-fx
 ::listen-to-window-dimensions-change
 (fn []
   (dimensions/add-event-listener)))

(re-frame/reg-fx
 ::get-fcm-token-fx
 (fn [_]
   (when platform/mobile?
     (notifications/get-fcm-token))))

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

(defn- reset-keychain []
  (.. (keychain/reset)
      (then
       #(re-frame/dispatch [:initialize-keychain]))))

(defn- handle-reset-data  []
  (.. (realm/delete-realms)
      (then reset-keychain)
      (catch reset-keychain)))

(defn handle-invalid-key-parameters [encryption-key]
  {:title               (i18n/label :invalid-key-title)
   :content             (i18n/label :invalid-key-content)
   :confirm-button-text (i18n/label :invalid-key-confirm)
   ;; On cancel we initialize the app with the invalid key, to allow the user
   ;; to recover the seed phrase
   :on-cancel           #(do
                           (log/warn "initializing app with invalid key")
                           (re-frame/dispatch [:initialize-app encryption-key]))
   :on-accept           handle-reset-data})

(defn handle-decryption-failed-parameters [encryption-key]
  {:title               (i18n/label :decryption-failed-title)
   :content             (i18n/label :decryption-failed-content)
   :confirm-button-text (i18n/label :decryption-failed-confirm)
   ;; On cancel we initialize the app with the same key, in case the error was
   ;; not related/fs error
   :on-cancel           #(do
                           (log/warn "initializing app with same key after decryption failed")
                           (re-frame/dispatch [:initialize-app encryption-key]))
   :on-accept           handle-reset-data})

(defn initialize-views [{{:accounts/keys [accounts] :as db} :db}]
  {:db                                  (if (empty? accounts)
                                          (assoc db :view-id :intro :navigation-stack (list :intro))
                                          (let [{:keys [address photo-path name]} (first (sort-by :last-sign-in > (vals accounts)))]
                                            (-> db
                                                (assoc :view-id :login
                                                       :navigation-stack (list :login))
                                                (update :accounts/login assoc
                                                        :address address
                                                        :photo-path photo-path
                                                        :name name))))
   :handle-initial-push-notification-fx db})

(defn initialize-db
  "Initialize db to the initial state"
  [{{:universal-links/keys [url]
     :push-notifications/keys [initial?]
     :keys [status-module-initialized? status-node-started?
            network-status network peers-count peers-summary device-UUID]
     :or {network (get app-db :network)}} :db}]
  {:db (assoc app-db
              :contacts/contacts {}
              :network-status network-status
              :peers-count (or peers-count 0)
              :peers-summary (or peers-summary [])
              :status-module-initialized? (or platform/ios? js/goog.DEBUG status-module-initialized?)
              :status-node-started? status-node-started?
              :network network
              :universal-links/url url
              :push-notifications/initial? initial?
              :device-UUID device-UUID)})

;; Entrypoint, fetches the key from the keychain and initialize the app
(handlers/register-handler-fx
 :initialize-keychain
 (fn [_ _]
   {:get-encryption-key [:initialize-app]}))

;; Check the key is valid, shows options if not, otherwise continues loading
;; the database
(handlers/register-handler-fx
 :initialize-app
 (fn [cofx [_ encryption-key error]]
   (cond
     (= :invalid-key error)
     {:show-confirmation (handle-invalid-key-parameters encryption-key)}

     (= :decryption-failed error)
     {:show-confirmation (handle-decryption-failed-parameters encryption-key)}

     :else
     (handlers-macro/merge-fx cofx
                              {::init-device-UUID                   nil
                               ::init-store                         encryption-key
                               ::listen-to-window-dimensions-change nil
                               ::testfairy-alert                    nil}
                              (initialize-db)))))

;; DB has been decrypted, load accounts, initialize geth, etc
(handlers/register-handler-fx
 :after-decryption
 [(re-frame/inject-cofx :data-store/get-all-accounts)]
 (fn [cofx _]
   (handlers-macro/merge-fx cofx
                            {:dispatch-n
                             [[:listen-to-network-status]
                              [:initialize-geth]]}
                            (accounts.models/load-accounts)
                            (initialize-views))))

(handlers/register-handler-fx
 :logout
 (fn [{:keys [db] :as cofx} _]
   (let [{:transport/keys [chats]} db]
     (handlers-macro/merge-fx cofx
                              {:dispatch [:initialize-keychain]}
                              (navigation/navigate-to-clean nil)
                              (transport/stop-whisper)))))

(defn initialize-account-db [address {:keys [db web3]}]
  (let [{:keys [accounts/accounts accounts/create contacts/contacts networks/networks
                network network-status peers-count peers-summary view-id navigation-stack
                status-module-initialized? status-node-started? device-UUID
                push-notifications/initial? semaphores]
         :or   {network (get app-db :network)}} db
        console-contact (get contacts constants/console-chat-id)
        current-account (accounts address)
        account-network-id (get current-account :network network)
        account-network (get-in current-account [:networks account-network-id])]
    {:db (cond-> (assoc app-db
                        :current-public-key (:public-key current-account)
                        :view-id view-id
                        :navigation-stack navigation-stack
                        :status-module-initialized? (or platform/ios? js/goog.DEBUG status-module-initialized?)
                        :status-node-started? status-node-started?
                        :accounts/create create
                        :networks/networks networks
                        :account/account current-account
                        :network-status network-status
                        :network network
                        :chain (ethereum/network->chain-name account-network)
                        :push-notifications/initial? initial?
                        :peers-summary peers-summary
                        :peers-count peers-count
                        :device-UUID device-UUID
                        :semaphores semaphores
                        :web3 web3)
           console-contact
           (assoc :contacts/contacts {constants/console-chat-id console-contact}))}))

(handlers/register-handler-fx
 :initialize-account
 [(re-frame/inject-cofx :protocol/get-web3)
  (re-frame/inject-cofx :get-default-contacts)
  (re-frame/inject-cofx :get-default-dapps)
  (re-frame/inject-cofx :data-store/all-chats)
  (re-frame/inject-cofx :data-store/get-messages)
  (re-frame/inject-cofx :data-store/get-user-statuses)
  (re-frame/inject-cofx :data-store/unviewed-messages)
  (re-frame/inject-cofx :data-store/message-ids)
  (re-frame/inject-cofx :data-store/get-unanswered-requests)
  (re-frame/inject-cofx :data-store/get-local-storage-data)
  (re-frame/inject-cofx :data-store/get-all-contacts)
  (re-frame/inject-cofx :data-store/get-all-mailservers)
  (re-frame/inject-cofx :data-store/transport)
  (re-frame/inject-cofx :data-store/all-browsers)
  (re-frame/inject-cofx :data-store/all-dapp-permissions)]
 (fn [{:keys [web3] :as cofx} [_ address events-after]]
   (handlers-macro/merge-fx cofx
                            {:protocol/set-default-account [web3 address]
                             :fetch-web3-node-version      web3
                             ::get-fcm-token-fx            nil
                             :dispatch-n                   (or events-after [])}
                            (initialize-account-db address)
                            (models.protocol/initialize-protocol address)
                            (models.contacts/load-contacts)
                            (chat/initialize-chats)
                            (chat/process-pending-messages)
                            (browser/initialize-browsers)
                            (browser/initialize-dapp-permissions)
                            (models.wallet/update-wallet)
                            (transactions/run-update)
                            (transactions/start-sync)
                            (models.account/update-sign-in-time))))

(handlers/register-handler-fx
 :initialize-geth
 (fn [{db :db} _]
   (when-not (:status-node-started? db)
     (let [default-networks (:networks/networks db)
           default-network  (:network db)]
       {:initialize-geth-fx (get-in default-networks [default-network :config])}))))

(handlers/register-handler-fx
 :fetch-web3-node-version-callback
 (fn [{:keys [db]} [_ resp]]
   (when-let [git-commit (nth (re-find #"-([0-9a-f]{7,})/" resp) 1)]
     {:db (assoc db :web3-node-version git-commit)})))

(handlers/register-handler-fx
 :discovery/summary
 (fn [{:keys [db] :as cofx} [_ peers-summary]]
   (let [previous-summary (:peers-summary db)
         peers-count      (count peers-summary)]
     (handlers-macro/merge-fx cofx
                              {:db (assoc db
                                          :peers-summary peers-summary
                                          :peers-count peers-count)}
                              (transport.handlers/resend-contact-messages previous-summary)
                              (inbox/peers-summary-change-fx previous-summary)))))

(handlers/register-handler-fx
 :signal-event
 (fn [_ [_ event-str]]
   (log/debug :event-str event-str)
   (instabug/log (str "Signal event: " event-str))
   (let [{:keys [type event]} (types/json->clj event-str)
         to-dispatch (case type
                       "node.started"        [:status-node-started]
                       "node.stopped"        [:status-node-stopped]
                       "module.initialized"  [:status-module-initialized]
                       "envelope.sent"       [:signals/envelope-status (:hash event) :sent]
                       "envelope.expired"    [:signals/envelope-status (:hash event) :not-sent]
                       "discovery.summary"   [:discovery/summary event]
                       (log/debug "Event " type " not handled"))]
     (when to-dispatch
       {:dispatch to-dispatch}))))

(handlers/register-handler-fx
 :status-module-initialized
 (fn [{:keys [db]} _]
   {:db                            (assoc db :status-module-initialized? true)
    ::status-module-initialized-fx nil}))

(handlers/register-handler-fx
 :status-node-started
 (fn [{{:node/keys [after-start] :as db} :db} _]
   (merge {:db (assoc db :status-node-started? true)}
          (when after-start {:dispatch-n [after-start]}))))

(handlers/register-handler-fx
 :status-node-stopped
 (fn [{{:node/keys [after-stop]} :db} _]
   (when after-stop {:dispatch-n [after-stop]})))

(handlers/register-handler-fx
 :app-state-change
 (fn [{:keys [db] :as cofx} [_ state]]
   (let [app-coming-from-background? (= state "active")]
     (handlers-macro/merge-fx cofx
                              {::app-state-change-fx state
                               :db                   (assoc db :app-state state)}
                              (inbox/request-messages app-coming-from-background?)))))

(handlers/register-handler-fx
 :request-permissions
 (fn [_ [_ options]]
   {:request-permissions-fx options}))

(handlers/register-handler-fx
 :request-notifications
 (fn [_ _]
   {::request-notifications-fx {}}))

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
