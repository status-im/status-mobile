(ns status-im.network.core
  (:require [clojure.string :as string]
            [re-frame.core :as re-frame]
            [status-im.ethereum.core :as ethereum]
            [status-im.ethereum.json-rpc :as json-rpc]
            [status-im.fleet.core :as fleet-core]
            [status-im.i18n :as i18n]
            [status-im.node.core :as node]
            [status-im.ui.screens.navigation :as navigation]
            [status-im.utils.fx :as fx]
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

(defn chain-id-available? [current-networks network]
  (let [chain-id (get-in network [:config :NetworkId])]
    (every? #(not= chain-id (get-in % [1 :config :NetworkId])) current-networks)))

(defn get-network [{:keys [db]} network-id]
  (get-in db [:networks/networks network-id]))

(fx/defn set-input
  {:events [::input-changed]}
  [{:keys [db]} input-key value]
  {:db (-> db
           (update-in [:networks/manage input-key] assoc :value value)
           (update-in [:networks/manage] validate-manage))})

;; No edit functionality actually implemented
(fx/defn edit
  {:events [::add-network-pressed]}
  [{db :db}]
  {:db       (assoc db :networks/manage (validate-manage default-manage))
   :dispatch [:navigate-to :edit-network]})

(fx/defn connect-success
  {:events [::connect-success]}
  [_ network-id]
  {:ui/show-confirmation
   {:title               (i18n/label :t/close-app-title)
    :content             (i18n/label :t/logout-app-content)
    :confirm-button-text (i18n/label :t/close-app-button)
    :on-accept           #(re-frame/dispatch [::save-network-settings-pressed network-id])
    :on-cancel           nil}})

(fx/defn connect-failure
  {:events [::connect-failure]}
  [_ reason]
  {:utils/show-popup
   {:title   (i18n/label :t/error)
    :content (str reason)}})

(fx/defn connect
  {:events [::connect-network-pressed]}
  [{:keys [db] :as cofx} network-id]
  (if-let [config (get-in db [:networks/networks network-id :config])]
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
                                                [::connect-success network-id]
                                                [::connect-failure (if (not= expected-network-id rpc-network-id)
                                                                     (i18n/label :t/network-invalid-network-id)
                                                                     (i18n/label :t/network-invalid-url))])))
                   :failure-event-creator (fn [{:keys [response-body status-code]}]
                                            (let [reason (if status-code
                                                           (i18n/label :t/network-invalid-status-code {:code status-code})
                                                           (str response-body))]
                                              [::connect-failure reason]))}}
      (connect-success cofx network-id))
    (connect-failure cofx "A network with the specified id doesn't exist")))

(fx/defn delete
  {:events [::delete-network-pressed]}
  [{:keys [db]} network]
  (let [current-network? (= (:networks/current-network db) network)]
    (if (or current-network?
            (not (get-in db [:networks/networks network])))
      {:ui/show-error (i18n/label :t/delete-network-error)}
      {:ui/show-confirmation {:title               (i18n/label :t/delete-network-title)
                              :content             (i18n/label :t/delete-network-confirmation)
                              :confirm-button-text (i18n/label :t/delete)
                              :on-accept           #(re-frame/dispatch [::remove-network-confirmed network])
                              :on-cancel           nil}})))

(fx/defn save-network-settings
  {:events [::save-network-settings-pressed]}
  [{:keys [db] :as cofx} network]
  (fx/merge cofx
            {:db (assoc db :networks/current-network network)
             ::json-rpc/call [{:method "settings_saveConfig"
                               :params ["current-network" network]
                               :on-success #()}]}
            (node/prepare-new-config {:on-success #(re-frame/dispatch [:logout])})))

(fx/defn remove-network
  {:events [::remove-network-confirmed]}
  [{:keys [db] :as cofx} network]
  (let [networks (dissoc (:networks/networks db) network)]
    {:db (assoc db :networks/networks networks)
     ::json-rpc/call [{:method "settings_saveConfig"
                       :params ["networks" (types/serialize networks)]
                       :on-success #(re-frame/dispatch [:navigate-back])}]}))

(defn new-network
  [random-id network-name upstream-url chain-type chain-id]
  (let [data-dir (str "/ethereum/" (name chain-type) "_rpc")
        config   {:NetworkId      (or (when chain-id (int chain-id))
                                      (ethereum/chain-keyword->chain-id chain-type))
                  :DataDir        data-dir
                  :UpstreamConfig {:Enabled true
                                   :URL     upstream-url}}]
    {:id         random-id
     :name       network-name
     :config     config}))

(fx/defn save
  {:events [::save-network-pressed]
   :interceptors [(re-frame/inject-cofx :random-id-generator)]}
  [{{:networks/keys [manage networks] :as db} :db
    random-id-generator :random-id-generator :as cofx}]
  (if (valid-manage? manage)
    ;; rename network-id from UI to chain-id
    (let [{:keys [name url chain network-id]} manage
          random-id (string/replace (random-id-generator) "-" "")
          network (new-network random-id
                               (:value name)
                               (:value url)
                               (:value chain)
                               (:value network-id))
          custom-chain-type? (= :custom (:value chain))
          new-networks (assoc networks random-id network)]
      (if (or (not custom-chain-type?)
              (chain-id-available? networks network))
        {:db (-> db
                 (dissoc :networks/manage)
                 (assoc :networks/networks new-networks))
         ::json-rpc/call [{:method "settings_saveConfig"
                           :params ["networks" (types/serialize new-networks)]
                           :on-success #(re-frame/dispatch [:navigate-back])}]}
        {:ui/show-error "chain-id already defined"}))
    {:ui/show-error "invalid network parameters"}))

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
  {:events [::network-entry-pressed]}
  [cofx network]
  (let [db                  (:db cofx)
        rpc-network?        (get-in network [:config :UpstreamConfig :Enabled] false)
        fleet               (fleet-core/current-fleet db)
        fleet-supports-les? (fleet-core/fleet-supports-les? fleet)]
    (if (or rpc-network? fleet-supports-les?)
      (navigate-to-network-details cofx network (not rpc-network?))
      ;; Otherwise, we show an explanation dialog to a user if the current fleet does not suport LES
      {:utils/show-popup {:title   "LES not supported"
                          :content (not-supported-warning fleet)}})))
