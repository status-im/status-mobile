(ns status-im.bootnodes.core
  (:require [clojure.string :as string]
            [re-frame.core :as re-frame]
            [status-im.multiaccounts.update.core :as multiaccounts.update]
            [status-im.i18n :as i18n]

            [status-im.ui.screens.navigation :as navigation]
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
  (let [network (get-in cofx [:db :network])]
    (get-in cofx [:db :multiaccount :bootnodes network id])))

(fx/defn set-input
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
  [{:keys [db] :as cofx} id]
  (let [{:keys [id
                address
                name]}   (fetch cofx id)
        fxs              (fx/merge cofx
                                   (set-input :id id)
                                   (set-input :url (str address))
                                   (set-input :name (str name)))]
    (assoc fxs :dispatch [:navigate-to :edit-bootnode])))

(defn custom-bootnodes-in-use? [{:keys [db] :as cofx}]
  (let [network (:network db)]
    (get-in db [:multiaccount :settings :bootnodes network])))

(fx/defn delete [{{:keys [multiaccount] :as db} :db :as cofx} id]
  (let [network     (:network db)
        new-multiaccount (update-in multiaccount [:bootnodes network] dissoc id)]
    (fx/merge cofx
              {:db (assoc db :multiaccount new-multiaccount)}
              (multiaccounts.update/multiaccount-update
               (select-keys new-multiaccount [:bootnodes])
               {:success-event (when (custom-bootnodes-in-use? cofx)
                                 [:multiaccounts.update.callback/save-settings-success])}))))

(fx/defn upsert
  [{{:bootnodes/keys [manage] :keys [multiaccount] :as db} :db
    random-id-generator :random-id-generator :as cofx}]
  (let [{:keys [name id url]} manage
        network (:network db)
        bootnode (build
                  (or (:value id) (random-id-generator))
                  (:value name)
                  (:value url)
                  network)
        new-bootnodes (assoc-in
                       (:bootnodes multiaccount)
                       [network (:id bootnode)]
                       bootnode)]

    (fx/merge cofx
              {:db       (dissoc db :bootnodes/manage)
               :dispatch [:navigate-back]}
              (multiaccounts.update/multiaccount-update
               {:bootnodes new-bootnodes}
               {:success-event (when (custom-bootnodes-in-use? cofx)
                                 [:multiaccounts.update.callback/save-settings-success])}))))

(fx/defn toggle-custom-bootnodes
  [{:keys [db] :as cofx} value]
  (let [current-network (:networks/current-network db)
        settings (get-in db [:multiaccount :settings])]
    (multiaccounts.update/update-settings cofx
                                          (assoc-in settings [:bootnodes current-network] value)
                                          {:success-event [:multiaccounts.update.callback/save-settings-success]})))

(fx/defn set-bootnodes-from-qr
  [cofx url]
  (assoc (set-input cofx :url (string/trim url))
         :dispatch [:navigate-back]))

(fx/defn show-delete-bootnode-confirmation
  [_ bootnode-id]
  {:ui/show-confirmation {:title (i18n/label :t/delete-bootnode-title)
                          :content (i18n/label :t/delete-bootnode-are-you-sure)
                          :confirm-button-text (i18n/label :t/delete-bootnode)
                          :on-accept #(re-frame/dispatch [:bootnodes.ui/delete-confirmed bootnode-id])}})

(fx/defn delete-bootnode
  [cofx bootnode-id]
  (fx/merge cofx
            (delete bootnode-id)
            (navigation/navigate-back)))
