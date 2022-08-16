(ns ^{:doc "Mailserver events and API"}
 status-im.mailserver.core
  (:require [clojure.string :as string]
            [re-frame.core :as re-frame]
            [status-im.ethereum.json-rpc :as json-rpc]
            [status-im.i18n.i18n :as i18n]
            [status-im.multiaccounts.update.core :as multiaccounts.update]
            [status-im.node.core :as node]
            [status-im.utils.mobile-sync :as mobile-network-utils]
            [status-im.navigation :as navigation]
            [status-im.utils.fx :as fx]
            [taoensso.timbre :as log]))

;; How do mailserver work ?
;;
;; - We send a request to the mailserver, we are only interested in the
;; messages since `last-request` up to the last seven days
;; and the last 24 hours for topics that were just joined
;; - The mailserver doesn't directly respond to the request and
;; instead we start receiving messages in the filters for the requested
;; topics.
;; - If the mailserver was not ready when we tried for instance to request
;; the history of a topic after joining a chat, the request will be done
;; as soon as the mailserver becomes available


(defn connected? [db id]
  (= (:mailserver/current-id db) id))

(defn fetch [db id]
  (get-in db [:mailserver/mailservers (node/current-fleet-key db) id]))

(defn fetch-current [db]
  (fetch db (:mailserver/current-id db)))

(defn preferred-mailserver-id [db]
  (get-in db [:multiaccount :pinned-mailservers (node/current-fleet-key db)]))

(defn connection-error-dismissed [db]
  (get-in db [:mailserver/connection-error-dismissed]))

(defn mailserver-address->id [db address]
  (let [current-fleet (node/current-fleet-key db)]
    (:id (some #(when (= address (:address %))
                  %)
               (-> db
                   :mailserver/mailservers
                   current-fleet
                   vals)))))

(fx/defn disconnect-from-mailserver
  {:events [::disconnect-from-mailserver]}
  [{:keys [db] :as cofx}]
  {:db (-> db
           (assoc :mailserver/state nil)
           (dissoc :mailserver/current-request :mailserver/fetching-gaps-in-progress))})

(defn fetch-use-mailservers? [{:keys [db]}]
  (get-in db [:multiaccount :use-mailservers?]))

(defonce showing-connection-error-popup? (atom false))

(defn cancel-connection-popup! []
  (reset! showing-connection-error-popup? false)
  (re-frame/dispatch [:mailserver.ui/dismiss-connection-error true]))

(re-frame/reg-fx
 ::cancel-connection-popup
 cancel-connection-popup!)

(fx/defn show-connection-error! [cofx current-fleet preferred-mailserver]
  (reset! showing-connection-error-popup? true)
  {:ui/show-confirmation
   {:title               (i18n/label :t/mailserver-error-title)
    :content             (i18n/label :t/mailserver-error-content)
    :confirm-button-text (i18n/label :t/mailserver-pick-another)
    :on-cancel           cancel-connection-popup!
    :on-accept           #(do
                            (reset! showing-connection-error-popup? false)
                            (re-frame/dispatch [:mailserver.ui/dismiss-connection-error true])
                            (re-frame/dispatch [:navigate-to :offline-messaging-settings]))
    :extra-options       [{:text    (i18n/label :t/mailserver-retry)
                           :onPress #(do
                                       (reset! showing-connection-error-popup? false)
                                       (re-frame/dispatch
                                        [:mailserver.ui/connect-confirmed
                                         current-fleet
                                         preferred-mailserver]))
                           :style   "default"}]}})

(fx/defn handle-successful-request
  {:events [::request-success]}
  [{:keys [db] :as cofx} response-js]
  {:db       (dissoc db :mailserver/current-request)
   :dispatch [:sanitize-messages-and-process-response response-js]})

(fx/defn handle-mailserver-not-working [{:keys [db] :as cofx}]
  (let [current-fleet (node/current-fleet-key db)
        error-dismissed? (connection-error-dismissed db)
        pinned-mailserver  (get-in db [:multiaccount :pinned-mailservers current-fleet])]
    (when (and pinned-mailserver
               (not error-dismissed?)
               (not @showing-connection-error-popup?))
      (show-connection-error! cofx current-fleet pinned-mailserver))))

(fx/defn handle-request-error
  {:events [::request-error]}
  [{:keys [db] :as cofx}]
  {:db (dissoc db :mailserver/current-request)})

(fx/defn process-next-messages-request
  {:events [::request-messages]}
  [{:keys [db now] :as cofx}]
  (when (and
         (:messenger/started? db)
         (mobile-network-utils/syncing-allowed? cofx)
         (fetch-use-mailservers? cofx)
         (not (:mailserver/current-request db)))
    {:db (assoc db :mailserver/current-request true)
     ::json-rpc/call [{:method "wakuext_requestAllHistoricMessagesWithRetries"
                       :params []
                       :js-response true
                       :on-success #(do
                                      (log/info "fetched historical messages")
                                      (re-frame/dispatch [::request-success %]))
                       :on-error #(do
                                    (log/error "failed retrieve historical messages" %)
                                    (re-frame/dispatch [::request-error]))}]}))

(fx/defn handle-mailserver-changed
  [{:keys [db] :as cofx} ms]
  (if (seq ms)
    {:db (assoc db :mailserver/state :connecting
                :mailserver/current-id (keyword ms))}
    {:db (assoc db :mailserver/state nil)}))

(fx/defn handle-mailserver-available
  [{:keys [db] :as cofx} ms]
  {::cancel-connection-popup []
   :db (assoc db :mailserver/state :connected
              :mailserver/current-id (keyword ms))})

(fx/defn connected-to-mailserver
  [{:keys [db] :as cofx}]
  (let [{:keys [address]}       (fetch-current db)]
    (fx/merge
     cofx
     {:mailserver/update-mailservers [[address] #(re-frame/dispatch [::request-messages])]})))

(fx/defn handle-request-success
  {:events [:mailserver.callback/request-success]}
  [{{:keys [chats] :as db} :db} {:keys [request-id topics]}]
  (when (:mailserver/current-request db)
    {:db (assoc-in db [:mailserver/current-request :request-id]
                   request-id)}))

(fx/defn toggle-use-mailservers
  [cofx value]
  {::json-rpc/call
   [{:method "wakuext_toggleUseMailservers"
     :params [value]
     :on-success #(log/info "successfully toggled use-mailservers" value)
     :on-failure #(log/error "failed to toggle use-mailserver" value %)}]})

(fx/defn update-use-mailservers
  {:events [:mailserver.ui/use-history-switch-pressed]}
  [cofx use-mailservers?]
  (fx/merge cofx
            (multiaccounts.update/optimistic :use-mailservers? use-mailservers?)
            (toggle-use-mailservers use-mailservers?)
            (when use-mailservers?
              (disconnect-from-mailserver))))

(fx/defn retry-next-messages-request
  {:events [:mailserver.ui/retry-request-pressed]}
  [{:keys [db] :as cofx}]
  (fx/merge cofx
            {:db (dissoc db :mailserver/request-error)}
            (process-next-messages-request)))

(fx/defn show-request-error-popup
  {:events [:mailserver.ui/request-error-pressed]}
  [{:keys [db]}]
  (let [mailserver-error (:mailserver/request-error db)]
    {:utils/show-confirmation
     {:title (i18n/label :t/mailserver-request-error-title)
      :content (i18n/label :t/mailserver-request-error-content
                           {:error mailserver-error})
      :on-accept #(re-frame/dispatch [:mailserver.ui/retry-request-pressed])
      :confirm-button-text (i18n/label :t/mailserver-request-retry)}}))

(def enode-url-regex
  #"enode://[a-zA-Z0-9]+\@\b\d{1,3}\.\d{1,3}\.\d{1,3}\.\d{1,3}\b:(\d{1,5})")

(defn- extract-url-components [address]
  (rest (re-matches #"enode://(.*)@(.*)" address)))

(defn valid-enode-url? [address]
  (re-matches enode-url-regex address))

(defn build-url [address]
  (let [[initial host] (extract-url-components address)]
    (str "enode://" initial "@" host)))

(fx/defn set-input
  {:events [:mailserver.ui/input-changed]}
  [{:keys [db]} input-key value]
  {:db (update
        db
        :mailserver.edit/mailserver
        assoc
        input-key
        {:value value
         :error (case input-key
                  :id   false
                  :name (string/blank? value)
                  :url  (not (valid-enode-url? value)))})})

(defn- address->mailserver [address]
  (let [[enode url :as response] (extract-url-components address)]
    {:address (if (seq response)
                (str "enode://" enode "@" url)
                address)
     :custom true}))

(defn- build [id mailserver-name address]
  (assoc (address->mailserver address)
         :id id
         :name mailserver-name))

(def default? (comp not :custom fetch))

(fx/defn edit
  {:events [:mailserver.ui/custom-mailserver-selected]}
  [{:keys [db] :as cofx} id]
  (let [{:keys [id address name]} (fetch db id)
        url (when address (build-url address))]
    (fx/merge cofx
              (set-input :id id)
              (set-input :url (str url))
              (set-input :name (str name))
              (navigation/navigate-to-cofx :edit-mailserver nil))))

(defn mailserver->rpc
  [mailserver current-fleet]
  (-> mailserver
      (assoc :fleet (name current-fleet))
      (update :id name)))

(fx/defn upsert
  {:events [:mailserver.ui/save-pressed]
   :interceptors [(re-frame/inject-cofx :random-id-generator)]}
  [{{:mailserver.edit/keys [mailserver] :keys [multiaccount] :as db} :db
    random-id-generator :random-id-generator :as cofx}]

  (let [{:keys [name url id]} mailserver
        current-fleet (node/current-fleet-key db)]
    (when (and (not (string/blank? (:value name)))
               (valid-enode-url? (:value url)))

      (let [mailserver (build
                        (or (:value id)
                            (keyword (string/replace (random-id-generator) "-" "")))
                        (:value name)
                        (:value url))
            current (connected? db (:id mailserver))]
        {:db (-> db
                 (dissoc :mailserver.edit/mailserver)
                 (assoc-in [:mailserver/mailservers current-fleet (:id mailserver)]
                           mailserver))
         ::json-rpc/call
         [{:method "mailservers_addMailserver"
           :params [(mailserver->rpc mailserver current-fleet)]
           :on-success (fn []
                         ;; we naively logout if the user is connected to
                         ;; the edited mailserver
                         (when current
                           (re-frame/dispatch
                            [:multiaccounts.logout.ui/logout-confirmed]))
                         (log/debug "saved mailserver" id "successfuly"))
           :on-failure #(log/error "failed to save mailserver" id %)}]
         :dispatch [:navigate-back]}))))

(defn can-delete?
  [db id]
  (not (or (default? db id)
           (connected? db id))))

(fx/defn delete
  {:events [:mailserver.ui/delete-confirmed]}
  [{:keys [db] :as cofx} id]
  (if (can-delete? db id)
    {:db (-> db
             (update-in
              [:mailserver/mailservers (node/current-fleet-key db)]
              dissoc id)
             (dissoc :mailserver.edit/mailserver))
     ::json-rpc/call
     [{:method "mailservers_deleteMailserver"
       :params [(name id)]
       :on-success #(log/debug "deleted mailserver" id)
       :on-failure #(log/error "failed to delete mailserver" id %)}]
     :dispatch [:navigate-back]}
    {:dispatch [:navigate-back]}))

(fx/defn show-connection-confirmation
  {:events [:mailserver.ui/default-mailserver-selected :mailserver.ui/connect-pressed]}
  [{:keys [db]} mailserver-id]
  (let [current-fleet (node/current-fleet-key db)]
    {:ui/show-confirmation
     {:title (i18n/label :t/close-app-title)
      :content
      (i18n/label :t/connect-mailserver-content
                  {:name (get-in db [:mailserver/mailservers
                                     current-fleet mailserver-id :name])})
      :confirm-button-text (i18n/label :t/close-app-button)
      :on-accept
      #(re-frame/dispatch
        [:mailserver.ui/connect-confirmed current-fleet mailserver-id])
      :on-cancel nil}}))

(fx/defn show-delete-confirmation
  {:events [:mailserver.ui/delete-pressed]}
  [{:keys [db]} mailserver-id]
  {:ui/show-confirmation
   {:title               (i18n/label :t/delete-mailserver-title)
    :content             (i18n/label :t/delete-mailserver-are-you-sure)
    :confirm-button-text (i18n/label :t/delete-mailserver)
    :on-accept           #(re-frame/dispatch
                           [:mailserver.ui/delete-confirmed mailserver-id])}})

(fx/defn set-url-from-qr
  {:events [:mailserver.callback/qr-code-scanned]}
  [cofx url]
  (assoc (set-input cofx :url url)
         :dispatch [:navigate-back]))

(fx/defn dismiss-connection-error
  {:events [:mailserver.ui/dismiss-connection-error]}
  [{:keys [db]} new-state]
  {:db (assoc db :mailserver/connection-error-dismissed new-state)})

(fx/defn pin-mailserver
  {:events [:mailserver.ui/connect-confirmed]}
  [{:keys [db] :as cofx} current-fleet mailserver-id]
  (let [pinned-mailservers (-> db
                               (get-in [:multiaccount :pinned-mailservers])
                               (assoc current-fleet mailserver-id))]
    (fx/merge cofx
              {:db (assoc db :mailserver/current-id mailserver-id)
               ::json-rpc/call [{:method "wakuext_setPinnedMailservers"
                                 :params [pinned-mailservers]
                                 :on-success #(log/info "successfully pinned mailserver")
                                 :on-error #(log/error "failed to pin mailserver" %)}]}
              (multiaccounts.update/optimistic :pinned-mailservers pinned-mailservers))))

(fx/defn unpin
  {:events [:mailserver.ui/unpin-pressed]}
  [{:keys [db] :as cofx}]
  (let [current-fleet (node/current-fleet-key db)
        pinned-mailservers  (-> db
                                (get-in [:multiaccount :pinned-mailservers])
                                (dissoc current-fleet))]
    (fx/merge cofx
              {::json-rpc/call [{:method "wakuext_setPinnedMailservers"
                                 :params [pinned-mailservers]
                                 :on-success #(log/info "successfully unpinned mailserver")
                                 :on-error #(log/error "failed to unpin mailserver" %)}]}
              (multiaccounts.update/optimistic
               :pinned-mailservers pinned-mailservers)
              (dismiss-connection-error false))))

(fx/defn pin
  {:events [:mailserver.ui/pin-pressed]}
  [{:keys [db] :as cofx}]
  (let [current-fleet (node/current-fleet-key db)
        mailserver-id (:mailserver/current-id db)
        pinned-mailservers (get-in db [:multiaccount :pinned-mailservers])]
    (fx/merge cofx
              (multiaccounts.update/multiaccount-update
               :pinned-mailservers (assoc pinned-mailservers
                                          current-fleet
                                          mailserver-id)
               {})
              (dismiss-connection-error false))))

(fx/defn mailserver-ui-add-pressed
  {:events [:mailserver.ui/add-pressed]}
  [{:keys [db] :as cofx}]
  (fx/merge cofx
            {:db (dissoc db :mailserver.edit/mailserver)}
            (navigation/navigate-to-cofx :edit-mailserver nil)))
