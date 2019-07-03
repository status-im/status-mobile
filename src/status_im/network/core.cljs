(ns status-im.network.core
  (:require [clojure.string :as string]
            [re-frame.core :as re-frame]
            [status-im.multiaccounts.update.core :as multiaccounts.update]
            [status-im.chaos-mode.core :as chaos-mode]
            [status-im.ethereum.core :as ethereum]
            [status-im.fleet.core :as fleet-core]
            [status-im.i18n :as i18n]
            [status-im.mailserver.core :as mailserver]
            [status-im.native-module.core :as status]
            [status-im.ui.screens.mobile-network-settings.events :as mobile-network]
            [status-im.ui.screens.navigation :as navigation]
            [status-im.utils.fx :as fx]
            [status-im.utils.handlers :as handlers]
            [status-im.utils.http :as http]
            [status-im.utils.types :as types]
            status-im.network.subs))

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

(defn get-network-id-for-chain-id [{:keys [db]} chain-id]
  (let [networks (get-in db [:multiaccount :networks])
        filtered (filter #(= chain-id (get-in % [1 :config :NetworkId])) networks)]
    (first (keys filtered))))

(defn chain-id-available? [current-networks network]
  (let [chain-id (get-in network [:config :NetworkId])]
    (every? #(not= chain-id (get-in % [1 :config :NetworkId])) current-networks)))

(defn new-network [random-id network-name upstream-url type chain-id]
  (let [data-dir (str "/ethereum/" (name type) "_rpc")
        config   {:NetworkId      (or (when chain-id (int chain-id))
                                      (ethereum/chain-keyword->chain-id type))
                  :DataDir        data-dir
                  :UpstreamConfig {:Enabled true
                                   :URL     upstream-url}}]
    {:id         (string/replace random-id "-" "")
     :name       network-name
     :config     config}))

(defn get-network [{:keys [db]} network-id]
  (get-in db [:multiaccount :networks network-id]))

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
  [{{:networks/keys [manage] :keys [multiaccount] :as db} :db
    random-id-generator :random-id-generator :as cofx}
   {:keys [data success-event on-success on-failure network-id chain-id-unique?]}]
  (let [data (or data manage)]
    (if (valid-manage? data)
      ;; rename network-id from UI to chain-id
      (let [{:keys [name url chain] chain-id :network-id} data
            ;; network-id overrides random id
            network      (new-network (or network-id (random-id-generator))
                                      (:value name)
                                      (:value url)
                                      (:value chain)
                                      (:value chain-id))
            current-networks (:networks multiaccount)
            new-networks (merge {(:id network) network} current-networks)]
        (if (or (not chain-id-unique?)
                (chain-id-available? current-networks network))
          (fx/merge cofx
                    {:db (dissoc db :networks/manage)}
                    #(action-handler on-success (:id network) %)
                    (multiaccounts.update/multiaccount-update
                     {:networks new-networks}
                     {:success-event success-event}))
          (action-handler on-failure "chain-id already defined" nil)))
      (action-handler on-failure "invalid network parameters" nil))))

;; No edit functionality actually implemented
(fx/defn edit
  [{db :db}]
  {:db       (assoc db :networks/manage (validate-manage default-manage))
   :dispatch [:navigate-to :edit-network]})

(fx/defn connect-success [{:keys [db] :as cofx}
                          {:keys [network-id on-success client-version]}]
  (let [current-network (get-in db [:multiaccount :networks (:network db)])
        network-with-upstream-rpc? (ethereum/network-with-upstream-rpc?
                                    current-network)]
    (fx/merge
     cofx
     {:ui/show-confirmation
      {:title               (i18n/label :t/close-app-title)
       :content             (if network-with-upstream-rpc?
                              (i18n/label :t/logout-app-content)
                              (i18n/label :t/close-app-content))
       :confirm-button-text (i18n/label :t/close-app-button)
       :on-accept           #(re-frame/dispatch
                              [(if network-with-upstream-rpc?
                                 :network.ui/save-rpc-network-pressed
                                 :network.ui/save-non-rpc-network-pressed)
                               network-id])
       :on-cancel           nil}}
     #(action-handler on-success {:network-id network-id
                                  :client-version client-version} %))))

(defn connect-failure [{:keys [network-id on-failure reason]}]
  (action-handler on-failure
                  {:network-id network-id :reason reason}
                  nil))

(fx/defn connect [{:keys [db] :as cofx} {:keys [network-id on-success on-failure]}]
  (if-let [config (get-in db [:multiaccount :networks network-id :config])]
    (if-let [upstream-url (get-in config [:UpstreamConfig :URL])]
      {:http-post {:url                   upstream-url
                   :data                  (types/clj->json [{:jsonrpc "2.0"
                                                             :method  "web3_clientVersion"
                                                             :id      1}
                                                            {:jsonrpc "2.0"
                                                             :method  "net_version"
                                                             :id      2}])
                   :opts                  {:headers {"Content-Type" "application/json"}}
                   :success-event-creator (fn [{:keys [response-body]}]
                                            (let [responses           (http/parse-payload response-body)
                                                  client-version      (:result (first responses))
                                                  expected-network-id (:NetworkId config)
                                                  rpc-network-id      (when-let [res (:result (second responses))]
                                                                        (js/parseInt res))]
                                              (if (and client-version network-id
                                                       (= expected-network-id rpc-network-id))
                                                [::connect-success {:network-id     network-id
                                                                    :on-success     on-success
                                                                    :client-version client-version}]
                                                [::connect-failure {:network-id network-id
                                                                    :on-failure on-failure
                                                                    :reason     (if (not= expected-network-id rpc-network-id)
                                                                                  (i18n/label :t/network-invalid-network-id)
                                                                                  (i18n/label :t/network-invalid-url))}])))
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
  [{{:keys [multiaccount]} :db :as cofx} {:keys [network on-success on-failure]}]
  (let [current-network? (= (:network multiaccount) network)]
    (if (or current-network?
            (not (get-in multiaccount [:networks network])))
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
  (multiaccounts.update/multiaccount-update cofx
                                            {:network      network
                                             :last-updated now}
                                            {:success-event [:network.callback/non-rpc-network-saved]}))

(fx/defn save-rpc-network
  [{:keys [now] :as cofx} network]
  (multiaccounts.update/multiaccount-update
   cofx
   {:network      network
    :last-updated now}
   {:success-event [:multiaccounts.update.callback/save-settings-success]}))

(fx/defn remove-network
  [{:keys [db now] :as cofx} network success-event]
  (let [networks (dissoc (get-in db [:multiaccount :networks]) network)]
    (multiaccounts.update/multiaccount-update cofx
                                              {:networks     networks
                                               :last-updated now}
                                              {:success-event success-event})))

(fx/defn save-network
  [cofx]
  (save cofx
        {:data          (get-in cofx [:db :networks/manage])
         :success-event [:navigate-back]}))

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
