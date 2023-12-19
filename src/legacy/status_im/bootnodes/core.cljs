(ns legacy.status-im.bootnodes.core
  (:require
    [clojure.string :as string]
    [legacy.status-im.multiaccounts.update.core :as multiaccounts.update]
    [re-frame.core :as re-frame]
    [status-im2.navigation.events :as navigation]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(def address-regex #"enode://[a-zA-Z0-9]+:?(.*)\@\b\d{1,3}\.\d{1,3}\.\d{1,3}\.\d{1,3}\b:(\d{1,5})")

(defn valid-address?
  [address]
  (re-matches address-regex address))

(defn- build
  [id bootnode-name address chain]
  {:address address
   :chain   chain
   :id      (string/replace id "-" "")
   :name    bootnode-name})

(rf/defn fetch
  [cofx id]
  (let [network (get-in cofx [:db :networks/current-network])]
    (get-in cofx [:db :profile/profile :custom-bootnodes network id])))

(rf/defn set-input
  {:events [:bootnodes.ui/input-changed]}
  [{:keys [db]} input-key value]
  {:db (update
        db
        :bootnodes/manage
        assoc
        input-key
        {:value value
         :error (case input-key
                  :id   false
                  :name (string/blank? value)
                  :url  (not (valid-address? value)))})})

(rf/defn edit
  {:events [:bootnodes.ui/add-bootnode-pressed]}
  [{:keys [db] :as cofx} id]
  (let [{:keys [id
                address
                name]}
        (fetch cofx id)
        fxs (rf/merge cofx
                      (set-input :id id)
                      (set-input :url (str address))
                      (set-input :name (str name)))]
    (assoc fxs :dispatch [:navigate-to :edit-bootnode])))

(defn custom-bootnodes-in-use?
  [{:keys [db]}]
  (let [network (:networks/current-network db)]
    (get-in db [:profile/profile :custom-bootnodes-enabled? network])))

(rf/defn delete
  [{{:profile/keys [profile] :as db} :db :as cofx} id]
  (let [network     (:networks/current-network db)
        new-profile (update-in profile [:custom-bootnodes network] dissoc id)]
    (rf/merge cofx
              {:db (assoc db :profile/profile new-profile)}
              (multiaccounts.update/multiaccount-update
               :custom-bootnodes
               (:custom-bootnodes new-profile)
               {:success-event (when (custom-bootnodes-in-use? cofx)
                                 [:multiaccounts.update.callback/save-settings-success])}))))

(rf/defn upsert
  {:events       [:bootnodes.ui/save-pressed]
   :interceptors [(re-frame/inject-cofx :random-id-generator)]}
  [{{:bootnodes/keys [manage] :profile/keys [profile] :as db} :db
    random-id-generator                                       :random-id-generator
    :as                                                       cofx}]
  (let [{:keys [name id url]} manage
        network               (:networks/current-network db)
        bootnode              (build
                               (or (:value id) (random-id-generator))
                               (:value name)
                               (:value url)
                               network)
        new-bootnodes         (assoc-in
                               (:custom-bootnodes profile)
                               [network (:id bootnode)]
                               bootnode)]

    (rf/merge cofx
              {:db       (dissoc db :bootnodes/manage)
               :dispatch [:navigate-back]}
              (multiaccounts.update/multiaccount-update
               :custom-bootnodes
               new-bootnodes
               {:success-event (when (custom-bootnodes-in-use? cofx)
                                 [:multiaccounts.update.callback/save-settings-success])}))))

(rf/defn toggle-custom-bootnodes
  {:events [:bootnodes.ui/custom-bootnodes-switch-toggled]}
  [{:keys [db] :as cofx} value]
  (let [current-network    (:networks/current-network db)
        bootnodes-settings (get-in db [:profile/profile :custom-bootnodes-enabled?])]
    (multiaccounts.update/multiaccount-update
     cofx
     :custom-bootnodes-enabled?
     (assoc bootnodes-settings current-network value)
     {:success-event [:multiaccounts.update.callback/save-settings-success]})))

(rf/defn set-bootnodes-from-qr
  {:events [:bootnodes.callback/qr-code-scanned]}
  [cofx url]
  (assoc (set-input cofx :url (string/trim url))
         :dispatch
         [:navigate-back]))

(rf/defn show-delete-bootnode-confirmation
  {:events [:bootnodes.ui/delete-pressed]}
  [_ bootnode-id]
  {:ui/show-confirmation {:title               (i18n/label :t/delete-bootnode-title)
                          :content             (i18n/label :t/delete-bootnode-are-you-sure)
                          :confirm-button-text (i18n/label :t/delete-bootnode)
                          :on-accept           #(re-frame/dispatch [:bootnodes.ui/delete-confirmed
                                                                    bootnode-id])}})

(rf/defn delete-bootnode
  {:events [:bootnodes.ui/delete-confirmed]}
  [cofx bootnode-id]
  (rf/merge cofx
            (delete bootnode-id)
            (navigation/navigate-back)))
