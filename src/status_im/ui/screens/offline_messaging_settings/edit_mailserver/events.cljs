(ns status-im.ui.screens.offline-messaging-settings.edit-mailserver.events
  (:require [clojure.string :as string]
            [re-frame.core :as re-frame]
            [status-im.models.wnode :as models.wnode]
            [status-im.utils.handlers :refer [register-handler] :as handlers]
            [status-im.utils.handlers-macro :as handlers-macro]
            [status-im.ui.screens.accounts.utils :as accounts.utils]
            [status-im.utils.ethereum.core :as ethereum]
            [status-im.utils.types :as types]
            [status-im.utils.inbox :as utils.inbox]
            [status-im.data-store.mailservers :as data-store.mailservers]))

(defn- build-mailserver [id mailserver-name address]
  (assoc (utils.inbox/address->mailserver address)
         :id (string/replace id "-" "")
         :name mailserver-name))

(defn upsert-mailserver [{{:mailservers/keys [manage] :account/keys [account] :as db} :db :as cofx} _]
  (let [{:keys [name url id]} manage
        network               (get (:networks (:account/account db)) (:network db))
        chain                 (ethereum/network->chain-keyword network)
        mailserver            (build-mailserver
                               (or (:value id)
                                   (string/replace (:random-id cofx) "-" ""))
                               (:value name)
                               (:value url))
        connected-to-wnode?   (models.wnode/current-wnode? (:id mailserver) cofx)]
    {:db (-> db
             (dissoc :mailservers/manage)
             (assoc-in [:inbox/wnodes chain (:id mailserver)] mailserver))
     :data-store/tx [{:transaction
                      (data-store.mailservers/save-mailserver-tx (assoc
                                                                  mailserver
                                                                  :chain
                                                                  chain))
                      ;; we naively logout if the user is connected to the edited mailserver
                      :success-event (when connected-to-wnode? [:logout])}]
     :dispatch [:navigate-back]}))

(defn set-input [input-key value {:keys [db]}]
  {:db (update db :mailservers/manage assoc input-key {:value value
                                                       :error (case input-key
                                                                :id   false
                                                                :name (string/blank? value)
                                                                :url  (not (utils.inbox/valid-enode-address? value)))})})

(defn edit-mailserver [wnode-id {:keys [db] :as cofx}]
  (let [{:keys [id
                address
                password
                name]}   (models.wnode/get-wnode wnode-id cofx)
        url              (when address (models.wnode/build-url address password))
        fxs              (handlers-macro/merge-fx
                          cofx
                          (set-input :id id)
                          (set-input :url (str url))
                          (set-input :name (str name)))]
    (assoc fxs :dispatch [:navigate-to :edit-mailserver])))

(handlers/register-handler-fx
 :upsert-mailserver
 [(re-frame/inject-cofx :random-id)]
 upsert-mailserver)

(handlers/register-handler-fx
 :mailserver-set-input
 (fn [cofx [_ input-key value]]
   (set-input input-key value cofx)))

(handlers/register-handler-fx
 :edit-mailserver
 (fn [cofx [_ wnode-id]]
   (edit-mailserver wnode-id cofx)))

(handlers/register-handler-fx
 :set-mailserver-from-qr
 (fn [cofx [_ _ contact-identity]]
   (assoc (set-input :url contact-identity cofx)
          :dispatch [:navigate-back])))
