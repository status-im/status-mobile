(ns legacy.status-im.network.core
  (:require
    [clojure.string :as string]
    [legacy.status-im.node.core :as node]
    [re-frame.core :as re-frame]
    [status-im2.navigation.events :as navigation]
    [utils.ethereum.chain :as chain]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(def url-regex
  #"https?://(www\.)?[-a-zA-Z0-9@:%._\+~#=]{2,256}(\.[a-z]{2,6})?\b([-a-zA-Z0-9@:%_\+.~#?&//=]*)")

(defn valid-rpc-url?
  [url]
  (boolean (re-matches url-regex (str url))))

(def default-manage
  {:name   {:value ""}
   :url    {:value ""}
   :symbol {:value ""}
   :chain  {:value :mainnet}})

(defn validate-string
  [{:keys [value]}]
  {:value value
   :error (string/blank? value)})

(defn validate-network-id
  [{:keys [value]}]
  {:value value
   :error (and (not (string/blank? value))
               (= (int value) 0))})

(defn validate-url
  [{:keys [value]}]
  {:value value
   :error (not (valid-rpc-url? value))})

(defn validate-manage
  [manage]
  (-> manage
      (update :url validate-url)
      (update :name validate-string)
      (update :symbol validate-string)
      (update :chain validate-string)
      (update :network-id validate-network-id)))

(defn valid-manage?
  [manage]
  (->> (validate-manage manage)
       vals
       (map :error)
       (not-any? identity)))

(defn chain-id-available?
  [current-networks network]
  (let [chain-id (get-in network [:config :NetworkId])]
    (every? #(not= chain-id (get-in % [1 :config :NetworkId])) current-networks)))

(defn get-network
  [{:keys [db]} network-id]
  (get-in db [:networks/networks network-id]))

(rf/defn set-input
  {:events [::input-changed]}
  [{:keys [db]} input-key value]
  {:db (-> db
           (update-in [:networks/manage input-key] assoc :value value)
           (update-in [:networks/manage] validate-manage))})

;; No edit functionality actually implemented
(rf/defn edit
  {:events [::add-network-pressed]}
  [{db :db}]
  {:db       (assoc db :networks/manage (validate-manage default-manage))
   :dispatch [:navigate-to :edit-network]})

(rf/defn connect-success
  {:events [::connect-success]}
  [_ network-id]
  {:ui/show-confirmation
   {:title               (i18n/label :t/close-app-title)
    :content             (i18n/label :t/logout-app-content)
    :confirm-button-text (i18n/label :t/close-app-button)
    :on-accept           #(re-frame/dispatch [::save-network-settings-pressed network-id])
    :on-cancel           nil}})

(rf/defn connect-failure
  {:events [::connect-failure]}
  [_ reason]
  {:effects.utils/show-popup
   {:title   (i18n/label :t/error)
    :content (str reason)}})

(rf/defn connect
  {:events [::connect-network-pressed]}
  [{:keys [db] :as cofx} network-id]
  (if (get-in db [:networks/networks network-id :config])
    (connect-success cofx network-id)
    (connect-failure cofx "A network with the specified id doesn't exist")))

(rf/defn delete
  {:events [::delete-network-pressed]}
  [{:keys [db]} network]
  (let [current-network? (= (:networks/current-network db) network)]
    (if (or current-network?
            (not (get-in db [:networks/networks network])))
      {:ui/show-error (i18n/label :t/delete-network-error)}
      {:ui/show-confirmation {:title               (i18n/label :t/delete-network-title)
                              :content             (i18n/label :t/delete-network-confirmation)
                              :confirm-button-text (i18n/label :t/delete)
                              :on-accept           #(re-frame/dispatch [::remove-network-confirmed
                                                                        network])
                              :on-cancel           nil}})))

(rf/defn save-network-settings
  {:events [::save-network-settings-pressed]}
  [{:keys [db] :as cofx} network]
  (rf/merge cofx
            {:db            (assoc db :networks/current-network network)
             :json-rpc/call [{:method     "settings_saveSetting"
                              :params     [:networks/current-network network]
                              :on-success #()}]}
            (node/prepare-new-config {:on-success #(re-frame/dispatch [:logout])})))

(rf/defn remove-network
  {:events [::remove-network-confirmed]}
  [{:keys [db] :as cofx} network]
  (let [networks (dissoc (:networks/networks db) network)]
    {:db            (assoc db :networks/networks networks)
     :json-rpc/call [{:method     "settings_saveSetting"
                      :params     [:networks/networks (vals networks)]
                      :on-success #(re-frame/dispatch [:navigate-back])}]}))

(defn new-network
  [random-id network-name sym upstream-url chain-type chain-id]
  (let [data-dir (str "/ethereum/" (name chain-type) "_rpc")
        config   {:NetworkId      (or (when chain-id (int chain-id))
                                      (chain/chain-keyword->chain-id chain-type))
                  :DataDir        data-dir
                  :UpstreamConfig {:Enabled true
                                   :URL     upstream-url}}]
    {:id     random-id
     :name   network-name
     :symbol sym
     :config config}))

(rf/defn save
  {:events       [::save-network-pressed]
   :interceptors [(re-frame/inject-cofx :random-id-generator)]}
  [{{:networks/keys [manage networks] :as db} :db
    random-id-generator                       :random-id-generator
    :as                                       cofx}]
  (if (valid-manage? manage)
    ;; rename network-id from UI to chain-id
    (let [{:keys [name url chain network-id symbol]} manage
          random-id                                  (string/replace (random-id-generator) "-" "")
          network                                    (new-network random-id
                                                                  (:value name)
                                                                  (:value symbol)
                                                                  (:value url)
                                                                  (:value chain)
                                                                  (:value network-id))
          custom-chain-type?                         (= :custom (:value chain))
          new-networks                               (assoc networks random-id network)]
      (if (or (not custom-chain-type?)
              (chain-id-available? networks network))
        {:db            (-> db
                            (dissoc :networks/manage)
                            (assoc :networks/networks new-networks))
         :json-rpc/call [{:method     "settings_saveSetting"
                          :params     [:networks/networks (vals new-networks)]
                          :on-success #(re-frame/dispatch [:navigate-back])}]}
        {:ui/show-error "chain-id already defined"}))
    {:ui/show-error "invalid network parameters"}))

(rf/defn open-network-details
  {:events [::network-entry-pressed]}
  [cofx network]
  (navigation/navigate-to cofx :network-details {:networks/selected-network network}))
