(ns status-im.bootnodes.core
  (:require [clojure.string :as string]
            [re-frame.core :as re-frame]
            [status-im.i18n.i18n :as i18n]
            [status-im.multiaccounts.update.core :as multiaccounts.update]
            [status-im.navigation :as navigation]
            [status-im.utils.fx :as fx]))

(def address-regex #"enode://[a-zA-Z0-9]+:?(.*)\@\b\d{1,3}\.\d{1,3}\.\d{1,3}\.\d{1,3}\b:(\d{1,5})")

(defn valid-address? [address]
  (re-matches address-regex address))

(defn- build [id bootnode-name address chain]
  {:address address
   :chain   chain
   :id      (string/replace id "-" "")
   :name    bootnode-name})

(fx/defn fetch [cofx id]
  (let [network (get-in cofx [:db :networks/current-network])]
    (get-in cofx [:db :multiaccount :custom-bootnodes network id])))

(fx/defn set-input
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

(fx/defn edit
  {:events [:bootnodes.ui/add-bootnode-pressed]}
  [{:keys [db] :as cofx} id]
  (let [{:keys [id
                address
                name]}   (fetch cofx id)
        fxs              (fx/merge cofx
                                   (set-input :id id)
                                   (set-input :url (str address))
                                   (set-input :name (str name)))]
    (assoc fxs :dispatch [:navigate-to :edit-bootnode])))

(defn custom-bootnodes-in-use?
  [{:keys [db]}]
  (let [network (:networks/current-network db)]
    (get-in db [:multiaccount :custom-bootnodes-enabled? network])))

(fx/defn delete [{{:keys [multiaccount] :as db} :db :as cofx} id]
  (let [network     (:networks/current-network db)
        new-multiaccount (update-in multiaccount [:custom-bootnodes network] dissoc id)]
    (fx/merge cofx
              {:db (assoc db :multiaccount new-multiaccount)}
              (multiaccounts.update/multiaccount-update
               :custom-bootnodes (:custom-bootnodes new-multiaccount)
               {:success-event (when (custom-bootnodes-in-use? cofx)
                                 [:multiaccounts.update.callback/save-settings-success])}))))

(fx/defn upsert
  {:events [:bootnodes.ui/save-pressed]
   :interceptors [(re-frame/inject-cofx :random-id-generator)]}
  [{{:bootnodes/keys [manage] :keys [multiaccount] :as db} :db
    random-id-generator :random-id-generator :as cofx}]
  (let [{:keys [name id url]} manage
        network (:networks/current-network db)
        bootnode (build
                  (or (:value id) (random-id-generator))
                  (:value name)
                  (:value url)
                  network)
        new-bootnodes (assoc-in
                       (:custom-bootnodes multiaccount)
                       [network (:id bootnode)]
                       bootnode)]

    (fx/merge cofx
              {:db       (dissoc db :bootnodes/manage)
               :dispatch [:navigate-back]}
              (multiaccounts.update/multiaccount-update
               :custom-bootnodes new-bootnodes
               {:success-event (when (custom-bootnodes-in-use? cofx)
                                 [:multiaccounts.update.callback/save-settings-success])}))))

(fx/defn toggle-custom-bootnodes
  {:events [:bootnodes.ui/custom-bootnodes-switch-toggled]}
  [{:keys [db] :as cofx} value]
  (let [current-network (:networks/current-network db)
        bootnodes-settings (get-in db [:multiaccount :custom-bootnodes-enabled?])]
    (multiaccounts.update/multiaccount-update
     cofx
     :custom-bootnodes-enabled? (assoc bootnodes-settings current-network value)
     {:success-event [:multiaccounts.update.callback/save-settings-success]})))

(fx/defn set-bootnodes-from-qr
  {:events [:bootnodes.callback/qr-code-scanned]}
  [cofx url]
  (assoc (set-input cofx :url (string/trim url))
         :dispatch [:navigate-back]))

(fx/defn show-delete-bootnode-confirmation
  {:events [:bootnodes.ui/delete-pressed]}
  [_ bootnode-id]
  {:ui/show-confirmation {:title (i18n/label :t/delete-bootnode-title)
                          :content (i18n/label :t/delete-bootnode-are-you-sure)
                          :confirm-button-text (i18n/label :t/delete-bootnode)
                          :on-accept #(re-frame/dispatch [:bootnodes.ui/delete-confirmed bootnode-id])}})

(fx/defn delete-bootnode
  {:events [:bootnodes.ui/delete-confirmed]}
  [cofx bootnode-id]
  (fx/merge cofx
            (delete bootnode-id)
            (navigation/navigate-back)))
