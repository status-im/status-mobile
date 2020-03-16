(ns ^{:doc "Mailserver events and API"}
 status-im.mailserver.core
  (:require [clojure.string :as string]
            [re-frame.core :as re-frame]
            [status-im.data-store.mailservers :as data-store.mailservers]
            [status-im.ethereum.json-rpc :as json-rpc]
            [status-im.waku.core :as waku]
            [status-im.node.core :as node]
            [status-im.i18n :as i18n]
            [status-im.mailserver.constants :as constants]
            [status-im.mailserver.topics :as mailserver.topics]
            [status-im.multiaccounts.model :as multiaccounts.model]
            [status-im.multiaccounts.update.core :as multiaccounts.update]
            [status-im.native-module.core :as status]
            [status-im.transport.utils :as transport.utils]
            [status-im.ui.screens.mobile-network-settings.utils
             :as
             mobile-network-utils]
            [status-im.ui.screens.navigation :as navigation]
            [status-im.utils.config :as config]
            [status-im.utils.fx :as fx]
            [status-im.utils.handlers :as handlers]
            [status-im.utils.platform :as platform]
            [status-im.utils.random :as rand]
            [status-im.utils.utils :as utils]
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


(def limit (atom constants/default-limit))
(def success-counter (atom 0))

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
  (let [current-fleet (node/current-fleet-key db)
        current-id    (:mailserver/current-id db)
        preference    (preferred-mailserver-id db)]
    (when (and preference
               (fetch db preference))
      preference)))

(defn add-peer! [enode]
  (status/add-peer enode
                   (handlers/response-handler
                    #(log/debug "mailserver: add-peer success" %)
                    #(log/error "mailserver: add-peer error" %))))

;; We now wait for a confirmation from the mailserver before marking the message
;; as sent.
(defn update-mailservers! [waku-enabled? enodes]
  (json-rpc/call
   {:method (json-rpc/call-ext-method waku-enabled? "updateMailservers")
    :params [enodes]
    :on-success #(log/debug "mailserver: update-mailservers success" %)
    :on-error #(log/error "mailserver: update-mailservers error" %)}))

(defn remove-peer! [enode]
  (let [args    {:jsonrpc "2.0"
                 :id      2
                 :method  "admin_removePeer"
                 :params  [enode]}
        payload (.stringify js/JSON (clj->js args))]
    (status/call-private-rpc payload
                             (handlers/response-handler
                              #(log/debug "mailserver: remove-peer success" %)
                              #(log/error "mailserver: remove-peer error" %)))))

(re-frame/reg-fx
 :mailserver/add-peer
 (fn [enode]
   (add-peer! enode)))

(re-frame/reg-fx
 :mailserver/remove-peer
 (fn [enode]
   (remove-peer! enode)))

(re-frame/reg-fx
 :mailserver/update-mailservers
 (fn [[waku-enabled? enodes]]
   (update-mailservers! waku-enabled? enodes)))

(defn decrease-limit []
  (max constants/min-limit (/ @limit 2)))

(defn increase-limit []
  (min constants/max-limit (* @limit 2)))

(re-frame/reg-fx
 :mailserver/set-limit
 (fn [n]
   (reset! limit n)))

(re-frame/reg-fx
 :mailserver/increase-limit
 (fn []
   (if (>= @success-counter 2)
     (reset! limit (increase-limit))
     (swap! success-counter inc))))

(re-frame/reg-fx
 :mailserver/decrease-limit
 (fn []
   (reset! limit (decrease-limit))
   (reset! success-counter 0)))

(defn mark-trusted-peer! [waku-enabled? enode]
  (json-rpc/call
   {:method  (if waku-enabled?
               "waku_markTrustedPeer"
               "shh_markTrustedPeer")
    :params [enode]
    :on-success
    #(re-frame/dispatch [:mailserver.callback/mark-trusted-peer-success %])
    :on-error
    #(re-frame/dispatch [:mailserver.callback/mark-trusted-peer-error %])}))

(re-frame/reg-fx
 :mailserver/mark-trusted-peer
 (fn [[waku-enabled? enode]]
   (mark-trusted-peer! waku-enabled? enode)))

(fx/defn generate-mailserver-symkey
  [{:keys [db] :as cofx} {:keys [password id] :as mailserver}]
  (let [current-fleet (node/current-fleet-key db)]
    {:db (assoc-in db [:mailserver/mailservers current-fleet id
                       :generating-sym-key?]
                   true)
     :shh/generate-sym-key-from-password
     [(waku/enabled? cofx)
      {:password    password
       :on-success
       (fn [_ sym-key-id]
         (re-frame/dispatch
          [:mailserver.callback/generate-mailserver-symkey-success
           mailserver sym-key-id]))
       :on-error   #(log/error "mailserver: get-sym-key error" %)}]}))

(defn registered-peer?
  "truthy if the enode is a registered peer"
  [peers enode]
  (let [registered-enodes (into #{} (map :enode) peers)]
    (contains? registered-enodes enode)))

(defn update-mailserver-state [db state]
  (assoc db :mailserver/state state))

(fx/defn mark-trusted-peer
  [{:keys [db] :as cofx}]
  (let [{:keys [address sym-key-id generating-sym-key?] :as mailserver}
        (fetch-current db)]
    (fx/merge cofx
              {:db (update-mailserver-state db :added)
               :mailserver/mark-trusted-peer [(waku/enabled? cofx) address]}
              (when-not (or sym-key-id generating-sym-key?)
                (generate-mailserver-symkey mailserver)))))

(fx/defn add-peer
  [{:keys [db now] :as cofx}]
  (let [{:keys [address sym-key-id generating-sym-key?] :as mailserver}
        (fetch-current db)]
    (fx/merge
     cofx
     {:db (assoc
           (update-mailserver-state db :connecting)
           :mailserver/last-connection-attempt now)
      :mailserver/add-peer address
      ;; Any message sent before this takes effect will not be marked as sent
      ;; probably we should improve the UX so that is more transparent to the
      ;; user
      :mailserver/update-mailservers [(waku/enabled? cofx) [address]]}
     (when-not (or sym-key-id generating-sym-key?)
       (generate-mailserver-symkey mailserver)))))

(defn executing-gap-request?
  [{:mailserver/keys [current-request fetching-gaps-in-progress]}]
  (= (get fetching-gaps-in-progress (:chat-id current-request))
     (select-keys
      current-request
      [:from :to :force-to? :topics :chat-id])))

(fx/defn connect-to-mailserver
  "Add mailserver as a peer using `::add-peer` cofx and generate sym-key when
   it doesn't exists
   Peer summary will change and we will receive a signal from status go when
   this is successful
   A connection-check is made after `connection timeout` is reached and
   mailserver-state is changed to error if it is not connected by then"
  {:events [:mailserver.ui/reconnect-mailserver-pressed]}
  [{:keys [db] :as cofx}]
  (let [{:keys [address]} (fetch-current db)
        {:keys [peers-summary]} db
        added?       (registered-peer? peers-summary address)
        gap-request? (executing-gap-request? db)]
    (fx/merge cofx
              {:db (cond-> (dissoc db :mailserver/current-request)
                     gap-request?
                     (-> (assoc :mailserver/fetching-gaps-in-progress {})
                         (dissoc :mailserver/planned-gap-requests)))}
              (if added?
                (mark-trusted-peer)
                (add-peer)))))

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

(fx/defn set-current-mailserver-with-lowest-latency
  "Picks a random mailserver amongs the ones with the lowest latency
   The results with error are ignored
   The pool size is 1/4 of the mailservers were pinged successfully"
  {:events [::get-latency-callback]}
  [{:keys [db] :as cofx} latency-results]
  (let [successful-pings (remove :error latency-results)]
    (when (seq successful-pings)
      (let [address (-> (take (pool-size (count successful-pings))
                              (sort-by :rttMs successful-pings))
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
  (when (and (not config/nimbus-enabled?) (:multiaccount db))
    (if (:mailserver/current-id db)
      (let [{:keys [peers-summary peers-count]} db
            {:keys [address sym-key-id] :as mailserver} (fetch-current db)
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
          (mark-trusted-peer cofx)
          mailserver-removed?
          (connect-to-mailserver cofx)))
        ;; if there is no current mailserver defined,
        ;; we set it first
      (set-current-mailserver cofx))))

(defn adjust-request-for-transit-time
  [from]
  (let [ttl               (:ttl whisper-opts)
        whisper-tolerance (:whisper-drift-tolerance whisper-opts)
        adjustment    (+ whisper-tolerance ttl)
        adjusted-from (- (max from adjustment) adjustment)]
    (log/debug "Adjusting mailserver request" "from:" from
               "adjusted-from:" adjusted-from)
    adjusted-from))

(defn chats->never-synced-public-chats [chats]
  (into {} (filter (fn [[k v]] (:might-have-join-time-messages? v)) chats)))

(fx/defn handle-request-success [{{:keys [chats] :as db} :db}
                                 {:keys [request-id topics]}]
  (when (:mailserver/current-request db)
    (let [by-topic-never-synced-chats
          (reduce-kv
           #(assoc %1 (transport.utils/get-topic %2) %3)
           {}
           (chats->never-synced-public-chats chats))
          never-synced-chats-in-this-request
          (select-keys by-topic-never-synced-chats (vec topics))]
      (if (seq never-synced-chats-in-this-request)
        {:db
         (-> db
             ((fn [db]
                (reduce
                 (fn [db chat]
                   (assoc-in db [:chats (:chat-id chat)
                                 :join-time-mail-request-id] request-id))
                 db
                 (vals never-synced-chats-in-this-request))))
             (assoc-in [:mailserver/current-request :request-id]
                       request-id))}
        {:db (assoc-in db [:mailserver/current-request :request-id]
                       request-id)}))))

(defn request-messages!
  [waku-enabled?
   {:keys [sym-key-id address]}
   {:keys [topics cursor to from force-to?] :as request}]
  ;; Add some room to from, unless we break day boundaries so that
  ;; messages that have been received after the last request are also fetched
  (let [actual-from   (adjust-request-for-transit-time from)
        actual-limit  (or (:limit request)
                          @limit)]
    (log/info "mailserver: request-messages for: "
              " topics " topics
              " from " actual-from
              " force-to? " force-to?
              " to " to
              " range " (- to from)
              " cursor " cursor
              " limit " actual-limit)
    (json-rpc/call
     {:method (json-rpc/call-ext-method waku-enabled? "requestMessages")
      :params [(cond-> {:topics         topics
                        :mailServerPeer address
                        :symKeyID       sym-key-id
                        :timeout        constants/request-timeout
                        :limit          actual-limit
                        :cursor         cursor
                        :from           actual-from}
                 force-to?
                 (assoc :to to))]
      :on-success (fn [request-id]
                    (log/info "mailserver: messages request success for topic "
                              topics "from" from "to" to)
                    (re-frame/dispatch
                     [:mailserver.callback/request-success
                      {:request-id request-id :topics topics}]))
      :on-error (fn [error]
                  (log/error "mailserver: messages request error for topic "
                             topics ": " error)
                  (utils/set-timeout
                   #(re-frame/dispatch
                     [:mailserver.callback/resend-request {:request-id nil}])
                   constants/backoff-interval-ms))})))

(re-frame/reg-fx
 :mailserver/request-messages
 (fn [{:keys [mailserver request waku-enabled?]}]
   (request-messages! waku-enabled? mailserver request)))

(defn get-mailserver-when-ready
  "return the mailserver if the mailserver is ready"
  [{:keys [db] :as cofx}]
  (let [{:keys [sym-key-id] :as mailserver} (fetch-current db)
        mailserver-state (:mailserver/state db)]
    (when (and (= :connected mailserver-state)
               sym-key-id)
      mailserver)))

(defn topic->request
  [default-request-to requests-from requests-to]
  (fn [[topic {:keys [last-request]}]]
    (let [force-request-from (get requests-from topic)
          force-request-to (get requests-to topic)]
      (when (or force-request-from
                (> default-request-to last-request))
        (let [from (or force-request-from
                       (max last-request
                            (- default-request-to constants/max-request-range)))
              to   (or force-request-to default-request-to)]
          {:gap-topics #{topic}
           :from      from
           :to        to
           :force-to? (not (nil? force-request-to))})))))

(defn aggregate-requests
  [acc {:keys [gap-topics from to force-to? gap chat-id]}]
  (when from
    (update acc [from to force-to?]
            (fn [{:keys [topics]}]
              {:topics    ((fnil clojure.set/union #{}) topics gap-topics)
               :from      from
               :to        to
               ;; To is sent to the mailserver only when force-to? is true,
               ;; also we use to calculate when the last-request was sent.
               :force-to? force-to?
               :gap       gap
               :chat-id   chat-id}))))

(defn prepare-messages-requests
  [{{:keys [:mailserver/requests-from
            :mailserver/requests-to
            :mailserver/topics
            :mailserver/planned-gap-requests]} :db}
   default-request-to]
  (transduce
   (keep (topic->request default-request-to requests-from requests-to))
   (completing aggregate-requests vals)
   (reduce
    aggregate-requests
    {}
    (vals planned-gap-requests))
   topics))

(fx/defn process-next-messages-request
  [{:keys [db now] :as cofx}]
  (when (and
         (:filters/initialized db)
         (mobile-network-utils/syncing-allowed? cofx)
         (not (:mailserver/current-request db)))
    (when-let [mailserver (get-mailserver-when-ready cofx)]
      (let [request-to (or (:mailserver/request-to db)
                           (quot now 1000))
            requests   (prepare-messages-requests cofx request-to)]
        (log/debug "Mailserver: planned requests " requests)
        (if-let [request (first requests)]
          {:db (assoc db
                      :mailserver/pending-requests (count requests)
                      :mailserver/current-request request
                      :mailserver/request-to request-to)
           :mailserver/request-messages {:waku-enabled? (waku/enabled? cofx)
                                         :mailserver mailserver
                                         :request    request}}
          {:db (dissoc db
                       :mailserver/pending-requests
                       :mailserver/request-to
                       :mailserver/requests-from
                       :mailserver/requests-to)})))))

(fx/defn add-mailserver-trusted
  "the current mailserver has been trusted
  update mailserver status to `:connected` and request messages
  if mailserver is ready"
  [{:keys [db] :as cofx}]
  (fx/merge cofx
            {:db (update-mailserver-state db :connected)}
            (process-next-messages-request)))

(fx/defn add-mailserver-sym-key
  "the current mailserver sym-key has been generated
  add sym-key to the mailserver in app-db and request messages if
  mailserver is ready"
  [{:keys [db] :as cofx} {:keys [id]} sym-key-id]
  (let [current-fleet (node/current-fleet-key db)]
    (fx/merge
     cofx
     {:db (-> db
              (assoc-in [:mailserver/mailservers current-fleet id :sym-key-id]
                        sym-key-id)
              (update-in [:mailserver/mailservers current-fleet id]
                         dissoc :generating-sym-key?))}
     (process-next-messages-request))))

(fx/defn change-mailserver
  "mark mailserver status as `:error` if custom mailserver is used
  otherwise try to reconnect to another mailserver"
  [{:keys [db] :as cofx}]
  (when-not (zero? (:peers-count db))
    (if-let [preferred-mailserver (preferred-mailserver-id db)]
      (let [current-fleet (node/current-fleet-key db)]
        {:db
         (update-mailserver-state db :error)
         :ui/show-confirmation
         {:title               (i18n/label :t/mailserver-error-title)
          :content             (i18n/label :t/mailserver-error-content)
          :confirm-button-text (i18n/label :t/mailserver-pick-another)
          :on-accept           #(re-frame/dispatch
                                 [:navigate-to (if platform/desktop?
                                                 :advanced-settings
                                                 :offline-messaging-settings)])
          :extra-options       [{:text    (i18n/label :t/mailserver-retry)
                                 :onPress #(re-frame/dispatch
                                            [:mailserver.ui/connect-confirmed
                                             current-fleet
                                             preferred-mailserver])
                                 :style   "default"}]}})
      (let [{:keys [address]} (fetch-current db)]
        (fx/merge cofx
                  {:mailserver/remove-peer address}
                  (set-current-mailserver))))))

(defn check-connection! []
  (re-frame/dispatch [:mailserver/check-connection-timeout]))

(fx/defn check-connection
  "Check connection checks that the connection is successfully connected,
  otherwise it will try to change mailserver and connect again"
  [{:keys [db now] :as cofx}]
  ;; check if logged into multiaccount
  (when (contains? db :multiaccount)
    (let [last-connection-attempt (:mailserver/last-connection-attempt db)]
      (when (<= (- now last-connection-attempt))
        (fx/merge cofx
                  (when (not= :connected (:mailserver/state db))
                    (change-mailserver)))))))

(fx/defn reset-request-to
  [{:keys [db]}]
  {:db (dissoc db :mailserver/request-to)})

(fx/defn remove-gaps
  [{:keys [db] :as cofx} chat-id]
  (fx/merge cofx
            {:db (update db :mailserver/gaps dissoc chat-id)}
            (data-store.mailservers/delete-gaps-by-chat-id chat-id)))

(fx/defn remove-range
  [{:keys [db]} chat-id]
  {:db (update db :mailserver/ranges dissoc chat-id)
   ::json-rpc/call
   [{:method "mailservers_deleteChatRequestRange"
     :params [chat-id]
     :on-success #(log/debug "deleted chat request range successfully")
     :on-failure #(log/error "failed to delete chat request range" %)}]})

(defn update-mailserver-topic
  [{:keys [last-request] :as config}
   {:keys [request-to]}]
  (cond-> config
    (> request-to last-request)
    (assoc :last-request request-to)))

(defn check-existing-gaps
  [chat-id chat-gaps request]
  (let [request-from (:from request)
        request-to (:to request)]
    (reduce
     (fn [acc {:keys [from to id] :as gap}]
       (cond
         ;; F----T
         ;;         RF---RT
         (< to request-from)
         acc

         ;;          F----T
         ;; RF---RT
         (< request-to from)
         (reduced acc)

         ;;     F------T
         ;; RF-----RT
         (and (<= request-from from)
              (< from request-to to))
         (let [updated-gap (assoc gap
                                  :from request-to
                                  :to to)]
           (reduced
            (update acc :updated-gaps assoc id updated-gap)))

         ;;   F------T
         ;; RF----------RT
         (and (<= request-from from)
              (<= to request-to))
         (update acc :deleted-gaps conj (:id gap))

         ;; F---------T
         ;;     RF-------RT
         (and (< from request-from to)
              (<= to request-to))
         (let [updated-gap (assoc gap
                                  :from from
                                  :to request-from)]
           (update acc :updated-gaps assoc id updated-gap))

         ;; F---------T
         ;;   RF---RT
         (and (< from request-from)
              (< request-to to))
         (reduced
          (-> acc
              (update :deleted-gaps conj (:id gap))
              (update :new-gaps concat [{:chat-id chat-id
                                         :from    from
                                         :to      request-from}
                                        {:chat-id chat-id
                                         :from    request-to
                                         :to      to}])))

         :else acc))
     {}
     (sort-by :from (vals chat-gaps)))))

(defn check-all-gaps
  [gaps chat-ids request]
  (transduce
   (map (fn [chat-id]
          (let [chat-gaps (get gaps chat-id)]
            [chat-id (check-existing-gaps chat-id chat-gaps request)])))
   (completing
    (fn [acc [chat-id {:keys [new-gaps updated-gaps deleted-gaps]}]]
      (cond-> acc
        (seq new-gaps)
        (assoc-in [:new-gaps chat-id] new-gaps)

        (seq updated-gaps)
        (assoc-in [:updated-gaps chat-id] updated-gaps)

        (seq deleted-gaps)
        (assoc-in [:deleted-gaps chat-id] deleted-gaps))))
   {}
   chat-ids))

(fx/defn update-ranges
  [{:keys [db] :as cofx}]
  (let [{:keys [topics from to]}
        (get db :mailserver/current-request)
        chat-ids       (mapcat
                        :chat-ids
                        (-> (:mailserver/topics db)
                            (select-keys topics)
                            vals))
        ranges         (:mailserver/ranges db)
        updated-ranges (into
                        {}
                        (keep
                         (fn [chat-id]
                           (let [chat-id (str chat-id)
                                 {:keys [lowest-request-from
                                         highest-request-to]
                                  :as   range}
                                 (get ranges chat-id)]
                             [chat-id
                              (cond-> (assoc range :chat-id chat-id)
                                (or (nil? highest-request-to)
                                    (> to highest-request-to))
                                (assoc :highest-request-to to)

                                (or (nil? lowest-request-from)
                                    (< from lowest-request-from))
                                (assoc :lowest-request-from from))])))
                        chat-ids)]
    (fx/merge cofx
              {:db (update db :mailserver/ranges merge updated-ranges)
               ::json-rpc/call
               (mapv (fn [chat-requests-range]
                       {:method "mailservers_addChatRequestRange"
                        :params [chat-requests-range]
                        :on-success #()
                        :on-failure
                        #(log/error "failed to save chat request range" %)})
                     (vals updated-ranges))})))

(defn prepare-new-gaps [new-gaps ranges {:keys [from to] :as req} chat-ids]
  (into
   {}
   (comp
    (map (fn [chat-id]
           (let [gaps (get new-gaps chat-id)
                 {:keys [highest-request-to lowest-request-from]}
                 (get ranges chat-id)]
             [chat-id (cond-> gaps
                        (and
                         (not (nil? highest-request-to))
                         (< highest-request-to from))
                        (conj {:chat-id chat-id
                               :from    highest-request-to
                               :to      from})
                        (and
                         (not (nil? lowest-request-from))
                         (< to lowest-request-from))
                        (conj {:chat-id chat-id
                               :from    to
                               :to      lowest-request-from}))])))
    (keep (fn [[chat-id gaps]]
            [chat-id
             (into {}
                   (map (fn [gap]
                          (let [id (rand/guid)]
                            [id (assoc gap :id id)])))
                   gaps)])))
   chat-ids))

(fx/defn update-gaps
  [{:keys [db] :as cofx}]
  (let [{:keys [topics] :as request} (get db :mailserver/current-request)
        chat-ids          (into #{}
                                (comp
                                 (keep #(get-in db [:mailserver/topics %]))
                                 (mapcat :chat-ids)
                                 (map str))
                                topics)

        {:keys [updated-gaps new-gaps deleted-gaps]}
        (check-all-gaps (get db :mailserver/gaps) chat-ids request)

        ranges            (:mailserver/ranges db)
        prepared-new-gaps (prepare-new-gaps new-gaps ranges request chat-ids)]
    (fx/merge
     cofx
     {:db
      (reduce (fn [db chat-id]
                (let [chats-deleted-gaps (get deleted-gaps chat-id)
                      chats-updated-gaps (merge (get updated-gaps chat-id)
                                                (get prepared-new-gaps chat-id))]
                  (update-in db [:mailserver/gaps chat-id]
                             (fn [chat-gaps]
                               (-> (apply dissoc chat-gaps chats-deleted-gaps)
                                   (merge chats-updated-gaps))))))
              db
              chat-ids)}
     (data-store.mailservers/delete-gaps (mapcat val deleted-gaps))
     (data-store.mailservers/save-gaps
      (concat (mapcat vals (vals updated-gaps))
              (mapcat vals (vals prepared-new-gaps)))))))

(fx/defn update-chats-and-gaps
  [cofx cursor]
  (when (or (nil? cursor)
            (and (string? cursor)
                 (clojure.string/blank? cursor)))
    (fx/merge
     cofx
     (update-gaps)
     (update-ranges))))

(defn get-updated-mailserver-topics [db requested-topics from to]
  (into
   {}
   (keep (fn [topic]
           (when-let [config (get-in db [:mailserver/topics topic])]
             [topic (update-mailserver-topic config
                                             {:request-from from
                                              :request-to   to})])))
   requested-topics))

(fx/defn update-mailserver-topics
  "TODO: add support for cursors
  if there is a cursor, do not update `last-request`"
  [{:keys [db now] :as cofx} {:keys [request-id cursor]}]
  (when-let [request (get db :mailserver/current-request)]
    (let [{:keys [from to topics]} request
          mailserver-topics (get-updated-mailserver-topics db topics from to)]
      (log/info "mailserver: message request " request-id
                "completed for mailserver topics" topics "from" from "to" to)
      (if (empty? mailserver-topics)
        ;; when topics were deleted (filter was removed while request was pending)
        (fx/merge cofx
                  {:db (dissoc db :mailserver/current-request)}
                  (process-next-messages-request))
        ;; If a cursor is returned, add cursor and fire request again
        (if (seq cursor)
          (when-let [mailserver (get-mailserver-when-ready cofx)]
            (let [request-with-cursor (assoc request :cursor cursor)]
              {:db (assoc db :mailserver/current-request request-with-cursor)
               :mailserver/request-messages {:waku-enabled? (waku/enabled? cofx)
                                             :mailserver mailserver
                                             :request    request-with-cursor}}))
          (let [{:keys [gap chat-id]} request]
            (fx/merge
             cofx
             {:db (-> db
                      (dissoc :mailserver/current-request)
                      (update :mailserver/requests-from
                              #(apply dissoc % topics))
                      (update :mailserver/requests-to
                              #(apply dissoc % topics))
                      (update :mailserver/topics merge mailserver-topics)
                      (update :mailserver/fetching-gaps-in-progress
                              (fn [gaps]
                                (if gap
                                  (update gaps chat-id dissoc gap)
                                  gaps)))
                      (update :mailserver/planned-gap-requests
                              dissoc gap))
              ::json-rpc/call
              (mapv (fn [[topic mailserver-topic]]
                      {:method "mailservers_addMailserverTopic"
                       :params [(assoc mailserver-topic :topic topic)]
                       :on-success
                       #(log/debug "added mailserver-topic successfully")
                       :on-failure
                       #(log/error "failed to add mailserver topic" %)})
                    mailserver-topics)}
             (process-next-messages-request))))))))

(fx/defn retry-next-messages-request
  [{:keys [db] :as cofx}]
  (fx/merge cofx
            {:db (dissoc db :mailserver/request-error)}
            (process-next-messages-request)))

;; At some point we should update `last-request`, as eventually we want to move
;; on, rather then keep asking for the same data, say after n amounts of attempts
(fx/defn handle-request-error
  [{:keys [db]} error]
  {:mailserver/decrease-limit   []
   :db (-> db
           (assoc  :mailserver/request-error error)
           (dissoc :mailserver/current-request
                   :mailserver/pending-requests))})

(fx/defn handle-request-completed
  [{{:keys [chats]} :db :as cofx}
   {:keys [requestID lastEnvelopeHash cursor errorMessage]}]
  (when (multiaccounts.model/logged-in? cofx)
    (if (empty? errorMessage)
      (let [never-synced-chats-in-request
            (->> (chats->never-synced-public-chats chats)
                 (filter (fn [[k v]] (= requestID (:join-time-mail-request-id v))))
                 keys)]
        (if (seq never-synced-chats-in-request)
          (if (= lastEnvelopeHash
                 "0x0000000000000000000000000000000000000000000000000000000000000000")
            (fx/merge
             cofx
             {:mailserver/increase-limit []
              :dispatch-n
              (map
               #(identity [:chat.ui/join-time-messages-checked %])
               never-synced-chats-in-request)}
             (update-chats-and-gaps cursor)
             (update-mailserver-topics {:request-id requestID
                                        :cursor     cursor}))
            (fx/merge
             cofx
             {:mailserver/increase-limit []
              :dispatch-later
              (vec
               (map
                #(identity
                  {:ms       1000
                   :dispatch [:chat.ui/join-time-messages-checked %]})
                never-synced-chats-in-request))}
             (update-chats-and-gaps cursor)
             (update-mailserver-topics {:request-id requestID
                                        :cursor     cursor})))
          (fx/merge
           cofx
           {:mailserver/increase-limit []}
           (update-chats-and-gaps cursor)
           (update-mailserver-topics {:request-id requestID
                                      :cursor     cursor}))))
      (handle-request-error cofx errorMessage))))

(fx/defn show-request-error-popup
  [{:keys [db]}]
  (let [mailserver-error (:mailserver/request-error db)]
    {:utils/show-confirmation
     {:title (i18n/label :t/mailserver-request-error-title)
      :content (i18n/label :t/mailserver-request-error-content
                           {:error mailserver-error})
      :on-accept #(re-frame/dispatch [:mailserver.ui/retry-request-pressed])
      :confirm-button-text (i18n/label :t/mailserver-request-retry)}}))

(fx/defn fetch-history
  "Retrive a list of topics given a chat id, set them to the specified
  time interval and start a mailserver request"
  [{:keys [db] :as cofx} chat-id {:keys [from to]}]
  (let [topics  (mailserver.topics/topics-for-chat
                 db
                 chat-id)]
    (log/debug "fetch-history" "chat-id:" chat-id "from-timestamp:"
               from "topics:" topics)
    (fx/merge cofx
              {:db (reduce
                    (fn [db topic]
                      (cond-> (assoc-in db [:mailserver/requests-from topic] from)

                        to
                        (assoc-in [:mailserver/requests-to topic] to)))
                    db
                    topics)}
              (process-next-messages-request))))

(fx/defn fill-the-gap
  [{:keys [db] :as cofx} {:keys [gaps topics chat-id]}]
  (let [mailserver      (get-mailserver-when-ready cofx)
        requests        (into {}
                              (map
                               (fn [{:keys [from to id]}]
                                 [id
                                  {:from (max from
                                              (- to constants/max-request-range))
                                   :to        to
                                   :force-to? true
                                   :topics    topics
                                   :gap-topics topics
                                   :chat-id   chat-id
                                   :gap       id}]))
                              gaps)
        first-request   (val (first requests))
        current-request (:mailserver/current-request db)]
    (cond-> {:db (-> db
                     (assoc :mailserver/planned-gap-requests requests)
                     (update :mailserver/fetching-gaps-in-progress
                             assoc chat-id requests))}
      (not current-request)
      (-> (assoc-in [:db :mailserver/current-request] first-request)
          (assoc :mailserver/request-messages
                 {:waku-enabled? (waku/enabled? cofx)
                  :mailserver    mailserver
                  :request       first-request})))))

(fx/defn resend-request
  [{:keys [db] :as cofx} {:keys [request-id]}]
  (let [current-request (:mailserver/current-request db)
        gap-request? (executing-gap-request? db)]
    ;; no inflight request, do nothing
    (when (and current-request
               ;; the request was never successful
               (or (nil? request-id)
                   ;; we haven't received the request-id yet, but has expired,
                   ;; so we retry even though we are not sure it's the current
                   ;; request that failed
                   (nil? (:request-id current-request))
                   ;; this is the same request that we are currently processing
                   (= request-id (:request-id current-request))))

      (if            (<= constants/maximum-number-of-attempts
                         (:attempts current-request))
        (fx/merge cofx
                  {:db (update db :mailserver/current-request dissoc :attempts)}
                  (change-mailserver))
        (let [mailserver (get-mailserver-when-ready cofx)
              offline? (= :offline (:network-status db))]
          (cond
            (and gap-request? offline?)
            {:db (-> db
                     (dissoc :mailserver/current-request)
                     (update :mailserver/fetching-gaps-in-progress
                             dissoc (:chat-id current-request))
                     (dissoc :mailserver/planned-gap-requests))}

            mailserver
            (let [{:keys [topics from to cursor limit] :as request}
                  current-request]
              (log/info "mailserver: message request " request-id
                        "expired for mailserver topic" topics "from" from
                        "to" to "cursor" cursor "limit" (decrease-limit))
              {:db (update-in db [:mailserver/current-request :attempts] inc)
               :mailserver/decrease-limit   []
               :mailserver/request-messages
               {:waku-enabled? (waku/enabled? cofx)
                :mailserver    mailserver
                :request       (assoc request :limit (decrease-limit))}})

            :else
            {:mailserver/decrease-limit []}))))))

(fx/defn initialize-mailserver
  [cofx]
  (fx/merge cofx
            {:mailserver/set-limit constants/default-limit}
            (set-current-mailserver)))

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

(fx/defn set-input [{:keys [db]} input-key value]
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

(fx/defn edit [{:keys [db] :as cofx} id]
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
  [{{:mailserver.edit/keys [mailserver] :keys [multiaccount] :as db} :db
    random-id-generator :random-id-generator :as cofx}]
  (let [{:keys [name url id]} mailserver
        current-fleet (node/current-fleet-key db)
        mailserver (build
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
     :dispatch [:navigate-back]}))

(defn can-delete?
  [db id]
  (not (or (default? db id)
           (connected? db id))))

(fx/defn delete
  [{:keys [db] :as cofx} id]
  (if (can-delete? db id)
    {:db (update-in db
                    [:mailserver/mailservers (node/current-fleet-key db)]
                    dissoc id)
     ::json-rpc/call
     [{:method "mailservers_deleteMailserver"
       :params [(name id)]
       :on-success #(log/debug "deleted mailserver" id)
       :on-failure #(log/error "failed to delete mailserver" id %)}]
     :dispatch [:navigate-back]}
    {:dispatch [:navigate-back]}))

(fx/defn show-connection-confirmation
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
  [{:keys [db]} mailserver-id]
  {:ui/show-confirmation
   {:title               (i18n/label :t/delete-mailserver-title)
    :content             (i18n/label :t/delete-mailserver-are-you-sure)
    :confirm-button-text (i18n/label :t/delete-mailserver)
    :on-accept           #(re-frame/dispatch
                           [:mailserver.ui/delete-confirmed mailserver-id])}})

(fx/defn set-url-from-qr
  [cofx url]
  (assoc (set-input cofx :url url)
         :dispatch [:navigate-back]))

(fx/defn save-settings
  [{:keys [db] :as cofx} current-fleet mailserver-id]
  (let [{:keys [address]} (fetch-current db)
        pinned-mailservers (get-in db [:multiaccount :pinned-mailservers])
        ;; Check if previous mailserver was pinned
        pinned?  (get pinned-mailservers current-fleet)]
    (fx/merge cofx
              {:db (assoc db :mailserver/current-id mailserver-id)
               :mailserver/remove-peer address}
              (connect-to-mailserver)
              (when pinned?
                (multiaccounts.update/multiaccount-update
                 :pinned-mailservers (assoc pinned-mailservers
                                            current-fleet
                                            mailserver-id)
                 {})))))

(fx/defn unpin
  [{:keys [db] :as cofx}]
  (let [current-fleet (node/current-fleet-key db)
        pinned-mailservers (get-in db [:multiaccount :pinned-mailservers])]
    (fx/merge cofx
              (multiaccounts.update/multiaccount-update
               :pinned-mailservers (dissoc pinned-mailservers current-fleet)
               {})
              (change-mailserver))))

(fx/defn pin
  [{:keys [db] :as cofx}]
  (let [current-fleet (node/current-fleet-key db)
        mailserver-id (:mailserver/current-id db)
        pinned-mailservers (get-in db [:multiaccount :pinned-mailservers])]
    (multiaccounts.update/multiaccount-update
     cofx
     :pinned-mailservers (assoc pinned-mailservers
                                current-fleet
                                mailserver-id)
     {})))

(fx/defn load-gaps-fx [{:keys [db] :as cofx} chat-id]
  (when-not (get-in db [:chats chat-id :gaps-loaded?])
    (let [success-fn #(re-frame/dispatch [::gaps-loaded %1 %2])]
      (data-store.mailservers/load-gaps cofx chat-id success-fn))))

(fx/defn load-gaps
  {:events [::gaps-loaded]}
  [{:keys [db now] :as cofx} chat-id gaps]
  (let [now-s         (quot now 1000)
        outdated-gaps
        (into []
              (comp (filter #(< (:to %)
                                (- now-s constants/max-gaps-range)))
                    (map :id))
              (vals gaps))
        gaps          (apply dissoc gaps outdated-gaps)]
    (fx/merge
     cofx
     {:db
      (-> db
          (assoc-in [:chats chat-id :gaps-loaded?] true)
          (assoc-in [:mailserver/gaps chat-id] gaps))}

     (data-store.mailservers/delete-gaps outdated-gaps))))
