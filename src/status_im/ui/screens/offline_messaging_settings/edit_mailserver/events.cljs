(ns status-im.ui.screens.offline-messaging-settings.edit-mailserver.events
  (:require [clojure.string :as string]
            [re-frame.core :as re-frame]
            [status-im.utils.handlers :refer [register-handler] :as handlers]
            [status-im.utils.handlers-macro :as handlers-macro]
            [status-im.ui.screens.accounts.utils :as accounts.utils]
            [status-im.utils.ethereum.core :as ethereum]
            [status-im.utils.types :as types]
            [status-im.utils.inbox :as utils.inbox]
            [status-im.data-store.mailservers :as data-store.mailservers]))

(defn- new-mailserver [id mailserver-name address]
  (assoc (utils.inbox/address->mailserver address)
         :id (string/replace id "-" "")
         :name mailserver-name))

(defn save-new-mailserver [{{:mailservers/keys [manage] :account/keys [account] :as db} :db :as cofx} _]
  (let [{:keys [name url]} manage
        network            (get (:networks (:account/account db)) (:network db))
        chain              (ethereum/network->chain-keyword network)
        mailserver         (new-mailserver
                            (string/replace (:random-id cofx) "-" "")
                            (:value name)
                            (:value url))]
    {:db (-> db
             (dissoc :mailservers/manage)
             (assoc-in [:inbox/wnodes chain (:id mailserver)] mailserver))
     :data-store/tx [(data-store.mailservers/save-mailserver-tx (assoc
                                                                 mailserver
                                                                 :chain
                                                                 chain))]
     :dispatch [:navigate-back]}))

(defn set-input [input-key value {:keys [db]}]
  {:db (update db :mailservers/manage assoc input-key {:value value
                                                       :error (if (= input-key :name)
                                                                (string/blank? value)
                                                                (not (utils.inbox/valid-enode-address? value)))})})

(handlers/register-handler-fx
 :save-new-mailserver
 [(re-frame/inject-cofx :random-id)]
 save-new-mailserver)

(handlers/register-handler-fx
 :mailserver-set-input
 (fn [cofx [_ input-key value]]
   (set-input input-key value cofx)))

(handlers/register-handler-fx
 :edit-mailserver
 (fn [{db :db} _]
   {:db       (update-in db [:mailservers/manage] assoc
                         :name  {:error true}
                         :url   {:error true})
    :dispatch [:navigate-to :edit-mailserver]}))

(handlers/register-handler-fx
 :set-mailserver-from-qr
 (fn [cofx [_ _ contact-identity]]
   (assoc (set-input :url contact-identity cofx)
          :dispatch [:navigate-back])))
