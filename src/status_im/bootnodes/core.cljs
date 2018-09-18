(ns status-im.bootnodes.core
  (:require [clojure.string :as string]
            [re-frame.core :as re-frame]
            [status-im.accounts.update.core :as accounts.update]
            [status-im.i18n :as i18n]
            [status-im.ui.screens.navigation :as navigation]
            [status-im.utils.handlers-macro :as handlers-macro]))

(def address-regex #"enode://[a-zA-Z0-9]+:?(.*)\@\b\d{1,3}\.\d{1,3}\.\d{1,3}\.\d{1,3}\b:(\d{1,5})")

(defn valid-address? [address]
  (re-matches address-regex address))

(defn- build [id bootnode-name address chain]
  {:address address
   :chain   chain
   :id      (string/replace id "-" "")
   :name    bootnode-name})

(defn fetch [id cofx]
  (let [network (get-in cofx [:db :network])]
    (get-in cofx [:db :account/account :bootnodes network id])))

(defn set-input [input-key value {:keys [db]}]
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

(defn edit [id {:keys [db] :as cofx}]
  (let [{:keys [id
                address
                name]}   (fetch id cofx)
        fxs              (handlers-macro/merge-fx
                          cofx
                          (set-input :id id)
                          (set-input :url (str address))
                          (set-input :name (str name)))]
    (assoc fxs :dispatch [:navigate-to :edit-bootnode])))

(defn custom-bootnodes-in-use? [{:keys [db] :as cofx}]
  (let [network (:network db)]
    (get-in db [:account/account :settings :bootnodes network])))

(defn delete [id {{:account/keys [account] :as db} :db :as cofx}]
  (let [network     (:network db)
        new-account (update-in account [:bootnodes network] dissoc id)]
    (handlers-macro/merge-fx cofx
                             {:db (assoc db :account/account new-account)}
                             (accounts.update/account-update
                              (select-keys new-account [:bootnodes])
                              (when (custom-bootnodes-in-use? cofx)
                                [:accounts.update.callback/save-settings-success])))))

(defn upsert [{{:bootnodes/keys [manage] :account/keys [account] :as db} :db :as cofx}]
  (let [{:keys [name
                id
                url]} manage
        network       (:network db)
        bootnode      (build
                       (or (:value id) (:random-id cofx))
                       (:value name)
                       (:value url)
                       network)
        new-bootnodes (assoc-in
                       (:bootnodes account)
                       [network (:id bootnode)]
                       bootnode)]

    (handlers-macro/merge-fx
     cofx
     {:db       (dissoc db :bootnodes/manage)
      :dispatch [:navigate-back]}
     (accounts.update/account-update
      {:bootnodes new-bootnodes}
      (when (custom-bootnodes-in-use? cofx)
        [:accounts.update.callback/save-settings-success])))))

(defn toggle-custom-bootnodes [value {:keys [db] :as cofx}]
  (let [network  (get-in db [:account/account :network])
        settings (get-in db [:account/account :settings])]
    (handlers-macro/merge-fx cofx
                             (accounts.update/update-settings
                              (assoc-in settings [:bootnodes network] value)
                              [:accounts.update.callback/save-settings-success]))))

(defn set-bootnodes-from-qr [url cofx]
  (assoc (set-input :url url cofx)
         :dispatch [:navigate-back]))

(defn show-delete-bootnode-confirmation [bootnode-id]
  {:ui/show-confirmation {:title (i18n/label :t/delete-bootnode-title)
                          :content (i18n/label :t/delete-bootnode-are-you-sure)
                          :confirm-button-text (i18n/label :t/delete-bootnode)
                          :on-accept #(re-frame/dispatch [:bootnodes.ui/delete-confirmed bootnode-id])}})

(defn delete-bootnode [bootnode-id cofx]
  (handlers-macro/merge-fx cofx
                           (delete bootnode-id)
                           (navigation/navigate-back)))
