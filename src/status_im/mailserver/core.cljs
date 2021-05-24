(ns ^{:doc "Mailserver events and API"}
 status-im.mailserver.core
  (:require [clojure.string :as string]
            [re-frame.core :as re-frame]
            [status-im.ethereum.json-rpc :as json-rpc]
            [status-im.i18n.i18n :as i18n]
            [status-im.mailserver.constants :as constants]
            [status-im.multiaccounts.update.core :as multiaccounts.update]
            [status-im.native-module.core :as status]
            [status-im.node.core :as node]
            [status-im.utils.mobile-sync :as mobile-network-utils]
            [status-im.navigation :as navigation]
            [status-im.utils.config :as config]
            [status-im.utils.fx :as fx]
            [status-im.utils.handlers :as handlers]
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

(def whisper-opts
  {;; time drift that is tolerated by whisper, in seconds
   :whisper-drift-tolerance 10
   ;; ttl of 10 sec
   :ttl                     10
   :powTarget               config/pow-target
   :powTime                 config/pow-time})

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

(defn get-selected-mailserver
  "Use the preferred mailserver if set & exists"
  [db]
  (let [preference    (preferred-mailserver-id db)]
    (when (and preference
               (fetch db preference))
      preference)))

;; We now wait for a confirmation from the mailserver before marking the message
;; as sent.
(defn update-mailservers! [enodes on-success]
  (json-rpc/call
   {:method     (json-rpc/call-ext-method "updateMailservers")
    :params     [enodes]
    :on-success #(do
                   (log/debug "mailserver: update-mailservers success" %)
                   (when on-success
                     (on-success)))
    :on-error   #(log/error "mailserver: update-mailservers error" %)}))

(defn remove-peer! [enode]
  (let [args    {:jsonrpc "2.0"
                 :id      2
                 :method  "admin_removePeer"
                 :params  [enode]}
        payload (.stringify js/JSON (clj->js args))]
    (when enode
      (status/call-private-rpc payload
                               (handlers/response-handler
                                #(log/info "mailserver: remove-peer success" %)
                                #(log/error "mailserver: remove-peer error" %))))))

(re-frame/reg-fx
 :mailserver/remove-peer
 (fn [enode]
   (remove-peer! enode)))

(re-frame/reg-fx
 :mailserver/update-mailservers
 (fn [[enodes on-success]]
   (update-mailservers! enodes on-success)))

(defn registered-peer?
  "truthy if the enode is a registered peer"
  [peers enode]
  (let [registered-enodes (into #{} (map :enode) peers)]
    (contains? registered-enodes enode)))

(defn update-mailserver-state [db state]
  (assoc db :mailserver/state state))

(fx/defn add-peer
  [{:keys [db now] :as cofx}]
  (let [{:keys [address] :as mailserver}
        (fetch-current db)]
    {:db (assoc
          (update-mailserver-state db :connecting)
          :mailserver/last-connection-attempt now)
     ;; Any message sent before this takes effect will not be marked as sent
     ;; probably we should improve the UX so that is more transparent to the
     ;; user
     :mailserver/update-mailservers [[address]]}))

(fx/defn disconnect-from-mailserver
  [{:keys [db] :as cofx}]
  (let [{:keys [address]}       (fetch-current db)
        {:keys [peers-summary]} db]
    {:db (dissoc db :mailserver/current-request :mailserver/fetching-gaps-in-progress)
     :mailserver/remove-peer address}))

(defn fetch-use-mailservers? [{:keys [db]}]
  (get-in db [:multiaccount :use-mailservers?]))

(defn pool-size [fleet-size]
  (.ceil js/Math (/ fleet-size 4)))

(fx/defn get-mailservers-latency
  [{:keys [db] :as cofx}]
  (let [current-fleet (node/current-fleet-key db)
        addresses (mapv :address (-> db
                                     :mailserver/mailservers
                                     current-fleet
                                     vals))]
    {::json-rpc/call [{:method "mailservers_ping"
                       :params [{:addresses addresses
                                 :timeoutMs 500}]
                       :on-success
                       #(re-frame/dispatch [::get-latency-callback %])}]}))

(fx/defn log-mailserver-failure [{:keys [db now]}]
  (when-let [mailserver (fetch-current db)]
    {:db (assoc-in db [:mailserver/failures (:address mailserver)] now)}))

(defn sort-mailservers
  "Sort mailservers sorts the mailservers by recent failures, and by rtt
  for breaking ties"
  [{:keys [now db]} mailservers]
  (let [mailserver-failures (:mailserver/failures db)
        sort-fn (fn [a b]
                  (let [failures-a (get mailserver-failures (:address a))
                        failures-b (get mailserver-failures (:address b))
                        has-a-failed? (boolean
                                       (and failures-a (<= (- now failures-a) constants/cooloff-period)))
                        has-b-failed? (boolean
                                       (and failures-b (<= (- now failures-b) constants/cooloff-period)))]
                    ;; If both have failed, or none of them, then compare rtt
                    (cond
                      (= has-a-failed? has-b-failed?)
                      (compare (:rttMs a) (:rttMs b))
                     ;; Otherwise prefer the one that has not failed recently
                      has-a-failed? 1
                      has-b-failed? -1)))]
    (sort sort-fn mailservers)))

(defn get-mailserver-when-ready
  "return the mailserver if the mailserver is ready"
  [{:keys [db]}]
  (let [mailserver (fetch-current db)
        mailserver-state (:mailserver/state db)]
    (when (= :connected mailserver-state)
      mailserver)))

(fx/defn handle-successful-request
  {:events [::request-success]}
  [{:keys [db]} response-js]
  {:db (dissoc db :mailserver/current-request)})

(fx/defn process-next-messages-request
  {:events [::request-messages]}
  [{:keys [db now] :as cofx}]
  (when (and
         (:messenger/started? db)
         (mobile-network-utils/syncing-allowed? cofx)
         (fetch-use-mailservers? cofx)
         (not (:mailserver/current-request db)))
    (when-let [mailserver (get-mailserver-when-ready cofx)]
      {:db (assoc db :mailserver/current-request true)
       ::json-rpc/call [{:method "wakuext_requestAllHistoricMessages"
                         :params []
                         :js-response true
                         :on-success #(do
                                        (log/info "fetched historical messages")
                                        (re-frame/dispatch [::request-success %]))
                         :on-failure #(log/error "failed retrieve historical messages" %)}]})))

(fx/defn connected-to-mailserver
  [{:keys [db] :as cofx}]
  (let [{:keys [address]}       (fetch-current db)]
    (fx/merge
     cofx
     {:db (update-mailserver-state db :connected)
      :mailserver/update-mailservers [[address] #(re-frame/dispatch [::request-messages])]})))

(fx/defn connect-to-mailserver
  "Add mailserver as a peer using `::add-peer` cofx and generate sym-key when
  it doesn't exists
  Peer summary will change and we will receive a signal from status go when
  this is successful
  A connection-check is made after `connection timeout` is reached and
  mailserver-state is changed to error if it is not connected by then
  No attempt is made if mailserver usage is disabled"
  {:events [:mailserver.ui/reconnect-mailserver-pressed]}
  [{:keys [db] :as cofx}]
  (let [{:keys [address]}       (fetch-current db)
        {:keys [peers-summary]} db
        use-mailservers?        (fetch-use-mailservers? cofx)
        added?                  (registered-peer? peers-summary address)]
    (when use-mailservers?
      (fx/merge cofx
                {:db (dissoc db :mailserver/current-request)}
                (if added?
                  (connected-to-mailserver)
                  (add-peer))))))

(fx/defn set-current-mailserver-with-lowest-latency
  "Picks a random mailserver amongs the ones with the lowest latency
   The results with error are ignored
   The pool size is 1/4 of the mailservers were pinged successfully"
  {:events [::get-latency-callback]}
  [{:keys [db] :as cofx} latency-results]
  (let [successful-pings (remove :error latency-results)]
    (when (seq successful-pings)
      (let [address (-> (take (pool-size (count successful-pings))
                              (sort-mailservers cofx successful-pings))
                        rand-nth
                        :address)
            mailserver-id (mailserver-address->id db address)]
        (fx/merge cofx
                  {:db (assoc db :mailserver/current-id mailserver-id)}
                  (connect-to-mailserver))))))

(fx/defn set-current-mailserver
  [{:keys [db] :as cofx}]
  (if-let [mailserver-id (get-selected-mailserver db)]
    (fx/merge cofx
              {:db (assoc db :mailserver/current-id mailserver-id)}
              (connect-to-mailserver))
    (get-mailservers-latency cofx)))

(fx/defn peers-summary-change
  "There is only 2 summary changes that require mailserver action:
  - mailserver disconnected: we try to reconnect
  - mailserver connected: we mark the mailserver as trusted peer"
  [{:keys [db] :as cofx} previous-summary]
  (when (:multiaccount db)
    (if (:mailserver/current-id db)
      (let [{:keys [peers-summary peers-count]} db
            {:keys [address] :as mailserver} (fetch-current db)
            mailserver-was-registered? (registered-peer? previous-summary
                                                         address)
            mailserver-is-registered?  (registered-peer? peers-summary
                                                         address)
            mailserver-added?          (and mailserver-is-registered?
                                            (not mailserver-was-registered?))
            mailserver-removed?        (and mailserver-was-registered?
                                            (not mailserver-is-registered?))]
        (cond
          mailserver-added?
          (connected-to-mailserver cofx)
          mailserver-removed?
          (connect-to-mailserver cofx)))
        ;; if there is no current mailserver defined
        ;; we set it first
      (set-current-mailserver cofx))))

(fx/defn handle-request-success
  {:events [:mailserver.callback/request-success]}
  [{{:keys [chats] :as db} :db} {:keys [request-id topics]}]
  (when (:mailserver/current-request db)
    {:db (assoc-in db [:mailserver/current-request :request-id]
                   request-id)}))

(fx/defn update-use-mailservers
  {:events [:mailserver.ui/use-history-switch-pressed]}
  [cofx use-mailservers?]
  (fx/merge cofx
            (multiaccounts.update/multiaccount-update :use-mailservers? use-mailservers? {})
            (if use-mailservers?
              (connect-to-mailserver)
              (disconnect-from-mailserver))))

(defonce showing-connection-error-popup? (atom false))

(defn show-connection-error! [db current-fleet preferred-mailserver]
  (reset! showing-connection-error-popup? true)
  (assoc db :ui/show-confirmation
         {:title               (i18n/label :t/mailserver-error-title)
          :content             (i18n/label :t/mailserver-error-content)
          :confirm-button-text (i18n/label :t/mailserver-pick-another)
          :on-cancel           #(do
                                  (reset! showing-connection-error-popup? false)
                                  (re-frame/dispatch [:mailserver.ui/dismiss-connection-error true]))
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
                                 :style   "default"}]}))

(fx/defn change-mailserver
  "mark mailserver status as `:error` if custom mailserver is used
  otherwise try to reconnect to another mailserver"
  [{:keys [db] :as cofx}]
  (when (and (fetch-use-mailservers? cofx)
             ;; For some reason the tests are checking
             ;; for non-zero, so nil value is ok, not
             ;; sure is intentional, but will leave it as it is
             ;; instead of using pos?
             (not (zero? (:peers-count db))))
    (if-let [preferred-mailserver (preferred-mailserver-id db)]
      (let [error-dismissed? (connection-error-dismissed db)
            current-fleet (node/current-fleet-key db)]
        ;; Error connecting to the mail server
        (cond->  {:db (update-mailserver-state db :error)}
          (not (or error-dismissed? @showing-connection-error-popup?))
          (show-connection-error! current-fleet preferred-mailserver)))
      (let [{:keys [address]} (fetch-current db)]
        (fx/merge cofx
                  {:mailserver/remove-peer address}
                  (set-current-mailserver))))))

(defn check-connection! []
  (re-frame/dispatch [:mailserver/check-connection-timeout]))

(fx/defn check-connection
  "Check connection checks that the connection is successfully connected,
  otherwise it will try to change mailserver and connect again"
  {:events [:mailserver/check-connection-timeout]}
  [{:keys [db now] :as cofx}]
  ;; check if logged into multiaccount
  (when (contains? db :multiaccount)
    (let [last-connection-attempt (:mailserver/last-connection-attempt db)]
      (if (and (fetch-use-mailservers? cofx)
                 ;; We are not connected
               (not= :connected (:mailserver/state db))
                 ;; We either never tried to connect to this peer
               (or (nil? last-connection-attempt)
                     ;; Or 30 seconds have passed and no luck
                   (>= (- now last-connection-attempt) (* constants/connection-timeout 3))))
        ;; Then we change mailserver
        (change-mailserver cofx)
        ;; Just make sure it's set
        (let [{:keys [address] :as mailserver}
              (fetch-current db)]
          (when address
            {:mailserver/update-mailservers [[address]]}))))))

(fx/defn retry-next-messages-request
  {:events [:mailserver.ui/retry-request-pressed]}
  [{:keys [db] :as cofx}]
  (fx/merge cofx
            {:db (dissoc db :mailserver/request-error)}
            (process-next-messages-request)))

;; At some point we should update `last-request`, as eventually we want to move
;; on, rather then keep asking for the same data, say after n amounts of attempts
(fx/defn handle-request-error
  [{:keys [db]} error]
  {:db (-> db
           (assoc  :mailserver/request-error error)
           (dissoc :mailserver/current-request
                   :mailserver/pending-requests))})

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

(fx/defn initialize-mailserver
  [{:keys [db] :as cofx}]
  (fx/merge cofx
            {:db db}
            (set-current-mailserver)
            (process-next-messages-request)))

(def enode-address-regex
  #"enode://[a-zA-Z0-9]+\@\b\d{1,3}\.\d{1,3}\.\d{1,3}\.\d{1,3}\b:(\d{1,5})")
(def enode-url-regex
  #"enode://[a-zA-Z0-9]+:(.+)\@\b\d{1,3}\.\d{1,3}\.\d{1,3}\.\d{1,3}\b:(\d{1,5})")

(defn- extract-address-components [address]
  (rest (re-matches #"enode://(.*)@(.*)" address)))

(defn- extract-url-components [address]
  (rest (re-matches #"enode://(.*?):(.*)@(.*)" address)))

(defn valid-enode-url? [address]
  (re-matches enode-url-regex address))

(defn valid-enode-address? [address]
  (re-matches enode-address-regex address))

(defn build-url [address password]
  (let [[initial host] (extract-address-components address)]
    (str "enode://" initial ":" password "@" host)))

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
  (let [[enode password url :as response] (extract-url-components address)]
    (cond-> {:address      (if (seq response)
                             (str "enode://" enode "@" url)
                             address)
             :user-defined true}
      password (assoc :password password))))

(defn- build [id mailserver-name address]
  (assoc (address->mailserver address)
         :id id
         :name mailserver-name))

(def default? (comp not :user-defined fetch))

(fx/defn edit
  {:events [:mailserver.ui/user-defined-mailserver-selected]}
  [{:keys [db] :as cofx} id]
  (let [{:keys [id address password name]} (fetch db id)
        url (when address (build-url address password))]
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

(fx/defn save-settings
  {:events [:mailserver.ui/connect-confirmed]}
  [{:keys [db] :as cofx} current-fleet mailserver-id]
  (let [{:keys [address]} (fetch-current db)
        pinned-mailservers (get-in db [:multiaccount :pinned-mailservers])
        ;; Check if previous mailserver was pinned
        pinned?  (get pinned-mailservers current-fleet)
        use-mailservers? (fetch-use-mailservers? cofx)]
    (fx/merge cofx
              {:db (assoc db :mailserver/current-id mailserver-id)
               :mailserver/remove-peer address}
              (when use-mailservers? (connect-to-mailserver))
              (dismiss-connection-error false)
              (when pinned?
                (multiaccounts.update/multiaccount-update
                 :pinned-mailservers (assoc pinned-mailservers
                                            current-fleet
                                            mailserver-id)
                 {})))))

(fx/defn unpin
  {:events [:mailserver.ui/unpin-pressed]}
  [{:keys [db] :as cofx}]
  (let [current-fleet (node/current-fleet-key db)
        pinned-mailservers (get-in db [:multiaccount :pinned-mailservers])]
    (fx/merge cofx
              (multiaccounts.update/multiaccount-update
               :pinned-mailservers (dissoc pinned-mailservers current-fleet)
               {})
              (dismiss-connection-error false)
              (change-mailserver))))

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
