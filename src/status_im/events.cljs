(ns status-im.events
  (:require clojure.set
            [re-frame.core :as re-frame]
            status-im.add-new.core
            [status-im.async-storage.core :as async-storage]
            status-im.backup.core
            status-im.bootnodes.core
            [status-im.bottom-sheet.core :as bottom-sheet]
            status-im.browser.core
            status-im.browser.permissions
            [status-im.chat.models :as chat]
            status-im.chat.models.images
            status-im.chat.models.input
            status-im.chat.models.loading
            status-im.chat.models.transport
            [status-im.constants :as constants]
            status-im.contact.block
            status-im.contact.chat
            status-im.contact.core
            status-im.currency.core
            [status-im.ethereum.json-rpc :as json-rpc]
            status-im.ethereum.subscriptions
            status-im.fleet.core
            status-im.http.core
            [status-im.i18n.i18n :as i18n]
            status-im.init.core
            [status-im.keycard.core :as keycard]
            status-im.log-level.core
            status-im.mailserver.constants
            [status-im.mailserver.core :as mailserver]
            [status-im.multiaccounts.biometric.core :as biometric]
            [status-im.multiaccounts.core :as multiaccounts]
            status-im.multiaccounts.login.core
            status-im.multiaccounts.logout.core
            status-im.multiaccounts.update.core
            [status-im.native-module.core :as status]
            [status-im.navigation :as navigation]
            status-im.notifications-center.core
            status-im.activity-center.core
            status-im.pairing.core
            [status-im.popover.core :as popover]
            status-im.profile.core
            status-im.search.core
            status-im.signals.core
            status-im.stickers.core
            status-im.transport.core
            [status-im.ui.components.permissions :as permissions]
            [status-im.ui.components.react :as react]
            status-im.ui.screens.privacy-and-security-settings.events
            [status-im.utils.dimensions :as dimensions]
            [status-im.utils.fx :as fx]
            status-im.utils.logging.core
            [status-im.utils.universal-links.core :as universal-links]
            [status-im.utils.utils :as utils]
            [status-im.visibility-status-popover.core :as visibility-status-popover]
            status-im.visibility-status-updates.core
            status-im.waku.core
            status-im.wallet.accounts.core
            status-im.wallet.choose-recipient.core
            [status-im.wallet.core :as wallet]
            status-im.wallet.custom-tokens.core
            [status-im.navigation.core :as navigation.core]
            [status-im.navigation.state :as navigation.state]
            [status-im.signing.core :as signing]
            status-im.wallet-connect.core
            status-im.wallet-connect-legacy.core
            status-im.navigation2
            status-im.navigation2.core))

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
   (status/close-application)))

(re-frame/reg-fx
 ::app-state-change-fx
 (fn [state]
   (status/app-state-change state)))

(re-frame/reg-fx
 :ui/listen-to-window-dimensions-change
 (fn []
   (dimensions/add-event-listener)))

(fx/defn dismiss-keyboard
  {:events [:dismiss-keyboard]}
  [_]
  {:dismiss-keyboard nil})

(fx/defn identicon-generated
  {:events [:identicon-generated]}
  [{:keys [db]} path identicon]
  {:db (assoc-in db path identicon)})

(fx/defn gfycat-generated
  {:events [:gfycat-generated]}
  [{:keys [db]} path gfycat]
  {:db (assoc-in db path gfycat)})

(fx/defn system-theme-mode-changed
  {:events [:system-theme-mode-changed]}
  [{:keys [db] :as cofx} theme]
  (let [cur-theme        (get-in db [:multiaccount :appearance])
        current-tab      (get db :current-tab :chat)
        view-id          (:view-id db)
        screen-params    (get-in db [:navigation/screen-params view-id])
        root-id          @navigation.state/root-id
        key-uid          (get-in db [:multiaccounts/login :key-uid])
        keycard-account? (boolean (get-in db [:multiaccounts/multiaccounts
                                              key-uid
                                              :keycard-pairing]))
        dispatch-later   (cond-> []
                           (= view-id :chat)
                           (conj {:ms       1000
                                  :dispatch [:chat.ui/navigate-to-chat (:current-chat-id db)]})

                           (and
                            (= root-id :chat-stack)
                            (not-any? #(= view-id %) '(:home :empty-tab :wallet :status :my-profile :chat)))
                           (conj {:ms       1000
                                  :dispatch [:navigate-to view-id screen-params]})

                           (some #(= view-id %) navigation.core/community-screens)
                           (conj {:ms 800 :dispatch
                                  [:navigate-to :community
                                   (get-in db [:navigation/screen-params :community])]})

                           (= view-id :community-emoji-thumbnail-picker)
                           (conj {:ms 900 :dispatch
                                  [:navigate-to :create-community-channel
                                   (get-in db [:navigation/screen-params :create-community-channel])]}))]
    (when (and (some? root-id) (or (nil? cur-theme) (zero? cur-theme)))
      (navigation.core/dismiss-all-modals)
      (fx/merge cofx
                (merge
                 {::multiaccounts/switch-theme (if (= :dark theme) 2 1)}
                 (when (seq dispatch-later)
                   {:utils/dispatch-later dispatch-later}))
                (when (get-in db [:bottom-sheet/show?])
                  (bottom-sheet/hide-bottom-sheet))
                (when (get-in db [:popover/popover])
                  (popover/hide-popover))
                (when (get-in db [:visibility-status-popover/popover])
                  (visibility-status-popover/hide-visibility-status-popover))
                (when (get-in db [:signing/tx])
                  (signing/discard))
                (if (and (= root-id :multiaccounts) keycard-account?)
                  (navigation/init-root-with-component :multiaccounts-keycard :multiaccounts)
                  (navigation/init-root root-id))
                (when (= root-id :chat-stack)
                  (navigation/change-tab current-tab))))))

(def authentication-options
  {:reason (i18n/label :t/biometric-auth-reason-login)})

(defn- on-biometric-auth-result [{:keys [bioauth-success bioauth-code bioauth-message]}]
  (when-not bioauth-success
    (if (= bioauth-code "USER_FALLBACK")
      (re-frame/dispatch [:multiaccounts.logout.ui/logout-confirmed])
      (utils/show-confirmation {:title               (i18n/label :t/biometric-auth-confirm-title)
                                :content             (or bioauth-message (i18n/label :t/biometric-auth-confirm-message))
                                :confirm-button-text (i18n/label :t/biometric-auth-confirm-try-again)
                                :cancel-button-text  (i18n/label :t/biometric-auth-confirm-logout)
                                :on-accept           #(biometric/authenticate nil on-biometric-auth-result authentication-options)
                                :on-cancel           #(re-frame/dispatch [:multiaccounts.logout.ui/logout-confirmed])}))))

(fx/defn on-return-from-background [{:keys [db now] :as cofx}]
  (let [app-in-background-since (get db :app-in-background-since)
        signed-up? (get-in db [:multiaccount :signed-up?])
        biometric-auth? (= (:auth-method db) "biometric")
        requires-bio-auth (and
                           signed-up?
                           biometric-auth?
                           (some? app-in-background-since)
                           (>= (- now app-in-background-since)
                               constants/ms-in-bg-for-require-bioauth))]
    (fx/merge cofx
              {:db (-> db
                       (dissoc :app-in-background-since)
                       (assoc :app-active-since now))}
              (mailserver/process-next-messages-request)
              (wallet/restart-wallet-service-after-background app-in-background-since)
              (universal-links/process-stored-event)
              #(when-let [chat-id (:current-chat-id db)]
                 {:dispatch [:chat/mark-all-as-read chat-id]})
              #(when requires-bio-auth
                 (biometric/authenticate % on-biometric-auth-result authentication-options)))))

(fx/defn on-going-in-background
  [{:keys [db now] :as cofx}]
  {:db         (-> db
                   (dissoc :app-active-since)
                   (assoc :app-in-background-since now))
   :dispatch-n [[:audio-recorder/on-background] [:audio-message/on-background]]})

(fx/defn app-state-change
  {:events [:app-state-change]}
  [{:keys [db] :as cofx} state]
  (let [app-coming-from-background? (= state "active")
        app-going-in-background? (= state "background")]
    (fx/merge cofx
              {::app-state-change-fx state
               :db                   (assoc db :app-state state)}
              #(when app-coming-from-background?
                 (on-return-from-background %))
              #(when app-going-in-background?
                 (on-going-in-background %)))))

(fx/defn request-permissions
  {:events [:request-permissions]}
  [_ options]
  {:request-permissions-fx options})

(fx/defn update-window-dimensions
  {:events [:update-window-dimensions]}
  [{:keys [db]} dimensions]
  {:db (assoc db :dimensions/window (dimensions/window dimensions))})

(fx/defn init-timeline-chat
  {:events [:init-timeline-chat]}
  [{:keys [db] :as cofx}]
  (when-not (get-in db [:pagination-info constants/timeline-chat-id :messages-initialized?])
    (chat/preload-chat-data cofx constants/timeline-chat-id)))

(fx/defn on-will-focus
  {:events [:screens/on-will-focus]}
  [{:keys [db] :as cofx} view-id]
  (fx/merge cofx
            (cond
              (= :chat view-id)
              {::async-storage/set! {:chat-id (get-in cofx [:db :current-chat-id])
                                     :key-uid (get-in cofx [:db :multiaccount :key-uid])}
               :db (assoc db :screens/was-focused-once? true)}

              (= :login view-id)
              {}

              (not (get db :screens/was-focused-once?))
              {:db (assoc db :screens/was-focused-once? true)}

              :else
              {::async-storage/set! {:chat-id nil
                                     :key-uid nil}
               :db (assoc db :screens/was-focused-once? true)})
            #(case view-id
               :keycard-settings (keycard/settings-screen-did-load %)
               :reset-card (keycard/reset-card-screen-did-load %)
               :enter-pin-settings (keycard/enter-pin-screen-did-load %)
               :keycard-login-pin (keycard/login-pin-screen-did-load %)
               :add-new-account-pin (keycard/enter-pin-screen-did-load %)
               :keycard-authentication-method (keycard/authentication-method-screen-did-load %)
               :multiaccounts (keycard/multiaccounts-screen-did-load %)
               :wallet (wallet/wallet-will-focus %)
               nil)))

;;TODO :replace by named events
(fx/defn set-event
  {:events [:set]}
  [{:keys [db]} k v]
  {:db (assoc db k v)})

;;TODO :replace by named events
(fx/defn set-once-event
  {:events [:set-once]}
  [{:keys [db]} k v]
  (when-not (get db k)
    {:db (assoc db k v)}))

;;TODO :replace by named events
(fx/defn set-in-event
  {:events [:set-in]}
  [{:keys [db]} path v]
  {:db (assoc-in db path v)})

(defn on-ramp<-rpc [on-ramp]
  (clojure.set/rename-keys on-ramp {:logoUrl :logo-url
                                    :siteUrl :site-url}))

(fx/defn crypto-loaded-event
  {:events [::crypto-loaded]}
  [{:keys [db]} on-ramps]
  {:db (assoc
        db
        :buy-crypto/on-ramps
        (map on-ramp<-rpc on-ramps))})

(fx/defn buy-crypto-ui-loaded
  {:events [:buy-crypto.ui/loaded]}
  [_]
  {::json-rpc/call [{:method     "wallet_getCryptoOnRamps"
                     :params     []
                     :on-success (fn [on-ramps]
                                   (re-frame/dispatch [::crypto-loaded on-ramps]))}]})

(fx/defn open-buy-crypto-screen
  {:events [:buy-crypto.ui/open-screen]}
  [cofx]
  (fx/merge
   cofx
   (navigation/open-modal :buy-crypto nil)
   (wallet/keep-watching-history)))

;; Information Box

(def closable-information-boxes
  [{:id      :ens-banner
    :global? true}]) ;; global? - close information box across all profiles

(defn information-box-id-hash [id public-key global?]
  (if global?
    (hash id)
    (hash (str public-key id))))

(fx/defn close-information-box
  {:events [:close-information-box]}
  [{:keys [db]} id global?]
  (let [public-key (get-in db [:multiaccount :public-key])
        hash       (information-box-id-hash id public-key global?)]
    {::async-storage/set! {hash true}
     :db (assoc-in db [:information-box-states id] true)}))

(fx/defn information-box-states-loaded
  {:events [:information-box-states-loaded]}
  [{:keys [db]} hashes states]
  {:db (assoc db :information-box-states (reduce
                                          (fn [acc [id hash]]
                                            (assoc acc id (get states hash)))
                                          {} hashes))})

(fx/defn load-information-box-states
  {:events [:load-information-box-states]}
  [{:keys [db]}]
  (let [public-key            (get-in db [:multiaccount :public-key])
        {:keys [keys hashes]} (reduce (fn [acc {:keys [id global?]}]
                                        (let [hash (information-box-id-hash
                                                    id public-key global?)]
                                          (-> acc
                                              (assoc-in [:hashes id] hash)
                                              (update :keys #(conj % hash)))))
                                      {} closable-information-boxes)]
    {::async-storage/get {:keys keys
                          :cb   #(re-frame/dispatch
                                  [:information-box-states-loaded hashes %])}}))
