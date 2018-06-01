(ns status-im.models.mailserver
  (:require
   [clojure.string :as string]
   [status-im.utils.handlers-macro :as handlers-macro]
   [status-im.utils.ethereum.core :as ethereum]
   [status-im.utils.inbox :as utils.inbox]
   [status-im.data-store.mailservers :as data-store.mailservers]))

(defn- extract-address-components [address]
  (rest (re-matches #"enode://(.*)@(.*)" address)))

(defn- get-chain [db]
  (let [network  (get (:networks (:account/account db)) (:network db))]
    (ethereum/network->chain-keyword network)))

(defn- build [id mailserver-name address]
  (assoc (utils.inbox/address->mailserver address)
         :id (string/replace id "-" "")
         :name mailserver-name))

(defn build-url [address password]
  (let [[initial host] (extract-address-components address)]
    (str "enode://" initial ":" password "@" host)))

(defn set-input [input-key value {:keys [db]}]
  {:db (update
        db
        :mailservers/manage
        assoc
        input-key
        {:value value
         :error (case input-key
                  :id   false
                  :name (string/blank? value)
                  :url  (not (utils.inbox/valid-enode-address? value)))})})

(defn connected? [id {:keys [db]}]
  (let [current-id (get-in db [:account/account :settings :wnode (get-chain db)])]
    (= current-id id)))

(defn fetch [id {:keys [db]}]
  (get-in db [:inbox/wnodes (get-chain db) id]))

(def default? (comp not :user-defined fetch))

(defn delete [id {:keys [db] :as cofx}]
  (when-not (or
             (default? id cofx)
             (connected? id cofx))
    {:db            (update-in db [:inbox/wnodes (get-chain db)] dissoc id)
     :data-store/tx [(data-store.mailservers/delete-tx id)]}))

(defn edit [id {:keys [db] :as cofx}]
  (let [{:keys [id
                address
                password
                name]}   (fetch id cofx)
        url              (when address (build-url address password))
        fxs              (handlers-macro/merge-fx
                          cofx
                          (set-input :id id)
                          (set-input :url (str url))
                          (set-input :name (str name)))]
    (assoc fxs :dispatch [:navigate-to :edit-mailserver])))

(defn upsert [{{:mailservers/keys [manage] :account/keys [account] :as db} :db :as cofx}]
  (let [{:keys [name url id]} manage
        network               (get (:networks (:account/account db)) (:network db))
        chain                 (ethereum/network->chain-keyword network)
        mailserver            (build
                               (or (:value id)
                                   (string/replace (:random-id cofx) "-" ""))
                               (:value name)
                               (:value url))
        current               (connected? (:id mailserver) cofx)]
    {:db (-> db
             (dissoc :mailservers/manage)
             (assoc-in [:inbox/wnodes chain (:id mailserver)] mailserver))
     :data-store/tx [{:transaction
                      (data-store.mailservers/save-tx (assoc
                                                       mailserver
                                                       :chain
                                                       chain))
                      ;; we naively logout if the user is connected to the edited mailserver
                      :success-event (when current [:logout])}]
     :dispatch [:navigate-back]}))
