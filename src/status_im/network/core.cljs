(ns status-im.network.core
  (:require [clojure.string :as string]
            [re-frame.core :as re-frame]
            [status-im.accounts.update.core :as accounts.update]
            [status-im.i18n :as i18n]
            [status-im.native-module.core :as status]
            [status-im.network.net-info :as net-info]
            [status-im.mailserver.core :as mailserver]
            [status-im.utils.ethereum.core :as ethereum]
            [status-im.ui.screens.navigation :as navigation]
            [status-im.fleet.core :as fleet-core]
            [status-im.utils.fx :as fx]
            [status-im.utils.handlers :as handlers]
            [status-im.utils.http :as http]
            [status-im.utils.types :as types]))

(def url-regex
  #"https?://(www\.)?[-a-zA-Z0-9@:%._\+~#=]{2,256}(\.[a-z]{2,6})?\b([-a-zA-Z0-9@:%_\+.~#?&//=]*)")

(defn valid-rpc-url? [url]
  (boolean (re-matches url-regex (str url))))

(def default-manage
  {:name  {:value ""}
   :url   {:value ""}
   :chain {:value :mainnet}})

(defn validate-string [{:keys [value]}]
  {:value value
   :error (string/blank? value)})

(defn validate-network-id [{:keys [value]}]
  {:value value
   :error (and (not (string/blank? value))
               (= (int value) 0))})

(defn validate-url [{:keys [value]}]
  {:value value
   :error (not (valid-rpc-url? value))})

(defn validate-manage [manage]
  (-> manage
      (update :url validate-url)
      (update :name validate-string)
      (update :chain validate-string)
      (update :network-id validate-network-id)))

(defn valid-manage? [manage]
  (->> (validate-manage manage)
       vals
       (map :error)
       (not-any? identity)))

(defn new-network [random-id network-name upstream-url type network-id]
  (let [data-dir (str "/ethereum/" (name type) "_rpc")
        config   {:NetworkId      (or (when network-id (int network-id))
                                      (ethereum/chain-keyword->chain-id type))
                  :DataDir        data-dir
                  :UpstreamConfig {:Enabled true
                                   :URL     upstream-url}}]
    {:id         (string/replace random-id "-" "")
     :name       network-name
     :config     config}))

(defn get-chain [{:keys [db]}]
  (let [network  (get (:networks (:account/account db)) (:network db))]
    (ethereum/network->chain-keyword network)))

(fx/defn set-input
  [{:keys [db]} input-key value]
  {:db (-> db
           (update-in [:networks/manage input-key] assoc :value value)
           (update-in [:networks/manage] validate-manage))})

(defn- action-handler
  ([handler]
   (action-handler handler nil nil))
  ([handler data cofx]
   (when handler
     (handler data cofx))))

(fx/defn save
  [{{:network/keys [manage] :account/keys [account] :as db} :db
    random-id-generator :random-id-generator :as cofx}
   {:keys [data success-event on-success on-failure]}]
  (let [data (or data manage)]
    (if (valid-manage? data)
      (let [{:keys [name url chain network-id]} data
            network      (new-network (random-id-generator)
                                      (:value name)
                                      (:value url)
                                      (:value chain)
                                      (:value network-id))
            new-networks (merge {(:id network) network} (:networks account))]
        (fx/merge cofx
                  {:db (dissoc db :networks/manage)}
                  #(action-handler on-success (:id network) %)
                  (accounts.update/account-update
                   {:networks new-networks}
                   {:success-event success-event})))
      (action-handler on-failure))))

;; No edit functionality actually implemented
(fx/defn edit
  [{db :db}]
  {:db       (assoc db :networks/manage (validate-manage default-manage))
   :dispatch [:navigate-to :edit-network]})

(fx/defn connect-success [{:keys [db now] :as cofx} {:keys [network-id on-success client-version]}]
  (let [current-network (get-in db [:account/account :networks (:network db)])]
    (if (ethereum/network-with-upstream-rpc? current-network)
      (fx/merge cofx
                #(action-handler on-success {:network-id network-id :client-version client-version} %)
                (accounts.update/account-update
                 {:network      network-id
                  :last-updated now}
                 {:success-event [:accounts.update.callback/save-settings-success]}))
      (fx/merge cofx
                {:ui/show-confirmation {:title               (i18n/label :t/close-app-title)
                                        :content             (i18n/label :t/close-app-content)
                                        :confirm-button-text (i18n/label :t/close-app-button)
                                        :on-accept           #(re-frame/dispatch [:network.ui/save-non-rpc-network-pressed network-id])
                                        :on-cancel           nil}}
                #(action-handler on-success {:network-id network-id :client-version client-version} %)))))

(defn connect-failure [{:keys [network-id on-failure reason]}]
  (action-handler on-failure
                  {:network-id network-id :reason reason}
                  nil))

(fx/defn connect [{:keys [db] :as cofx} {:keys [network-id on-success on-failure]}]
  (if-let [config (get-in db [:account/account :networks network-id :config])]
    (if-let [upstream-url (get-in config [:UpstreamConfig :URL])]
      {:http-post {:url                   upstream-url
                   :data                  (types/clj->json {:jsonrpc "2.0"
                                                            :method  "web3_clientVersion"
                                                            :id      1})
                   :opts                  {:headers {"Content-Type" "application/json"}}
                   :success-event-creator (fn [{:keys [response-body]}]
                                            (if-let [client-version (:result (http/parse-payload response-body))]
                                              [::connect-success {:network-id     network-id
                                                                  :on-success     on-success
                                                                  :client-version client-version}]
                                              [::connect-failure {:network-id network-id
                                                                  :on-failure on-failure
                                                                  :reason     (i18n/label :t/network-invalid-url)}]))
                   :failure-event-creator (fn [{:keys [response-body status-code]}]
                                            (let [reason (if status-code
                                                           (i18n/label :t/network-invalid-status-code {:code status-code})
                                                           (str response-body))]
                                              [::connect-failure {:network-id network-id
                                                                  :on-failure on-failure
                                                                  :reason     reason}]))}}
      (connect-success cofx {:network-id     network-id
                             :on-success     on-success
                             :client-version ""}))
    (connect-failure {:network-id network-id
                      :on-failure on-failure
                      :reason     "A network with the specified id doesn't exist"})))

(handlers/register-handler-fx
 ::connect-success
 (fn [cofx [_ data]]
   (connect-success cofx data)))

(handlers/register-handler-fx
 ::connect-failure
 (fn [_ [_ data]]
   (connect-failure data)))

(fx/defn delete
  [{{:account/keys [account]} :db :as cofx} {:keys [network on-success on-failure]}]
  (let [current-network? (= (:network account) network)]
    (if (or current-network?
            (not (get-in account [:networks network])))
      (fx/merge cofx
                {:ui/show-error (i18n/label :t/delete-network-error)}
                #(action-handler on-failure network %))
      (fx/merge cofx
                {:ui/show-confirmation {:title               (i18n/label :t/delete-network-title)
                                        :content             (i18n/label :t/delete-network-confirmation)
                                        :confirm-button-text (i18n/label :t/delete)
                                        :on-accept           #(re-frame/dispatch [:network.ui/remove-network-confirmed network])
                                        :on-cancel           nil}}
                #(action-handler on-success network %)))))

(fx/defn save-non-rpc-network
  [{:keys [db now] :as cofx} network]
  (accounts.update/account-update cofx
                                  {:network      network
                                   :last-updated now}
                                  {:success-event [:network.callback/non-rpc-network-saved]}))

(fx/defn remove-network
  [{:keys [db now] :as cofx} network]
  (let [networks (dissoc (get-in db [:account/account :networks]) network)]
    (accounts.update/account-update cofx
                                    {:networks     networks
                                     :last-updated now}
                                    {:success-event [:navigate-back]})))

(fx/defn save-network
  [cofx]
  (save cofx
        {:data          (get-in cofx [:db :networks/manage])
         :success-event [:navigate-back]}))

(fx/defn handle-connection-status-change
  [{:keys [db] :as cofx} is-connected?]
  (fx/merge cofx
            {:db (assoc db :network-status (if is-connected? :online :offline))}
            (mailserver/network-connection-status-changed is-connected?)))

(defn- navigate-to-network-details
  [cofx network show-warning?]
  (fx/merge cofx
            (when show-warning?
              {:utils/show-popup {:title   "LES support is experimental!"
                                  :content "Use at your own risk!"}})
            (navigation/navigate-to-cofx :network-details {:networks/selected-network network})))

(defn- not-supported-warning [fleet]
  (str (name fleet) " does not support LES!\n"
       "Please, select one of the supported fleets:"
       (map name fleet-core/fleets-with-les)))

(fx/defn open-network-details
  [cofx network]
  (let [db                  (:db cofx)
        rpc-network?        (get-in network [:config :UpstreamConfig :Enabled] false)
        fleet               (fleet-core/current-fleet db nil)
        fleet-supports-les? (fleet-core/fleet-supports-les? fleet)]
    (if (or rpc-network? fleet-supports-les?)
      (navigate-to-network-details cofx network (not rpc-network?))
       ;; Otherwise, we show an explanation dialog to a user if the current fleet does not suport LES
      {:utils/show-popup {:title   "LES not supported"
                          :content (not-supported-warning fleet)}})))

(fx/defn handle-network-status-change
  [cofx data]
  {:network/notify-status-go data})

(re-frame/reg-fx
 :network/listen-to-network-status
 (fn []
   (let [callback-event #(re-frame/dispatch [:network/network-status-changed %])]
     (net-info/net-info callback-event)
     (net-info/add-net-info-listener callback-event))))

(re-frame/reg-fx
 :network/listen-to-connection-status
 (fn []
   (let [callback-event #(re-frame/dispatch [:network/connection-status-changed %])]
     (net-info/is-connected? callback-event)
     (net-info/add-connection-listener callback-event))))

(re-frame/reg-fx
 :network/notify-status-go
 (fn [data]
   (status/connection-change data)))
