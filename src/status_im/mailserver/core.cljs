(ns ^{:doc "Mailserver events and API"}
 status-im.mailserver.core
  (:require [re-frame.core :as re-frame]
            [status-im.accounts.db :as accounts.db]
            [status-im.data-store.core :as data-store]
            [status-im.fleet.core :as fleet]
            [status-im.native-module.core :as status]
            [status-im.transport.utils :as transport.utils]
            [status-im.utils.fx :as fx]
            [status-im.constants :as constants]
            [status-im.utils.utils :as utils]
            [taoensso.timbre :as log]
            [status-im.transport.db :as transport.db]
            [clojure.string :as string]
            [status-im.data-store.mailservers :as data-store.mailservers]
            [status-im.i18n :as i18n]
            [status-im.utils.handlers :as handlers]
            [status-im.accounts.update.core :as accounts.update]
            [status-im.ui.screens.navigation :as navigation]))

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


(def one-day (* 24 3600))
(def seven-days (* 7 one-day))
(def maximum-number-of-attempts 2)
(def request-timeout 30)
(def connection-timeout
  "Time after which mailserver connection is considered to have failed"
  10000)

(defn connected? [{:keys [db]} id]
  (= (:mailserver/current-id db) id))

(defn fetch [{:keys [db] :as cofx} id]
  (get-in db [:mailserver/mailservers (fleet/current-fleet db) id]))

(defn fetch-current [{:keys [db] :as cofx}]
  (fetch cofx (:mailserver/current-id db)))

(defn preferred-mailserver-id [{:keys [db] :as cofx}]
  (get-in db [:account/account :settings :mailserver (fleet/current-fleet db)]))

(defn- round-robin
  "Find the choice and pick the next one, default to first if not found"
  [choices current-id]
  (let [next-index (reduce
                    (fn [index choice]
                      (if (= current-id choice)
                        (reduced (inc index))
                        (inc index)))
                    0
                    choices)]
    (nth choices
         (mod
          next-index
          (count choices)))))

(defn selected-or-random-id
  "Use the preferred mailserver if set & exists, otherwise picks one randomly
  if current-id is not set, else round-robin"
  [{:keys [db] :as cofx}]
  (let [current-fleet (fleet/current-fleet db)
        current-id    (:mailserver/current-id db)
        preference    (preferred-mailserver-id cofx)
        choices       (-> db :mailserver/mailservers current-fleet keys)]
    (if (and preference
             (fetch cofx preference))
      preference
      (if current-id
        (round-robin choices current-id)
        (rand-nth choices)))))

(fx/defn set-current-mailserver
  [{:keys [db] :as cofx}]
  {:db (assoc db :mailserver/current-id
              (selected-or-random-id cofx))})

(fx/defn add-custom-mailservers
  [{:keys [db]} mailservers]
  {:db (reduce (fn [db {:keys [id fleet] :as mailserver}]
                 (assoc-in db [:mailserver/mailservers fleet id]
                           (-> mailserver
                               (dissoc :fleet)
                               (assoc :user-defined true))))
               db
               mailservers)})

(defn add-peer! [enode]
  (status/add-peer enode
                   (handlers/response-handler #(log/debug "mailserver: add-peer success" %)
                                              #(log/error "mailserver: add-peer error" %))))

;; We now wait for a confirmation from the mailserver before marking the message
;; as sent.

(defn update-mailservers! [enodes]
  (status/update-mailservers
   (.stringify js/JSON (clj->js enodes))
   (handlers/response-handler #(log/debug "mailserver: update-mailservers success" %)
                              #(log/error "mailserver: update-mailservers error" %))))

(defn remove-peer! [enode]
  (let [args    {:jsonrpc "2.0"
                 :id      2
                 :method  "admin_removePeer"
                 :params  [enode]}
        payload (.stringify js/JSON (clj->js args))]
    (status/call-private-rpc payload
                             (handlers/response-handler #(log/debug "mailserver: remove-peer success" %)
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
 (fn [enodes]
   (update-mailservers! enodes)))

(defn mark-trusted-peer! [web3 enode]
  (.markTrustedPeer (transport.utils/shh web3)
                    enode
                    (fn [error response]
                      (if error
                        (re-frame/dispatch [:mailserver.callback/mark-trusted-peer-error error])
                        (re-frame/dispatch [:mailserver.callback/mark-trusted-peer-success response])))))

(re-frame/reg-fx
 :mailserver/mark-trusted-peer
 (fn [{:keys [address web3]}]
   (mark-trusted-peer! web3 address)))

(fx/defn generate-mailserver-symkey
  [{:keys [db] :as cofx} {:keys [password id] :as mailserver}]
  (let [current-fleet (fleet/current-fleet db)]
    {:db (assoc-in db [:mailserver/mailservers current-fleet id :generating-sym-key?] true)
     :shh/generate-sym-key-from-password
     [{:password    password
       :web3       (:web3 db)
       :on-success (fn [_ sym-key-id]
                     (re-frame/dispatch [:mailserver.callback/generate-mailserver-symkey-success mailserver sym-key-id]))
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
  (let [{:keys [address sym-key-id generating-sym-key?] :as mailserver} (fetch-current cofx)]
    (fx/merge cofx
              {:db (update-mailserver-state db :added)
               :mailserver/mark-trusted-peer {:web3  (:web3 db)
                                              :address address}}
              (when-not (or sym-key-id generating-sym-key?)
                (generate-mailserver-symkey mailserver)))))

(fx/defn add-peer
  [{:keys [db] :as cofx}]
  (let [{:keys [address sym-key-id generating-sym-key?] :as mailserver} (fetch-current cofx)]
    (fx/merge cofx
              {:db (-> db
                       (update-mailserver-state :connecting)
                       (update :mailserver/connection-checks inc))
               :mailserver/add-peer address
               ;; Any message sent before this takes effect will not be marked as sent
               ;; probably we should improve the UX so that is more transparent to the user
               :mailserver/update-mailservers [address]
               :utils/dispatch-later [{:ms connection-timeout
                                       :dispatch [:mailserver/check-connection-timeout]}]}
              (when-not (or sym-key-id generating-sym-key?)
                (generate-mailserver-symkey mailserver)))))

(fx/defn connect-to-mailserver
  "Add mailserver as a peer using `::add-peer` cofx and generate sym-key when
   it doesn't exists
   Peer summary will change and we will receive a signal from status go when
   this is successful
   A connection-check is made after `connection timeout` is reached and
   mailserver-state is changed to error if it is not connected by then"
  [{:keys [db] :as cofx}]
  (let [{:keys [address] :as mailserver} (fetch-current cofx)
        {:keys [peers-summary]} db
        added? (registered-peer? peers-summary
                                 address)]
    (fx/merge cofx
              {:db (dissoc db :mailserver/current-request)}
              (if added?
                (mark-trusted-peer)
                (add-peer)))))

(fx/defn peers-summary-change
  "There is only 2 summary changes that require mailserver action:
  - mailserver disconnected: we try to reconnect
  - mailserver connected: we mark the mailserver as trusted peer"
  [{:keys [db] :as cofx} previous-summary]
  (when (:account/account db)
    (let [{:keys [peers-summary peers-count]} db
          {:keys [address sym-key-id] :as mailserver} (fetch-current cofx)
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
        (connect-to-mailserver cofx)))))

(defn request-messages! [web3 {:keys [sym-key-id address]} {:keys [topics to from]}]
  (log/info "mailserver: request-messages for: "
            " topics " topics
            " from " from
            " to   " to)
  (.requestMessages (transport.utils/shh web3)
                    (clj->js {:topics         topics
                              :mailServerPeer address
                              :symKeyID       sym-key-id
                              :timeout        request-timeout
                              :from           from
                              :to             to})
                    (fn [error request-id]
                      (if-not error
                        (log/info "mailserver: messages request success for topic " topics "from" from "to" to)
                        (log/error "mailserver: messages request error for topic " topics ": " error)))))

(re-frame/reg-fx
 :mailserver/request-messages
 (fn [{:keys [web3 mailserver request]}]
   (request-messages! web3 mailserver request)))

(defn get-mailserver-when-ready
  "return the mailserver if the mailserver is ready"
  [{:keys [db] :as cofx}]
  (let [{:keys [sym-key-id] :as mailserver} (fetch-current cofx)
        mailserver-state (:mailserver/state db)]
    (when (and (= :connected mailserver-state)
               sym-key-id)
      mailserver)))

(defn split-request-per-day
  "NOTE: currently the mailserver is only accepting requests for a span
  of 24 hours, so we split requests per 24h spans if the last request was
  done more than 24h ago"
  [now-in-s [last-request topics]]
  (let [days        (conj
                     (into [] (range (max last-request
                                          (- now-in-s one-day))
                                     now-in-s
                                     one-day))
                     now-in-s)
        day-ranges  (map vector days (rest days))]
    (for [[from to] day-ranges]
      {:topics topics
       :from  from
       :to    to})))

(defn prepare-messages-requests
  [{:keys [db now] :as cofx} request-to]
  (let [web3     (:web3 db)]
    (remove nil?
            (mapcat (partial split-request-per-day request-to)
                    (reduce (fn [acc [topic {:keys [last-request]}]]
                              (update acc last-request conj topic))
                            {}
                            (:mailserver/topics db))))))

(fx/defn process-next-messages-request
  [{:keys [db now] :as cofx}]
  (when (and (transport.db/all-filters-added? cofx)
             (not (:mailserver/current-request db)))
    (when-let [mailserver (get-mailserver-when-ready cofx)]
      (let [request-to (or (:mailserver/request-to db)
                           (quot now 1000))
            requests (prepare-messages-requests cofx request-to)
            web3 (:web3 db)]
        (if-let [request (first requests)]
          {:db (assoc db
                      :mailserver/pending-requests (count requests)
                      :mailserver/current-request request
                      :mailserver/request-to request-to)
           :mailserver/request-messages {:web3     web3
                                         :mailserver    mailserver
                                         :request request}}
          {:db (dissoc db
                       :mailserver/pending-requests
                       :mailserver/request-to)})))))

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
  (let [current-fleet (fleet/current-fleet db)]
    (fx/merge cofx
              {:db (-> db
                       (assoc-in [:mailserver/mailservers current-fleet id :sym-key-id] sym-key-id)
                       (update-in [:mailserver/mailservers current-fleet id] dissoc :generating-sym-key?))}
              (process-next-messages-request))))

(fx/defn change-mailserver
  "mark mailserver status as `:error` if custom mailserver is used
  otherwise try to reconnect to another mailserver"
  [{:keys [db] :as cofx}]
  (if (preferred-mailserver-id cofx)
    {:db (update-mailserver-state db :error)}
    (let [{:keys [address]} (fetch-current cofx)]
      (fx/merge cofx
                {:mailserver/remove-peer address}
                (set-current-mailserver)
                (connect-to-mailserver)))))

(fx/defn check-connection
  "connection-checks counter is used to prevent changing
   mailserver on flaky connections
   if there is more than one connection check pending
      decrement the connection check counter
   else
      change mailserver if mailserver is connected"
  [{:keys [db] :as cofx}]
  ;; check if logged into account
  (when (contains? db :account/account)
    (let [connection-checks (dec (:mailserver/connection-checks db))]
      (if (>= 0 connection-checks)
        (fx/merge cofx
                  {:db (dissoc db :mailserver/connection-checks)}
                  (when (= :connecting (:mailserver/state db))
                    (change-mailserver cofx)))
        {:db (update db :mailserver/connection-checks dec)}))))

(fx/defn reset-request-to
  [{:keys [db]}]
  {:db (dissoc db :mailserver/request-to)})

(fx/defn network-connection-status-changed
  "when host reconnects, reset request-to and
  reconnect to mailserver"
  [{:keys [db] :as cofx} is-connected?]
  (when (and (accounts.db/logged-in? cofx)
             is-connected?)
    (fx/merge cofx
              (reset-request-to)
              (connect-to-mailserver))))

(fx/defn remove-chat-from-mailserver-topic
  "if the chat is the only chat of the mailserver topic delete the mailserver topic
   and process-next-messages-requests again to remove pending request for that topic
   otherwise remove the chat-id of the chat from the mailserver topic and save"
  [{:keys [db now] :as cofx} chat-id]
  (let [topic (get-in db [:transport/chats chat-id :topic])
        {:keys [chat-ids] :as mailserver-topic} (update (get-in db [:mailserver/topics topic])
                                                        :chat-ids
                                                        disj chat-id)]
    (if (empty? chat-ids)
      (fx/merge cofx
                {:db (update db :mailserver/topics dissoc topic)
                 :data-store/tx [(data-store.mailservers/delete-mailserver-topic-tx topic)]}
                (process-next-messages-request))
      {:db (assoc-in db [:mailserver/topics topic] mailserver-topic)
       :data-store/tx [(data-store.mailservers/save-mailserver-topic-tx
                        {:topic topic
                         :mailserver-topic mailserver-topic})]})))

(defn get-updated-mailserver-topics [db topics last-request]
  (reduce (fn [acc topic]
            (if-let [mailserver-topic (some-> (get-in db [:mailserver/topics topic])
                                              (assoc :last-request last-request))]
              (assoc acc topic mailserver-topic)
              acc))
          {}
          topics))

(fx/defn update-mailserver-topics
  "TODO: add support for cursors
  if there is a cursor, do not update `last-request`"
  [{:keys [db now] :as cofx} {:keys [request-id]}]
  (when-let [request (get db :mailserver/current-request)]
    (let [{:keys [from to topics]} request
          mailserver-topics (get-updated-mailserver-topics db topics to)]
      (log/info "mailserver: message request " request-id
                "completed for mailserver topics" topics "from" from "to" to)
      (if (empty? mailserver-topics)
        ;; when topics were deleted (filter was removed while request was pending)
        (fx/merge cofx
                  {:db (dissoc db :mailserver/current-request)}
                  (process-next-messages-request))
        (fx/merge cofx
                  {:db (-> db
                           (dissoc :mailserver/current-request)
                           (update :mailserver/topics merge mailserver-topics))
                   :data-store/tx (mapv (fn [[topic mailserver-topic]]
                                          (data-store.mailservers/save-mailserver-topic-tx
                                           {:topic topic
                                            :mailserver-topic mailserver-topic}))
                                        mailserver-topics)}
                  (process-next-messages-request))))))

(fx/defn retry-next-messages-request
  [{:keys [db] :as cofx}]
  (fx/merge cofx
            {:db (dissoc db :mailserver/request-error)}
            (process-next-messages-request)))

(fx/defn handle-request-error
  [{:keys [db]} error]
  {:db (-> db
           (assoc :mailserver/request-error error)
           (dissoc :mailserver/current-request
                   :mailserver/pending-requests))})

(fx/defn handle-request-completed
  [cofx event]
  (when (accounts.db/logged-in? cofx)
    (let [error (:errorMessage event)]
      (if (empty? error)
        (update-mailserver-topics cofx
                                  {:request-id (:requestID event)
                                   :cursor     (:cursor event)})
        (handle-request-error cofx error)))))

(fx/defn show-request-error-popup
  [{:keys [db]}]
  (let [mailserver-error (:mailserver/request-error db)]
    {:utils/show-confirmation
     {:title (i18n/label :t/mailserver-request-error-title)
      :content (i18n/label :t/mailserver-request-error-content {:error mailserver-error})
      :on-accept #(re-frame/dispatch [:mailserver.ui/retry-request-pressed])
      :confirm-button-text (i18n/label :t/mailserver-request-retry)}}))

(fx/defn upsert-mailserver-topic
  "if the topic didn't exist
      create the topic
   else if chat-id is not in the topic
      add the chat-id to the topic and reset last-request
      there was no filter for the chat and messages for that
      so the whole history for that topic needs to be re-fetched"
  [{:keys [db] :as cofx} {:keys [topic chat-id]}]
  (let [{:keys [chat-ids last-request] :as current-mailserver-topic}
        (get-in db [:mailserver/topics topic] {:chat-ids #{}})]
    (when-let [mailserver-topic (when-not (chat-ids chat-id)
                                  (-> current-mailserver-topic
                                      (assoc :last-request 1)
                                      (update :chat-ids conj chat-id)))]
      (fx/merge cofx
                {:db (assoc-in db [:mailserver/topics topic] mailserver-topic)
                 :data-store/tx [(data-store.mailservers/save-mailserver-topic-tx
                                  {:topic topic
                                   :mailserver-topic mailserver-topic})]}))))
(fx/defn fetch-history
  [{:keys [db] :as cofx} chat-id]
  (let [topic  (or (get-in db [:transport/chats chat-id :topic])
                   (transport.utils/get-topic constants/contact-discovery))
        {:keys [chat-ids last-request] :as current-mailserver-topic}
        (get-in db [:mailserver/topics topic] {:chat-ids #{}})]
    (let [mailserver-topic (-> current-mailserver-topic
                               (assoc :last-request 1))]
      (fx/merge cofx
                {:db (assoc-in db [:mailserver/topics topic] mailserver-topic)
                 :data-store/tx [(data-store.mailservers/save-mailserver-topic-tx
                                  {:topic topic
                                   :mailserver-topic mailserver-topic})]}
                (process-next-messages-request)))))

(fx/defn resend-request
  [{:keys [db] :as cofx} {:keys [request-id]}]
  (if (<= maximum-number-of-attempts
          (get-in db [:mailserver/current-request :attemps]))
    (fx/merge cofx
              {:db (update db :mailserver/current-request dissoc :attemps)}
              (change-mailserver))
    (when-let [mailserver (get-mailserver-when-ready cofx)]
      (let [{:keys [topics from to] :as request} (get db :mailserver/current-request)
            web3 (:web3 db)]
        (log/info "mailserver: message request " request-id "expired for mailserver topic" topics "from" from "to" to)
        {:db (update-in db [:mailserver/current-request :attemps] inc)
         :mailserver/request-messages {:web3    web3
                                       :mailserver   mailserver
                                       :request request}}))))

(fx/defn initialize-mailserver
  [cofx custom-mailservers]
  (fx/merge cofx
            (add-custom-mailservers custom-mailservers)
            (set-current-mailserver)))

(def enode-address-regex #"enode://[a-zA-Z0-9]+\@\b\d{1,3}\.\d{1,3}\.\d{1,3}\.\d{1,3}\b:(\d{1,5})")
(def enode-url-regex #"enode://[a-zA-Z0-9]+:(.+)\@\b\d{1,3}\.\d{1,3}\.\d{1,3}\.\d{1,3}\b:(\d{1,5})")

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
  (let [{:keys [id
                address
                password
                name]}   (fetch cofx id)
        url              (when address (build-url address password))]
    (fx/merge cofx
              (set-input :id id)
              (set-input :url (str url))
              (set-input :name (str name))
              (navigation/navigate-to-cofx :edit-mailserver nil))))

(fx/defn upsert
  [{{:mailserver.edit/keys [mailserver] :account/keys [account] :as db} :db
    random-id-generator :random-id-generator :as cofx}]
  (let [{:keys [name url id]} mailserver
        current-fleet         (fleet/current-fleet db)
        mailserver            (build
                               (or (:value id)
                                   (keyword (string/replace (random-id-generator) "-" "")))
                               (:value name)
                               (:value url))
        current               (connected? cofx (:id mailserver))]
    {:db (-> db
             (dissoc :mailserver.edit/mailserver)
             (assoc-in [:mailserver/mailservers current-fleet (:id mailserver)] mailserver))
     :data-store/tx [{:transaction
                      (data-store.mailservers/save-tx (assoc
                                                       mailserver
                                                       :fleet
                                                       current-fleet))
                      ;; we naively logout if the user is connected to the edited mailserver
                      :success-event (when current [:accounts.logout.ui/logout-confirmed])}]
     :dispatch [:navigate-back]}))

(fx/defn delete
  [{:keys [db] :as cofx} id]
  (merge (when-not (or
                    (default? cofx id)
                    (connected? cofx id))
           {:db            (update-in db [:mailserver/mailservers (fleet/current-fleet db)] dissoc id)
            :data-store/tx [(data-store.mailservers/delete-tx id)]})
         {:dispatch [:navigate-back]}))

(fx/defn show-connection-confirmation
  [{:keys [db]} mailserver-id]
  (let [current-fleet (fleet/current-fleet db)]
    {:ui/show-confirmation
     {:title               (i18n/label :t/close-app-title)
      :content             (i18n/label :t/connect-mailserver-content
                                       {:name (get-in db [:mailserver/mailservers  current-fleet mailserver-id :name])})
      :confirm-button-text (i18n/label :t/close-app-button)
      :on-accept           #(re-frame/dispatch [:mailserver.ui/connect-confirmed current-fleet mailserver-id])
      :on-cancel           nil}}))

(fx/defn show-delete-confirmation
  [{:keys [db]} mailserver-id]
  {:ui/show-confirmation
   {:title               (i18n/label :t/delete-mailserver-title)
    :content             (i18n/label :t/delete-mailserver-are-you-sure)
    :confirm-button-text (i18n/label :t/delete-mailserver)
    :on-accept           #(re-frame/dispatch [:mailserver.ui/delete-confirmed mailserver-id])}})

(fx/defn set-url-from-qr
  [cofx url]
  (assoc (set-input cofx :url url)
         :dispatch [:navigate-back]))

(fx/defn save-settings
  [{:keys [db] :as cofx} current-fleet mailserver-id]
  (let [{:keys [address]} (fetch-current cofx)
        settings (get-in db [:account/account :settings])]
    (fx/merge cofx
              {:db (assoc db :mailserver/current-id mailserver-id)
               :mailserver/remove-peer address}
              (connect-to-mailserver)
              (accounts.update/update-settings (assoc-in settings [:mailserver current-fleet] mailserver-id)
                                               {}))))
