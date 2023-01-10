(ns status-im.events
  (:require
   clojure.set
   [re-frame.core :as re-frame]
   status-im.add-new.core
   [status-im.async-storage.core :as async-storage]
   status-im.backup.core
   status-im.bootnodes.core
   status-im.browser.core
   status-im.browser.permissions
   status-im.chat.models
   status-im.chat.models.images
   status-im.chat.models.input
   status-im.chat.models.loading
   status-im.chat.models.transport
   [status-im2.constants :as constants]
   status-im.contact.block
   status-im.contact.chat
   status-im.contact.core
   status-im.currency.core
   status-im.ethereum.subscriptions
   status-im.fleet.core
   status-im.http.core
   [utils.i18n :as i18n]
   [status-im.keycard.core :as keycard]
   status-im.log-level.core
   status-im.mailserver.constants
   [status-im.mailserver.core :as mailserver]
   [status-im.multiaccounts.biometric.core :as biometric]
   status-im.multiaccounts.login.core
   status-im.multiaccounts.logout.core
   [status-im.multiaccounts.model :as multiaccounts.model]
   status-im.multiaccounts.update.core
   [status-im.native-module.core :as status]
   status-im.network.net-info
   status-im.pairing.core
   status-im.profile.core
   status-im.search.core
   status-im.signals.core
   status-im.stickers.core
   status-im.transport.core
   [status-im.ui.components.permissions :as permissions]
   [status-im.ui.components.react :as react]
   status-im.ui.screens.privacy-and-security-settings.events
   [status-im.utils.dimensions :as dimensions]
   [utils.re-frame :as rf]
   status-im.utils.logging.core
   [status-im.utils.universal-links.core :as universal-links]
   [status-im.utils.utils :as utils]
   status-im.visibility-status-popover.core
   status-im.visibility-status-updates.core
   status-im.waku.core
   status-im.wallet-connect-legacy.core
   status-im.wallet-connect.core
   status-im.wallet.accounts.core
   status-im.wallet.choose-recipient.core
   [status-im.wallet.core :as wallet]
   status-im.wallet.custom-tokens.core
   status-im2.contexts.activity-center.events
   status-im2.contexts.shell.events
   status-im.chat.models.gaps
   [status-im2.navigation.events :as navigation]))

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

(rf/defn dismiss-keyboard
  {:events [:dismiss-keyboard]}
  [_]
  {:dismiss-keyboard nil})

(rf/defn identicon-generated
  {:events [:identicon-generated]}
  [{:keys [db]} path identicon]
  {:db (assoc-in db path identicon)})

(rf/defn gfycat-generated
  {:events [:gfycat-generated]}
  [{:keys [db]} path gfycat]
  {:db (assoc-in db path gfycat)})

(rf/defn system-theme-mode-changed
  {:events [:system-theme-mode-changed]}
  [{:keys [db] :as cofx} _]
  (let [current-theme-type (get-in cofx [:db :multiaccount :appearance])]
    (when (and (multiaccounts.model/logged-in? cofx)
               (= current-theme-type status-im2.constants/theme-type-system))
      {:multiaccounts.ui/switch-theme
       [(get-in db [:multiaccount :appearance])
        (:view-id db) true]})))

(def authentication-options
  {:reason (i18n/label :t/biometric-auth-reason-login)})

(defn- on-biometric-auth-result
  [{:keys [bioauth-success bioauth-code bioauth-message]}]
  (when-not bioauth-success
    (if (= bioauth-code "USER_FALLBACK")
      (re-frame/dispatch [:multiaccounts.logout.ui/logout-confirmed])
      (utils/show-confirmation
       {:title               (i18n/label :t/biometric-auth-confirm-title)
        :content             (or bioauth-message (i18n/label :t/biometric-auth-confirm-message))
        :confirm-button-text (i18n/label :t/biometric-auth-confirm-try-again)
        :cancel-button-text  (i18n/label :t/biometric-auth-confirm-logout)
        :on-accept           #(biometric/authenticate nil
                                                      on-biometric-auth-result
                                                      authentication-options)
        :on-cancel           #(re-frame/dispatch [:multiaccounts.logout.ui/logout-confirmed])}))))

(rf/defn on-return-from-background
  [{:keys [db now] :as cofx}]
  (let [app-in-background-since (get db :app-in-background-since)
        signed-up?              (get-in db [:multiaccount :signed-up?])
        biometric-auth?         (= (:auth-method db) "biometric")
        requires-bio-auth       (and
                                 signed-up?
                                 biometric-auth?
                                 (some? app-in-background-since)
                                 (>= (- now app-in-background-since)
                                     constants/ms-in-bg-for-require-bioauth))]
    (rf/merge cofx
              {:db (dissoc db :app-in-background-since)}
              (mailserver/process-next-messages-request)
              (wallet/restart-wallet-service-after-background app-in-background-since)
              (universal-links/process-stored-event)
              #(when-let [chat-id (:current-chat-id db)]
                 {:dispatch [:chat/mark-all-as-read chat-id]})
              #(when requires-bio-auth
                 (biometric/authenticate % on-biometric-auth-result authentication-options)))))

(rf/defn on-going-in-background
  [{:keys [db now]}]
  {:db (assoc db :app-in-background-since now)
   ;; event not implemented
   ;; :dispatch-n [[:audio-recorder/on-background] [:audio-message/on-background]]
  })

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
              {::async-storage/set! {:chat-id (get-in cofx [:db :current-chat-id])
                                     :key-uid (get-in cofx [:db :multiaccount :key-uid])}
               :db                  (assoc db :screens/was-focused-once? true)}

              (= :login view-id)
              {}

              (not (get db :screens/was-focused-once?))
              {:db (assoc db :screens/was-focused-once? true)}

              :else
              {::async-storage/set! {:chat-id nil
                                     :key-uid nil}
               :db                  (assoc db :screens/was-focused-once? true)})
            #(case view-id
               :keycard-settings              (keycard/settings-screen-did-load %)
               :reset-card                    (keycard/reset-card-screen-did-load %)
               :enter-pin-settings            (keycard/enter-pin-screen-did-load %)
               :keycard-login-pin             (keycard/login-pin-screen-did-load %)
               :add-new-account-pin           (keycard/enter-pin-screen-did-load %)
               :keycard-authentication-method (keycard/authentication-method-screen-did-load %)
               :multiaccounts                 (keycard/multiaccounts-screen-did-load %)
               :wallet                        (wallet/wallet-will-focus %)
               nil)))

;;TODO :replace by named events
(rf/defn set-event
  {:events [:set]}
  [{:keys [db]} k v]
  {:db (assoc db k v)})

(rf/defn set-view-id
  {:events [:set-view-id]}
  [{:keys [db]} view-id]
  {:db (assoc db :view-id view-id)})

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

(rf/defn open-buy-crypto-screen
  {:events [:buy-crypto.ui/open-screen]}
  [cofx]
  (rf/merge
   cofx
   (navigation/open-modal :buy-crypto nil)
   (wallet/keep-watching-history)))

;; Information Box

(def closable-information-boxes
  "[{:id      information box id
     :global? true/false (close information box across all profiles)}]"
  [])

(defn information-box-id-hash
  [id public-key global?]
  (if global?
    (hash id)
    (hash (str public-key id))))

(rf/defn close-information-box
  {:events [:close-information-box]}
  [{:keys [db]} id global?]
  (let [public-key (get-in db [:multiaccount :public-key])
        hash       (information-box-id-hash id public-key global?)]
    {::async-storage/set! {hash true}
     :db                  (assoc-in db [:information-box-states id] true)}))

(rf/defn information-box-states-loaded
  {:events [:information-box-states-loaded]}
  [{:keys [db]} hashes states]
  {:db (assoc db
              :information-box-states
              (reduce
               (fn [acc [id hash]]
                 (assoc acc id (get states hash)))
               {}
               hashes))})

(rf/defn load-information-box-states
  {:events [:load-information-box-states]}
  [{:keys [db]}]
  (let [public-key            (get-in db [:multiaccount :public-key])
        {:keys [keys hashes]} (reduce (fn [acc {:keys [id global?]}]
                                        (let [hash (information-box-id-hash
                                                    id
                                                    public-key
                                                    global?)]
                                          (-> acc
                                              (assoc-in [:hashes id] hash)
                                              (update :keys #(conj % hash)))))
                                      {}
                                      closable-information-boxes)]
    {::async-storage/get {:keys keys
                          :cb   #(re-frame/dispatch
                                  [:information-box-states-loaded hashes %])}}))
